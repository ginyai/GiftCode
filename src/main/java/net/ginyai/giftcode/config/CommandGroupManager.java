package net.ginyai.giftcode.config;

import com.google.common.reflect.TypeToken;
import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.object.CommandGroup;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CommandGroupManager {
    private static final ConfigurationOptions options;
    private static final TypeToken<CommandGroup> token = TypeToken.of(CommandGroup.class);

    static {
        TypeSerializerCollection serializers = TypeSerializers.getDefaultSerializers().newChild();
        serializers.registerType(TypeToken.of(CommandGroup.class),new CommandGroup.Serializer());
        options = ConfigurationOptions.defaults().setSerializers(serializers);
    }

    private GiftCodePlugin plugin = GiftCodePlugin.getPlugin();
    private Path configDir;
    private Path path;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private CommentedConfigurationNode node;

    private Map<String,CommandGroup> groupMap;

    public CommandGroupManager(Path configDir){
        this.configDir = configDir;
        this.path = configDir.resolve("command_groups.conf");
        loader = HoconConfigurationLoader.builder().setPath(path).build();
    }

    public void reload() throws IOException, ObjectMappingException {
        if(!Files.exists(configDir)){
            Files.createDirectories(configDir);
        }
        //noinspection ConstantConditions
        Sponge.getAssetManager()
                .getAsset(plugin,"example_command_groups.conf").get()
                .copyToFile(path,false);
        node = loader.load(options);
        groupMap = new TreeMap<>();
        for(Map.Entry<Object, ? extends CommentedConfigurationNode> node:node.getNode("command_groups").getChildrenMap().entrySet()){
            groupMap.put(node.getKey().toString(),node.getValue().getValue(token));
        }
    }

    public Map<String, CommandGroup> getCommandGroupMap() {
        return groupMap;
    }

    @Nullable
    public CommandGroup getCommandGroup(String name){
        return groupMap.get(name);
    }

    public Set<String> getCommandGroups(){
        return groupMap.keySet();
    }

    public boolean addGroup(CommandGroup group) throws IOException, ObjectMappingException {
        if(groupMap.containsKey(group.getName())){
            return false;
        }
        groupMap.put(group.getName(),group);
        save();
        return true;
    }

    public boolean updateGroup(CommandGroup group) throws IOException, ObjectMappingException {
        if(!groupMap.containsKey(group.getName())){
            return false;
        }
        groupMap.put(group.getName(),group);
        save();
        return true;
    }

    private void save() throws ObjectMappingException, IOException {
        node.getNode("command_groups").setValue(new TypeToken<Map<String, CommandGroup>>() {},groupMap);
        loader.save(node);
    }

}
