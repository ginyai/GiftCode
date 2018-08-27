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
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NonnullByDefault
public class CommandMain extends AbstractCommand {

    private Map<List<String>,CommandCallable> childrenMap = new HashMap<>();
    private Map<String,ICommand> children = new HashMap<>();

    private GiftCodePlugin plugin = GiftCodePlugin.getPlugin();

    public CommandMain(){
        super("giftcode","gf");
        addSubCommand(new CommandHelp(childrenMap));
        addSubCommand(new CommandReload());
        addSubCommand(new CommandUse());
        addSubCommand(new CommandGenerate());
        addSubCommand(new CommandExport());
        commandSpec = CommandSpec.builder()
                .children(childrenMap)
                .arguments(getArgument())
                .executor(this)
                .description(getDescription())
                .permission(getPermission("base"))
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(getMessage("use-help", "help_command", "/" + plugin.getMainCommandAlias() + " help"));
        return CommandResult.success();
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.none();
    }

    private void addSubCommand(ICommand command){
        childrenMap.put(command.getNameList(), command.getCallable());
        children.put(command.getName(),command);
    }

    @Nullable
    public ICommand getChild(String name){
        return children.get(name);
    }
}
