package net.ginyai.giftcode.command.args;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

public class ArgLocalDateTime extends CommandElement {

    private static final Text ERROR = Text.of("Failed to parse.");

    public ArgLocalDateTime(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        try {
            return LocalDateTime.parse(args.next());
        }catch (DateTimeParseException e){
            ArgumentParseException exception = args.createError(ERROR);
            exception.addSuppressed(e);
            throw exception;
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Collections.emptyList();
    }
}
