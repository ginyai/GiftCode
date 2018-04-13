package net.ginyai.giftcode.command;

import net.ginyai.giftcode.GiftCodePlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

public class CommandReload implements ICommand {
    @Override
    public String getPermission() {
        return GiftCodePlugin.PLUGIN_ID+".command.reload";
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.none();
    }

    @Override
    public Text getDescription() {
        return Text.of("Reload the plugin's configs.");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        try {
            GiftCodePlugin.getInstance().reload();
        } catch (Exception e) {
            GiftCodePlugin.getInstance().getLogger().error("Failed to reload config.",e);
            throw new CommandException(Text.of("Plugin failed to reload config."),e);
        }
        return CommandResult.success();
    }
}
