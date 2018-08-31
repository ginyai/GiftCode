package net.ginyai.giftcode.command.args;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.object.CommandGroup;
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
public class ArgCommandGroup extends CommandElement {

    public ArgCommandGroup(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        GiftCodePlugin plugin = GiftCodePlugin.getPlugin();
        String groupName = args.next();
        CommandGroup command = plugin.getCommandGroupManager().getCommandGroup(groupName);
        if(command == null){
            throw args.createError(plugin.getMessages().getMessage("giftcoode.args.no-group","name",groupName));
        }
        return command;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        String prefix =args.nextIfPresent().orElse("").toLowerCase();
        return GiftCodePlugin.getPlugin().getCommandGroupManager().getCommandGroups().stream().filter(s->s.toLowerCase().startsWith(prefix)).collect(Collectors.toList());
    }
}
