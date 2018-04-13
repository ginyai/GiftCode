package net.ginyai.giftcode;

import com.google.inject.Inject;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(
        id = "giftcode",
        name = "GiftCode",
        description = "GiftCodePlugin",
        authors = {
                "GiNYAi"
        }
)
public class GiftCodePlugin {
    public static final String PLUGIN_ID = "giftcode";
    public static final String PLUGIN_NAME = "GiftCode";
    public static final String VERSION = "0.1.0";

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

    private ICodeStorage codeStorage;
    private ILogStorage logStorage;

    public Logger getLogger() {
        return logger;
    }

    public Config getConfig() {
        return config;
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
    }

    @Listener
    public void onGamePreInit(GamePreInitializationEvent event){
        instance = this;
        this.config = new Config(configDir);
        try {
            reload();
        }catch (Exception e){
            logger.error("Failed to load configs.",e);
        }
    }

    @Listener
    public void onServerStart(GameStartingServerEvent event) {
        //todo: 注册命令
    }


    @Listener
    public void onGameReload(GameReloadEvent event) {
        try {
            reload();
        }catch (Exception e){
            logger.error("Failed to reload configs.",e);
        }
    }

}
