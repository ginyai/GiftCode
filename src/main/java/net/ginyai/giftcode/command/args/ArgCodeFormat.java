package net.ginyai.giftcode.command.args;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.object.CodeFormat;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class ArgCodeFormat extends CommandElement {

    private Text errorMessage = Text.of("Cannot find code format with the name ");

    public ArgCodeFormat(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        CodeFormat format = GiftCodePlugin.getInstance().getConfig().getCodeFormat(args.next());
        if(format == null){
            throw args.createError(errorMessage);
        }
        return format;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        String prefix =args.nextIfPresent().orElse("").toLowerCase();
        return GiftCodePlugin.getInstance().getConfig().getCodeFormats().stream().filter(s->s.toLowerCase().startsWith(prefix)).collect(Collectors.toList());
    }
}
