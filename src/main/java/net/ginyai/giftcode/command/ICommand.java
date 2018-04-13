package net.ginyai.giftcode.command;


import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public interface ICommand extends CommandExecutor {
    default CommandSpec getCommandSpec(){
        return CommandSpec.builder()
                .arguments(getArgument())
                .permission(getPermission())
                .description(getDescription())
                .executor(this)
                .build();
    }

    String getPermission();
    CommandElement getArgument();
    Text getDescription();
}
