package net.ginyai.giftcode;

import com.google.inject.Inject;
import net.ginyai.giftcode.command.CommandMain;
import net.ginyai.giftcode.config.CommandGroupManager;
import net.ginyai.giftcode.config.Config;
import net.ginyai.giftcode.exception.DataException;
import net.ginyai.giftcode.object.CommandGroup;
import net.ginyai.giftcode.query.QueryManager;
import net.ginyai.giftcode.storage.ICodeStorage;
import net.ginyai.giftcode.storage.ILogStorage;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

@Plugin(
        id = GiftCodePlugin.PLUGIN_ID,
        version = GiftCodePlugin.VERSION,
        name = GiftCodePlugin.PLUGIN_NAME,
        description = "GiftCodePlugin",
        authors = {
                "GiNYAi"
        }
)
public class GiftCodePlugin {
    public static final String PLUGIN_ID = "giftcode";
    public static final String PLUGIN_NAME = "GiftCode";
    public static final String VERSION = "@version@";

    private static GiftCodePlugin instance;

    public GiftCodePlugin() {
    }

    public static GiftCodePlugin getPlugin() {
        return instance;
    }

    public static Text getMessage(String key){
        return instance.getMessages().getMessage(key);
    }

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private Config config;
    private CommandGroupManager commandGroupManager;
    private Messages messages;
    private QueryManager queryManager;

    private SpongeExecutorService syncExecutor;
    private SpongeExecutorService asyncExecutor;

    private ICodeStorage codeStorage;
    private ILogStorage logStorage;

    private String mainCommandAlias;

    public Logger getLogger() {
        return logger;
    }

    public Config getConfig() {
        return config;
    }

    public CommandGroupManager getCommandGroupManager() {
        return commandGroupManager;
    }

    public Messages getMessages() {
        return messages;
    }

    public QueryManager getQueryManager() {
        return queryManager;
    }

    public Path getConfigDir() {
        return configDir;
    }

    public SpongeExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    public SpongeExecutorService getSyncExecutor() {
        return syncExecutor;
    }

    public ICodeStorage getCodeStorage() {
        return codeStorage;
    }

    public ILogStorage getLogStorage() {
        return logStorage;
    }

    public String getMainCommandAlias() {
        return mainCommandAlias;
    }

    public void reload() throws IOException, ObjectMappingException {
        messages.reload();
        config.reload();
        commandGroupManager.reload();
        this.codeStorage = config.getCodeStorage();
        this.logStorage = config.getLogStorage();
        codeStorage.init();
        logStorage.init();
        this.queryManager.reload();
    }

    @Listener
    public void onGamePreInit(GamePreInitializationEvent event){
        instance = this;
        this.syncExecutor = Sponge.getScheduler().createSyncExecutor(this);
        this.asyncExecutor = Sponge.getScheduler().createAsyncExecutor(this);
        this.config = new Config(configDir);
        this.commandGroupManager = new CommandGroupManager(configDir);
        this.messages = new Messages();
        this.queryManager = new QueryManager();
        try {
            reload();
        }catch (Exception e){
            logger.error("Failed to load configs.",e);
        }
    }

    @Listener
    public void onServerStart(GameStartingServerEvent event) {
        CommandMain commandMain = new CommandMain();
        Optional<CommandMapping> optionalCommandMapping =
                Sponge.getCommandManager().register(this,commandMain.getCallable(),commandMain.getNameList());
        optionalCommandMapping.ifPresent(commandMapping -> mainCommandAlias = commandMapping.getPrimaryAlias());
        Sponge.getCommandManager().register(this,Objects.requireNonNull(commandMain.getChild("use")).getCallable(),config.getUseCommandAlias());
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event){
        Sponge.getScheduler().createTaskBuilder().name("Query Tick")
                .execute(queryManager::tick).intervalTicks(1).submit(this);
    }


    @Listener
    public void onGameReload(GameReloadEvent event) {
        try {
            reload();
        }catch (Exception e){
            logger.error("Failed to reload configs.",e);
        }
    }

    public void log(Player player, String code, CommandGroup group){
        asyncExecutor.execute(()-> {
            try {
                logStorage.log(player,code,group);
            } catch (DataException e) {
                logger.error("Failed to log code usage.",e);
            }
        });
    }

}
