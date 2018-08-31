package net.ginyai.giftcode.config;

import com.google.common.reflect.TypeToken;
import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.object.CodeFormat;
import net.ginyai.giftcode.object.CommandGroup;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Config {

    public static final int CURRENT_VERSION = 4;

    private GiftCodePlugin plugin;
    private Path configDir;

    private Path mainConfigPath;
    private ConfigurationLoader<CommentedConfigurationNode> mainConfigLoader;

    private String databasePrefix;
    private String jdbcUrl;
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

    public Config(Path configDir) {
        this.plugin = GiftCodePlugin.getPlugin();
        this.configDir = configDir;
        this.mainConfigPath = configDir.resolve("main.conf");
        mainConfigLoader = HoconConfigurationLoader.builder().setPath(mainConfigPath).build();
    }

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
        charSets = new HashMap<>();
        removeOutdatedCode = rootNode.getNode("remove_outdated_code").getBoolean(false);
        removeUsedUpCode = rootNode.getNode("remove_used_up_code").getBoolean(false);
        useCommandAlias = rootNode.getNode("use_command_alias").getList(TypeToken.of(String.class));
        databasePrefix = rootNode.getNode("database","prefix").getString("");
        jdbcUrl =  rootNode.getNode("database","jdbc_url").getString();
        for(Map.Entry<Object, ? extends CommentedConfigurationNode> node:rootNode.getNode("random_char_set").getChildrenMap().entrySet()){
            charSets.put(node.getKey().toString(),node.getValue().getString());
        }
        codeFormatMap = new HashMap<>();
        for(Map.Entry<Object, ? extends CommentedConfigurationNode> node:rootNode.getNode("code_formats").getChildrenMap().entrySet()){
            codeFormatMap.put(node.getKey().toString(),new CodeFormat(node.getValue().getString()));
        }
        playerQueryMin = rootNode.getNode("query","player","min").getInt(0);
        playerQueryMax = Math.max(playerQueryMin,rootNode.getNode("query","player","max").getInt(100000));
        playerQueryPunish = rootNode.getNode("query","player","punish").getInt(1000);
        globalQueryMin = rootNode.getNode("query","global","min").getInt(0);
        globalQueryMax = Math.max(globalQueryMin,rootNode.getNode("query","global","max").getInt(100000));
        globalQueryPunish = rootNode.getNode("query","global","punish").getInt(1000);
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

    public String getDatabasePrefix() {
        return databasePrefix;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
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
