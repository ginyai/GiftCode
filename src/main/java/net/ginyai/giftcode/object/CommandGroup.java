package net.ginyai.giftcode.object;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;

public class CommandGroup {
    private String name;
    private List<String> commands;

    public String getName() {
        return name;
    }

    public void process(Player player){
        for(String commandLine:commands){
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(),commandLine.replaceAll("\\{player}",player.getName()));
        }
    }

    public static class Serializer implements TypeSerializer<CommandGroup>{

        @Override
        public CommandGroup deserialize(TypeToken<?> typeToken, ConfigurationNode node) throws ObjectMappingException {
            CommandGroup group = new CommandGroup();
            group.name = node.getKey().toString();
            group.commands = node.getNode("commands").getList(TypeToken.of(String.class));
            return group;
        }

        @Override
        public void serialize(TypeToken<?> typeToken, CommandGroup group, ConfigurationNode node) throws ObjectMappingException {
            node.getNode("commands").setValue(group.commands);
        }
    }

}
