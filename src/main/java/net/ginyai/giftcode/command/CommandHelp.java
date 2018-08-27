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
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Map;

@NonnullByDefault
public class CommandHelp extends AbstractCommand {
    private static final GiftCodePlugin plugin = GiftCodePlugin.getPlugin();
    private Map<List<String>, CommandCallable> childrenMap;

    public CommandHelp(Map<List<String>, CommandCallable> childrenMap) {
        super("help");
        this.childrenMap = childrenMap;
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.none();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Text.Builder builder = Text.builder();
        builder.append(getMessage("header"));
        for (Map.Entry<List<String>, CommandCallable> entry : childrenMap.entrySet()) {
            if (entry.getValue().testPermission(src)) {
                builder.append(Text.NEW_LINE,
                        Text.of(TextColors.GRAY, "/" + plugin.getMainCommandAlias() + " " + entry.getKey().get(0) + " "),
                        entry.getValue().getUsage(src), Text.NEW_LINE,
                        entry.getValue().getShortDescription(src).orElse(Text.of("no description"))
                );
            }
        }
        Text text = builder.build();
        src.sendMessage(text);
        return CommandResult.success();
    }
}