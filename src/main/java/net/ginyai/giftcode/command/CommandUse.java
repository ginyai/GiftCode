package net.ginyai.giftcode.command;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.command.args.ArgPermissionOther;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class CommandUse extends AbstractCommand {

    public CommandUse() {
        super("use");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.seq(
                new ArgPermissionOther(Text.of("player"),getPermission("other")),
                GenericArguments.string(Text.of("code"))
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        for(Player player:args.<Player>getAll("player")) {
            String code = args.<String>getOne("code").get();
            if(GiftCodePlugin.getPlugin().getQueryManager().query(player,code)){
                player.sendMessage(getMessage("querying"));
            }else {
                player.sendMessage(getMessage("please-wait"));
            }
        }
        return CommandResult.success();
    }
}
