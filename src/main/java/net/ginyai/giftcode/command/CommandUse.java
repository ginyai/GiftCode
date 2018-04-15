package net.ginyai.giftcode.command;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.object.CommandGroup;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.Optional;

public class CommandUse implements ICommand {
    @Override
    public String getPermission() {
        return GiftCodePlugin.PLUGIN_ID+".command.use.base";
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.seq(
                GenericArguments.optional(GenericArguments.requiringPermission(
                        GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of("player"))),
                        GiftCodePlugin.PLUGIN_ID+".command.use.other"
                )),
                GenericArguments.string(Text.of("code"))
        );
    }

    @Override
    public Text getDescription() {
        return Text.of("Use the code.");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player player = null;
        if(args.hasAny("player")){
            player = args.<Player>getOne("player").get();
        }else {
            if(src instanceof Player){
                player = (Player) src;
            }else {
                throw new CommandException(Text.of("Need a player specified."));
            }
        }
        String code = args.<String>getOne("code").get();
        Optional<CommandGroup> optionalCommandGroup = GiftCodePlugin.getInstance().getCodeStorage().useCode(code);
        if(optionalCommandGroup.isPresent()){
            optionalCommandGroup.get().process(player);
            GiftCodePlugin.getInstance().getLogStorage().log(player,code,optionalCommandGroup.get());
            return CommandResult.success();
        }else {
            throw new CommandException(Text.of("Wrong code"));
        }
    }
}
