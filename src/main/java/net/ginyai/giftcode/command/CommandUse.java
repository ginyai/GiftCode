package net.ginyai.giftcode.command;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.command.args.ArgPermissionOther;
import net.ginyai.giftcode.object.CommandGroup;
import net.ginyai.giftcode.object.GiftCode;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

import static net.ginyai.giftcode.util.Messages.*;

public class CommandUse implements ICommand {
    @Override
    public String getPermission() {
        return GiftCodePlugin.PLUGIN_ID+".command.use.base";
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.seq(
                new ArgPermissionOther(Text.of("player"),GiftCodePlugin.PLUGIN_ID+".command.use.other"),
                GenericArguments.string(Text.of("code"))
        );
    }

    @Override
    public Text getDescription() {
        return Text.of("Use the code.");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        for(Player player:args.<Player>getAll("player")) {
            String code = args.<String>getOne("code").get();
            Optional<GiftCode> optionalGiftCode = GiftCodePlugin.getInstance().getCodeStorage().getCode(code);
            if (optionalGiftCode.isPresent()) {
                optionalGiftCode.get().process(player);
            } else {
                if(GiftCodePlugin.getInstance().getLogStorage().isUsed(code)){
                    throw new CommandException(getText("use.used"));
                }
                throw new CommandException(getText("use.wrong-code"));
            }
        }
        return CommandResult.success();
    }
}
