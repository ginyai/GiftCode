package net.ginyai.giftcode;

import com.google.inject.Inject;
import net.ginyai.giftcode.command.CommandMain;
import net.ginyai.giftcode.command.CommandUse;
import net.ginyai.giftcode.object.CommandGroup;
import net.ginyai.giftcode.query.QueryManager;
import net.ginyai.giftcode.storage.ICodeStorage;
import net.ginyai.giftcode.storage.ILogStorage;
import net.ginyai.giftcode.storage.SqlStorage;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.io.IOException;
import java.nio.file.Path;

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
    public static final String VERSION = "0.2.0";

    private static GiftCodePlugin instance;

    public static GiftCodePlugin getInstance() {
        return instance;
    }

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private Config config;
    private QueryManager queryManager;

    private SpongeExecutorService syncExecutor;
    private SpongeExecutorService asyncExecutor;

    private ICodeStorage codeStorage;
    private ILogStorage logStorage;

    public Logger getLogger() {
        return logger;
    }

    public Config getConfig() {
        return config;
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

    public void reload() throws IOException, ObjectMappingException {
        config.reload();
        SqlStorage sqlStorage = new SqlStorage(config.getJdbcUrl(),config.getDatabasePrefix());
        this.codeStorage = sqlStorage;
        this.logStorage = sqlStorage;
        this.queryManager.reload();
    }

    @Listener
    public void onGamePreInit(GamePreInitializationEvent event){
        instance = this;
        this.syncExecutor = Sponge.getScheduler().createSyncExecutor(this);
        this.asyncExecutor = Sponge.getScheduler().createAsyncExecutor(this);
        this.config = new Config(configDir);
        this.queryManager = new QueryManager();
        try {
            reload();
        }catch (Exception e){
            logger.error("Failed to load configs.",e);
        }
    }

    @Listener
    public void onServerStart(GameStartingServerEvent event) {
        Sponge.getCommandManager().register(this,new CommandMain().getCommandSpec(),GiftCodePlugin.PLUGIN_ID);
        Sponge.getCommandManager().register(this,new CommandUse().getCommandSpec(),config.getUseCommandAlias());
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
        asyncExecutor.execute(()->logStorage.log(player,code,group));
    }

}
