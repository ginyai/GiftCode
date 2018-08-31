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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CommandGroup {
    private String name;
    private List<String> commands;
    @Nullable
    private Text successMessage;

    public CommandGroup(String name,List<String> commands,Text successMessage){
        this.name = Objects.requireNonNull(name);
        this.commands = Objects.requireNonNull(commands);
        this.successMessage = successMessage;
    }

    public String getName() {
        return name;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public void process(Player player){
        for(String commandLine:commands){
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(),commandLine.replaceAll("\\{player}",player.getName()));
        }
        if(successMessage!=null){
            player.sendMessage(successMessage);
        }else {
            player.sendMessage(GiftCodePlugin.getMessage("giftcode.code.default-success"));
        }
    }

    public Text getSuccessMessage() {
        return successMessage;
    }

    public static class Serializer implements TypeSerializer<CommandGroup>{

        @Override
        public CommandGroup deserialize(TypeToken<?> typeToken, ConfigurationNode node) throws ObjectMappingException {
            String name = node.getKey().toString();
            List<String> commands = node.getNode("commands").getList(TypeToken.of(String.class));
            Text successMessage = Optional.ofNullable(node.getNode("succes_message").getString(null))
                    .map(Messages::parseText).orElse(null);
            return new CommandGroup(name,commands,successMessage);
        }

        @Override
        public void serialize(TypeToken<?> typeToken, CommandGroup group, ConfigurationNode node) throws ObjectMappingException {
            node.getNode("commands").setValue(group.commands);
            if(group.successMessage!=null){
                node.getNode("succes_message").setValue(TextSerializers.JSON.serialize(group.successMessage));
            }
        }

    }

}
