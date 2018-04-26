package net.ginyai.giftcode.command;

import net.ginyai.giftcode.GiftCodePlugin;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandMain implements ICommand {
    private Text description = Text.of("Gif code.");
    private Text message = Text.builder(GiftCodePlugin.PLUGIN_NAME+" By GiNYAi")
            .append(Text.NEW_LINE)
            .append(Text.of("Usage:"+GiftCodePlugin.PLUGIN_ID+" help")).build();
    private CommandSpec commandSpec;
    private Map<List<String>,CommandCallable> childrenMap = new HashMap<>();

    private GiftCodePlugin plugin = GiftCodePlugin.getInstance();

    public CommandMain(){
        addSubCommand(new CommandHelp(childrenMap).getCommandSpec(),"help");
        addSubCommand(new CommandReload().getCommandSpec(),"reload");
        addSubCommand(new CommandUse().getCommandSpec(),"use");
        addSubCommand(new CommandGenerate().getCommandSpec(),"generate");
        addSubCommand(new CommandExport().getCommandSpec(),"export");
        commandSpec = CommandSpec.builder()
                .children(childrenMap)
                .arguments(getArgument())
                .executor(this)
                .description(getDescription())
                .permission(getPermission()).build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(message);
        return CommandResult.success();
    }

    @Override
    public CommandSpec getCommandSpec() {
        return commandSpec;
    }

    @Override
    public String getPermission() {
        return GiftCodePlugin.PLUGIN_ID+".command.main";
    }

    @Override
    public Text getDescription() {
        return description;
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.none();
    }

    private void addSubCommand(CommandCallable command,String... aliases){
        childrenMap.put(Arrays.asList(aliases),command);
    }
}
