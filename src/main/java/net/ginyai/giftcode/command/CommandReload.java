package net.ginyai.giftcode.command;

import net.ginyai.giftcode.GiftCodePlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class CommandReload extends AbstractCommand {

    public CommandReload() {
        super("reload");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.none();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        try {
            GiftCodePlugin.getPlugin().reload();
            src.sendMessage(getMessage("reloaded"));
            return CommandResult.success();
        } catch (Exception e) {
            GiftCodePlugin.getPlugin().getLogger().error("Failed to reload config.",e);
            throw new CommandException(getMessage("failed"),e);
        }
    }
}
