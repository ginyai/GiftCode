package net.ginyai.giftcode.command;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.command.args.ArgCodeFormat;
import net.ginyai.giftcode.command.args.ArgCommandGroup;
import net.ginyai.giftcode.command.args.ArgLocalDateTime;
import net.ginyai.giftcode.object.CodeFormat;
import net.ginyai.giftcode.object.CommandGroup;
import net.ginyai.giftcode.object.GiftCode;
import net.ginyai.giftcode.storage.ICodeStorage;
import net.ginyai.giftcode.util.Export;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@NonnullByDefault
public class CommandGenerate extends AbstractCommand {

    public CommandGenerate() {
        super("generate","gen");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.flags()
                .valueFlag(GenericArguments.integer(Text.of("usage-count")),"u","-use")
                .valueFlag(new ArgLocalDateTime(Text.of("start-time")),"s","-start")
                .valueFlag(new ArgLocalDateTime(Text.of("end-time")),"e","-end")
                .buildWith(GenericArguments.seq(
                        new ArgCodeFormat(Text.of("format")),
                        new ArgCommandGroup(Text.of("command_group")),
                        GenericArguments.integer(Text.of("amount"))
                ));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CodeFormat format = args.<CodeFormat>getOne("format").get();
        CommandGroup group = args.<CommandGroup>getOne("command_group").get();
        int amount = args.<Integer>getOne("amount").get();
        Optional<Integer> use = args.getOne("usage-count");
        Optional<LocalDateTime> start = args.getOne("start-time");
        Optional<LocalDateTime> end = args.getOne("end-time");
        ICodeStorage storage = GiftCodePlugin.getPlugin().getCodeStorage();
        GiftCodePlugin.getPlugin().getLogger().info("Generating codes...");
        List<GiftCode> toAdd = new ArrayList<>();
            for(int i = 0;i<amount;i++){
            String codeString = format.genCode();
            GiftCode code = new GiftCode(codeString,group);
            use.ifPresent(code::setUseCount);
            start.ifPresent(code::setStartTime);
            end.ifPresent(code::setEndTime);
            toAdd.add(code);
        }
        Collection<GiftCode> added = storage.addCode(toAdd);
        try {
            Path path = Export.export(added.stream().map(GiftCode::getCodeString).collect(Collectors.toList()), group.getName()+"_generated");
            src.sendMessage(getMessage("generated","amount",added.size(),"path",path.toString()));
            return CommandResult.success();
        } catch (IOException e) {
            GiftCodePlugin.getPlugin().getLogger().error("Failed to save codes to file.",e);
            throw new CommandException(getMessage("failed","amount",added.size()),e);
        }
    }
}
