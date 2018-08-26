package net.ginyai.giftcode;

import com.google.common.reflect.TypeToken;
import net.ginyai.giftcode.object.CodeFormat;
import net.ginyai.giftcode.object.CommandGroup;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Config {
    private GiftCodePlugin plugin;
    private Path configDir;

    private Path mainConfigPath;
    private Path commandGroupPath;

    private ConfigurationLoader<CommentedConfigurationNode> mainConfigLoader;
    private ConfigurationLoader<CommentedConfigurationNode> commandGroupLoader;
    private ConfigurationOptions commandGroupOptions;

    private CommentedConfigurationNode mainConfigRootNode;
    private CommentedConfigurationNode commandGroupRootNode;

    private String databasePrefix;
    private String jdbcUrl;
    private List<String> useCommandAlias;

    private boolean removeOutdatedCode = false;
    private boolean removeUsedUpCode = false;
    private Map<String,String> charSets;
    private Map<String,CodeFormat> codeFormatMap;
    private Map<String,CommandGroup> commandGroupMap;

    private int playerQueryMin;
    private int playerQueryMax;
    private int playerQueryPunish;

    private int globalQueryMin;
    private int globalQueryMax;
    private int globalQueryPunish;

    public Config(Path configDir) {
        this.plugin = GiftCodePlugin.getInstance();
        this.configDir = configDir;
        this.mainConfigPath = configDir.resolve("main.conf");
        this.commandGroupPath = configDir.resolve("command_groups.conf");
        mainConfigLoader = HoconConfigurationLoader.builder().setPath(mainConfigPath).build();
        commandGroupLoader = HoconConfigurationLoader.builder().setPath(commandGroupPath).build();
        TypeSerializerCollection serializers = TypeSerializers.getDefaultSerializers().newChild();
        serializers.registerType(TypeToken.of(CommandGroup.class),new CommandGroup.Serializer());
        commandGroupOptions = ConfigurationOptions.defaults().setSerializers(serializers);
    }

    public void reload() throws IOException, ObjectMappingException {
        //todo:自动更新配置文件格式
        if(!configDir.toFile().exists()){
            configDir.toFile().mkdirs();
        }
        if(!mainConfigPath.toFile().exists()){
            Sponge.getAssetManager().getAsset(plugin,"default_config.conf").get().copyToFile(mainConfigPath);
        }
        mainConfigRootNode = mainConfigLoader.load();
        charSets = new HashMap<>();
        removeOutdatedCode = mainConfigRootNode.getNode("remove_outdated_code").getBoolean(false);
        removeUsedUpCode = mainConfigRootNode.getNode("remove_used_up_code").getBoolean(false);
        useCommandAlias = mainConfigRootNode.getNode("use_command_alias").getList(TypeToken.of(String.class));
        databasePrefix = mainConfigRootNode.getNode("database","prefix").getString("");
        jdbcUrl =  mainConfigRootNode.getNode("database","jdbc_url").getString();
        for(Map.Entry<Object, ? extends CommentedConfigurationNode> node:mainConfigRootNode.getNode("random_char_set").getChildrenMap().entrySet()){
            charSets.put(node.getKey().toString(),node.getValue().getString());
        }
        codeFormatMap = new HashMap<>();
        for(Map.Entry<Object, ? extends CommentedConfigurationNode> node:mainConfigRootNode.getNode("code_formats").getChildrenMap().entrySet()){
            codeFormatMap.put(node.getKey().toString(),new CodeFormat(node.getValue().getString()));
        }

        if(!commandGroupPath.toFile().exists()){
            Sponge.getAssetManager().getAsset(plugin,"example_command_groups.conf").get().copyToFile(commandGroupPath);
        }
        commandGroupRootNode = commandGroupLoader.load(commandGroupOptions);
        commandGroupMap = new HashMap<>();
        for(Map.Entry<Object, ? extends CommentedConfigurationNode> node:commandGroupRootNode.getNode("command_groups").getChildrenMap().entrySet()){
            commandGroupMap.put(node.getKey().toString(),node.getValue().getValue(TypeToken.of(CommandGroup.class)));
        }
        playerQueryMin = mainConfigRootNode.getNode("query","player","min").getInt(0);
        playerQueryMax = Math.max(playerQueryMin,mainConfigRootNode.getNode("query","player","max").getInt(100000));
        playerQueryPunish = mainConfigRootNode.getNode("query","player","punish").getInt(1000);
        globalQueryMin = mainConfigRootNode.getNode("query","global","min").getInt(0);
        globalQueryMax = Math.max(globalQueryMin,mainConfigRootNode.getNode("query","global","max").getInt(100000));
        globalQueryPunish = mainConfigRootNode.getNode("query","global","punish").getInt(1000);
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

    public Map<String, CommandGroup> getCommandGroupMap() {
        return commandGroupMap;
    }

    public CommandGroup getCommandGroup(String name){
        return commandGroupMap.get(name);
    }

    public Set<String> getCommandGroups(){
        return commandGroupMap.keySet();
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
