package net.ginyai.giftcode.object;

import com.google.common.reflect.TypeToken;
import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.Messages;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Optional;

public class CommandGroup {
    private String name;
    private List<String> commands;
    private Text successMessage;

    public String getName() {
        return name;
    }

    public void process(Player player){
        for(String commandLine:commands){
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(),commandLine.replaceAll("\\{player}",player.getName()));
        }
        player.sendMessage(successMessage);
    }

    public Text getSuccessMessage() {
        return successMessage;
    }

    public static class Serializer implements TypeSerializer<CommandGroup>{

        @Override
        public CommandGroup deserialize(TypeToken<?> typeToken, ConfigurationNode node) throws ObjectMappingException {
            CommandGroup group = new CommandGroup();
            group.name = node.getKey().toString();
            group.commands = node.getNode("commands").getList(TypeToken.of(String.class));
            group.successMessage = Optional.ofNullable(node.getNode("succes_message").getString(null)).map(Messages::parseText)
                    .orElse(GiftCodePlugin.getMessage("giftcode.code.default-success"));
            return group;
        }

        @Override
        public void serialize(TypeToken<?> typeToken, CommandGroup group, ConfigurationNode node) throws ObjectMappingException {
            node.getNode("commands").setValue(group.commands);
            node.getNode("succes_message").setValue(TextSerializers.JSON.serialize(group.successMessage));
        }

    }

}
