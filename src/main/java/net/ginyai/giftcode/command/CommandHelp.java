package net.ginyai.giftcode.command;

import net.ginyai.giftcode.GiftCodePlugin;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Map;

public class CommandHelp implements ICommand {

    private Text description = Text.of("Show helps of Commands.");
    private Map<List<String>,CommandCallable> subCommandMap;

    CommandHelp(Map<List<String>,CommandCallable> subCommandMap){
        this.subCommandMap = subCommandMap;
    }

    @Override
    public String getPermission() {
        return GiftCodePlugin.PLUGIN_ID+".command.help";
    }

    @Override
    public Text getDescription() {
        return description;
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.none();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Text.Builder textBuilder = Text.builder("Commands:");
        for(Map.Entry<List<String>,CommandCallable> entry:subCommandMap.entrySet()){
            if(entry.getValue().testPermission(src)){
                textBuilder.append(Text.NEW_LINE);
                textBuilder.append(Text.of(entry.getKey().toString()));
                textBuilder.append(Text.NEW_LINE);
                textBuilder.append(Text.of("        :"));
                textBuilder.append(entry.getValue().getHelp(src).orElse(Text.EMPTY));
            }
        }
        src.sendMessage(textBuilder.toText());
        return CommandResult.success();
    }
}