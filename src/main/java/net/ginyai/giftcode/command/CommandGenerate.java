package net.ginyai.giftcode.command;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.command.args.ArgCodeFormat;
import net.ginyai.giftcode.command.args.ArgCommandGroup;
import net.ginyai.giftcode.object.CodeFormat;
import net.ginyai.giftcode.object.CommandGroup;
import net.ginyai.giftcode.storage.ICodeStorage;
import net.ginyai.giftcode.util.Export;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class CommandGenerate implements ICommand {
    @Override
    public String getPermission() {
        return GiftCodePlugin.PLUGIN_ID+".command.generate";
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.seq(
                new ArgCodeFormat(Text.of("format")),
                new ArgCommandGroup(Text.of("command_group")),
                GenericArguments.integer(Text.of("amount"))
        );
    }

    @Override
    public Text getDescription() {
        return Text.of("Generate codes");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CodeFormat format = args.<CodeFormat>getOne("format").get();
        CommandGroup group = args.<CommandGroup>getOne("command_group").get();
        int amount = args.<Integer>getOne("amount").get();
        ICodeStorage storage = GiftCodePlugin.getInstance().getCodeStorage();
        GiftCodePlugin.getInstance().getLogger().info("Generating codes...");
        List<String> added = new ArrayList<>();
        for(int i = 0;i<amount;i++){
            String code = format.genCode();
            if(storage.addCode(code,group)){
                added.add(code);
            }
        }
        try {
            Path path = Export.export(added,group.getName()+"_generated");
            src.sendMessage(Text.of("Added "+added.size()+" codes.Saved to "+path.toString()));
            return CommandResult.success();
        } catch (IOException e) {
            throw new CommandException(Text.of("Codes added but,failed to export."),e);
        }
    }
}
