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
            if(GiftCodePlugin.getInstance().getQueryManager().query(player,code)){
                player.sendMessage(Text.of("Querying ..."));
            }else {
                player.sendMessage(Text.of("Please wait a mount to use again."));
            }
        }
        return CommandResult.success();
    }
}
