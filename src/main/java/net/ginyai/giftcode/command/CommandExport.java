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
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

@NonnullByDefault
public class CommandExport extends AbstractCommand {

    public CommandExport() {
        super("export");
    }

    @Override
    public CommandElement getArgument() {
        return new ArgCommandGroup(Text.of("command_group"));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CommandGroup group = args.<CommandGroup>getOne("command_group").get();
        Collection<String> codes = GiftCodePlugin.getPlugin().getCodeStorage().getCodes(group);
        try {
            Path path = Export.export(codes,group.getName()+"_exported");
            src.sendMessage(getMessage("exported","amount",codes.size(),"path",path.toString()));
            return CommandResult.success();
        } catch (IOException e) {
            GiftCodePlugin.getPlugin().getLogger().error("Failed to export.",e);
            throw new CommandException(getMessage("failed"),e);
        }
    }
}
