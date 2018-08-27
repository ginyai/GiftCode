package net.ginyai.giftcode.command.args;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.object.CodeFormat;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@NonnullByDefault
public class ArgCodeFormat extends CommandElement {

    public ArgCodeFormat(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        GiftCodePlugin plugin = GiftCodePlugin.getPlugin();
        String formatName = args.next();
        CodeFormat format = plugin.getConfig().getCodeFormat(formatName);
        if(format == null){
            throw args.createError(plugin.getMessages().getMessage("giftcoode.args.no-format","name",formatName));
        }
        return format;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        String prefix =args.nextIfPresent().orElse("").toLowerCase();
        return GiftCodePlugin.getPlugin().getConfig().getCodeFormats().stream().filter(s->s.toLowerCase().startsWith(prefix)).collect(Collectors.toList());
    }
}
