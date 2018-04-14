package net.ginyai.giftcode.command;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.command.args.ArgCommandGroup;
import net.ginyai.giftcode.object.CommandGroup;
import net.ginyai.giftcode.util.Export;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public class CommandExport implements ICommand {
    @Override
    public String getPermission() {
        return GiftCodePlugin.PLUGIN_ID+".command.export";
    }

    @Override
    public CommandElement getArgument() {
        return new ArgCommandGroup(Text.of("command_group"));
    }

    @Override
    public Text getDescription() {
        return Text.of("Export available codes to a text file.");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CommandGroup group = args.<CommandGroup>getOne("command_group").get();
        Collection<String> codes = GiftCodePlugin.getInstance().getCodeStorage().getCodes(group);
        try {
            Path path = Export.export(codes,group.getName()+"_exported");
            src.sendMessage(Text.of(codes.size()+" codes saved to "+path.toString()));
            return CommandResult.success();
        } catch (IOException e) {
            throw new CommandException(Text.of("Failed to export."),e);
        }
    }
}
