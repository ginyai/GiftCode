package net.ginyai.giftcode.config;

import com.google.common.reflect.TypeToken;
import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.exception.DataException;
import net.ginyai.giftcode.storage.*;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Objects;

public class StorageProviders {
    private StorageProviders(){}

    private static class SqlProvider implements IStorageProvider{
        private String prefix;
        private String jdbcUrl;
        private SqlCodeStorage codeStorage;
        private SqlLogStorage logStorage;

        private SqlProvider(String prefix, String jdbcUrl) {
            this.prefix = prefix;
            this.jdbcUrl = jdbcUrl;
            IConnectionProvider connectionProvider = ()-> Sponge.getServiceManager().provideUnchecked(SqlService.class)
                    .getDataSource(GiftCodePlugin.getPlugin(),jdbcUrl).getConnection();
            codeStorage = new SqlCodeStorage(connectionProvider,prefix+"codes");
            logStorage = new SqlLogStorage(connectionProvider,prefix+"log");
        }

        @Override
        public ICodeStorage getCodeStorage() {
            return codeStorage;
        }

        @Override
        public ILogStorage getLogStorage() {
            return logStorage;
        }
    }

    private static class ConfigProvider implements IStorageProvider{

        private Path configPath;
        private String type;
        private ConfigFileStorage storage;

        private ConfigProvider(Path configPath, String type) {
            this.configPath = configPath;
            this.type = type;
            ConfigurationLoader<? extends ConfigurationNode> loader;
            switch (type){
                case "yaml":
                    loader = YAMLConfigurationLoader.builder().setPath(configPath).build();
                    break;
                case "hocon":
                    loader = HoconConfigurationLoader.builder().setPath(configPath).build();
                    break;
                case "json":
                    loader = GsonConfigurationLoader.builder().setPath(configPath).build();
                    break;
                default:
                    throw new UnsupportedOperationException(type);
            }
            this.storage = new ConfigFileStorage(loader,()-> {
                try {
                    Files.createDirectories(configPath.getParent());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public ICodeStorage getCodeStorage() throws DataException {
            return storage;
        }

        @Override
        public ILogStorage getLogStorage() throws DataException {
            return storage;
        }
    }

    public static class Serializer implements TypeSerializer<IStorageProvider>{
        @Override
        public IStorageProvider deserialize(TypeToken<?> typeToken, ConfigurationNode node) throws ObjectMappingException {
            String type = Objects.requireNonNull(node.getNode("type").getString());
            switch (type){
                case "sql":
                    return new SqlProvider(
                            node.getNode("prefix").getString(),
                            node.getNode("jdbc_url").getString()
                    );
                case "yaml":
                case "hocon":
                case "json":
                    return new ConfigProvider(
                            Paths.get(node.getNode("file").getString()),
                            type
                    );
                default:
                    throw new ObjectMappingException("Unsupported type "+type);
            }
        }

        @Override
        public void serialize(TypeToken<?> typeToken, IStorageProvider iStorageProvider, ConfigurationNode configurationNode) throws ObjectMappingException {

        }
    }
}
