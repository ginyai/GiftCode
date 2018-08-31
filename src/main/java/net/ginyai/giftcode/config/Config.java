package net.ginyai.giftcode.config;

import com.google.common.reflect.TypeToken;
import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.exception.DataException;
import net.ginyai.giftcode.object.CodeFormat;
import net.ginyai.giftcode.storage.ICodeStorage;
import net.ginyai.giftcode.storage.ILogStorage;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Config {

    public static final int CURRENT_VERSION = 4;
    public static final ConfigurationOptions OPTIONS;

    static {
        ConfigurationOptions options = ConfigurationOptions.defaults();
        TypeSerializerCollection serializers = options.getSerializers();
        serializers.registerType(TypeToken.of(IStorageProvider.class),new StorageProviders.Serializer());
        OPTIONS = options.setSerializers(serializers);
    }

    private GiftCodePlugin plugin;
    private Path configDir;

    private Path mainConfigPath;
    private ConfigurationLoader<CommentedConfigurationNode> mainConfigLoader;

    private List<String> useCommandAlias;

    private boolean removeOutdatedCode = false;
    private boolean removeUsedUpCode = false;
    private Map<String,String> charSets;
    private Map<String,CodeFormat> codeFormatMap;

    private int playerQueryMin;
    private int playerQueryMax;
    private int playerQueryPunish;

    private int globalQueryMin;
    private int globalQueryMax;
    private int globalQueryPunish;

    private Map<String,IStorageProvider> providerMap;
    private IStorageProvider codeProvider;
    private IStorageProvider logProvider;

    public Config(Path configDir) {
        this.plugin = GiftCodePlugin.getPlugin();
        this.configDir = configDir;
        this.mainConfigPath = configDir.resolve("main.conf");
        mainConfigLoader = HoconConfigurationLoader.builder().setPath(mainConfigPath).build();
    }

    @SuppressWarnings("ConstantConditions")
    public void reload() throws IOException, ObjectMappingException {
        Files.createDirectories(configDir);
        Sponge.getAssetManager().getAsset(plugin,"default_config.conf").get().copyToFile(mainConfigPath);
        CommentedConfigurationNode mainConfigRootNode = mainConfigLoader.load();
        int version = mainConfigRootNode.getNode("version").getInt(-1);
        if(version == CURRENT_VERSION){
            reload(mainConfigRootNode);
        }else{
            Files.move(mainConfigPath,configDir.resolve("main.conf.back"+System.currentTimeMillis()));
            if(version<=0){
                Sponge.getAssetManager().getAsset(plugin,"default_config.conf").get().copyToFile(mainConfigPath);
                mainConfigRootNode = mainConfigLoader.load();
                reload(mainConfigRootNode);
            }else if(Updaters.V4.canRead(version)){
                mainConfigRootNode = Updaters.V4.update(mainConfigRootNode);
                mainConfigLoader.save(mainConfigRootNode);
                reload(mainConfigRootNode);
            }else {
                throw new UnsupportedOperationException("config version "+version);
            }
        }
    }

    private void reload(CommentedConfigurationNode rootNode) throws ObjectMappingException {
        removeOutdatedCode = rootNode.getNode("giftcode","remove","outdated").getBoolean(false);
        removeUsedUpCode = rootNode.getNode("giftcode","remove","used-up").getBoolean(false);
        useCommandAlias = rootNode.getNode("giftcode","command-alias","use").getList(TypeToken.of(String.class));
        charSets = new HashMap<>();
        for(Map.Entry<Object, ? extends CommentedConfigurationNode> node:rootNode.getNode("giftcode","random-char-set").getChildrenMap().entrySet()){
            charSets.put(node.getKey().toString(),node.getValue().getString());
        }
        codeFormatMap = new HashMap<>();
        for(Map.Entry<Object, ? extends CommentedConfigurationNode> node:rootNode.getNode("giftcode","code-formats").getChildrenMap().entrySet()){
            codeFormatMap.put(node.getKey().toString(),new CodeFormat(node.getValue().getString()));
        }
        playerQueryMin = rootNode.getNode("giftcode","query","player","min").getInt(0);
        playerQueryMax = Math.max(playerQueryMin,rootNode.getNode("giftcode","query","player","max").getInt(100000));
        playerQueryPunish = rootNode.getNode("giftcode","query","player","punish").getInt(1000);
        globalQueryMin = rootNode.getNode("giftcode","query","global","min").getInt(0);
        globalQueryMax = Math.max(globalQueryMin,rootNode.getNode("giftcode","query","global","max").getInt(100000));
        globalQueryPunish = rootNode.getNode("giftcode","query","global","punish").getInt(1000);

        providerMap = rootNode.getNode("giftcode","storage").getValue(new TypeToken<Map<String, IStorageProvider>>() {}, Collections.emptyMap());
        codeProvider = Objects.requireNonNull(providerMap.get(rootNode.getNode("giftcode","storage-uasge","code").getString()));
        logProvider = Objects.requireNonNull(providerMap.get(rootNode.getNode("giftcode","storage-uasge","log").getString()));
    }

    public List<String> getUseCommandAlias() {
        return useCommandAlias;
    }

    public boolean isRemoveOutdatedCode() {
        return removeOutdatedCode;
    }

    public boolean isRemoveUsedUpCode() {
        return removeUsedUpCode;
    }

    public String getCharSet(String name){
        return charSets.get(name);
    }

    public CodeFormat getCodeFormat(String name){
        return codeFormatMap.get(name);
    }

    public Set<String> getCodeFormats(){
        return codeFormatMap.keySet();
    }

    public ICodeStorage getCodeStorage() throws DataException {
        return codeProvider.getCodeStorage();
    }

    public ILogStorage getLogStorage() throws DataException {
        return logProvider.getLogStorage();
    }

    public int getPlayerQueryMin() {
        return playerQueryMin;
    }

    public int getPlayerQueryMax() {
        return playerQueryMax;
    }

    public int getPlayerQueryPunish() {
        return playerQueryPunish;
    }

    public int getGlobalQueryMin() {
        return globalQueryMin;
    }

    public int getGlobalQueryMax() {
        return globalQueryMax;
    }

    public int getGlobalQueryPunish() {
        return globalQueryPunish;
    }
}
