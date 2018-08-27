package net.ginyai.giftcode.command.args;

import net.ginyai.giftcode.GiftCodePlugin;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

@NonnullByDefault
public class ArgLocalDateTime extends CommandElement {

    public ArgLocalDateTime(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        try {
            return LocalDateTime.parse(args.next());
        }catch (DateTimeParseException e){
            ArgumentParseException exception = args.createError(GiftCodePlugin.getMessage("giftcoode.args.parse-failed"));
            exception.addSuppressed(e);
            throw exception;
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Collections.emptyList();
    }
}
