package net.ginyai.giftcode.command;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.Messages;
import net.ginyai.giftcode.command.args.ArgCommandGroup;
import net.ginyai.giftcode.object.CommandGroup;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NonnullByDefault
@SuppressWarnings("ConstantConditions")
public class CommandGroups extends AbstractCommand {


    private Map<List<String>,CommandCallable> childrenMap = new HashMap<>();
    private Map<String,ICommand> children = new HashMap<>();

    public CommandGroups() {
        super("commandgroups","cg");
        addSubCommand(new Create());
        addSubCommand(new AddCommand());
        addSubCommand(new DelCommand());
        addSubCommand(new InsertCommand());
        addSubCommand(new Info());
        addSubCommand(new ListAll());
        addSubCommand(new CommandHelp(childrenMap,"cg"));
        commandSpec = CommandSpec.builder()
                .children(childrenMap)
                .arguments(getArgument())
                .executor(this)
                .childArgumentParseExceptionFallback(true)
                .description(getDescription())
                .permission(getPermission("base"))
                .build();
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.none();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String command = "/"+GiftCodePlugin.getPlugin().getMainCommandAlias()+" cg help";
        src.sendMessage(getMessage("use-help","help_command",command));
        return CommandResult.success();
    }

    @Override
    public Text getArgHelp(CommandSource source) {
        Text.Builder builder = Text.builder();
        builder.append(GiftCodePlugin.getMessage("giftcode.commands.args"),Text.NEW_LINE);
        builder.append(Text.of("    "),Messages.adjustLength(commandSpec.getUsage(source),30));
        builder.append(getMessage("children"));
        return builder.toText();
    }

    private void addSubCommand(ICommand command){
        childrenMap.put(command.getNameList(), command.getCallable());
        children.put(command.getName(),command);
    }

    @Nullable
    public ICommand getChild(String name){
        return children.get(name);
    }

    public static class Create extends AbstractCommand{

        Create() {
            super("create");
        }

        @Override
        public String getRootPermission() {
            return "giftcode.command.commandgroups" + name;
        }

        @Override
        protected String getMessageKey(String s) {
            return "giftcode.command.commandgroups" + name + "." + s;
        }

        @Override
        public CommandElement getArgument() {
            return GenericArguments.string(Text.of("name"));
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            String name = args.<String>getOne("name").get();
            CommandGroup group = new CommandGroup(name,new ArrayList<>(),null);
            try {
                if(GiftCodePlugin.getPlugin().getCommandGroupManager().addGroup(group)){
                    src.sendMessage(getMessage("success","name",name));
                    return CommandResult.success();
                }else {
                    throw new CommandException(getMessage("exists","name",name));
                }
            } catch (IOException | ObjectMappingException e) {
                GiftCodePlugin.getPlugin().getLogger().error("Failed to save command groups.",e);
                throw new CommandException(getMessage("exception"),e);
            }
        }
    }

    public static class AddCommand extends AbstractCommand{

        AddCommand() {
            super("addcommand","add");
        }

        @Override
        public String getRootPermission() {
            return "giftcode.command.commandgroups." + name;
        }

        @Override
        protected String getMessageKey(String s) {
            return "giftcode.command.commandgroups." + name + "." + s;
        }

        @Override
        public CommandElement getArgument() {
            return GenericArguments.seq(
                    new ArgCommandGroup(Text.of("group")),
                    GenericArguments.remainingJoinedStrings(Text.of("command"))
            );
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            CommandGroup group = args.<CommandGroup>getOne("group").get();
            String command = args.<String>getOne("command").get();
            group.getCommands().add(command);
            try {
                GiftCodePlugin.getPlugin().getCommandGroupManager().updateGroup(group);
                src.sendMessage(getMessage("success"));
                return CommandResult.success();
            } catch (IOException | ObjectMappingException e) {
                GiftCodePlugin.getPlugin().getLogger().error("Failed to save command groups.",e);
                throw new CommandException(getMessage("exception"),e);
            }
        }
    }
    public static class DelCommand extends AbstractCommand{

        public DelCommand() {
            super("delcommand","del");
        }

        @Override
        public String getRootPermission() {
            return "giftcode.command.commandgroups." + name;
        }

        @Override
        protected String getMessageKey(String s) {
            return "giftcode.command.commandgroups." + name + "." + s;
        }

        @Override
        public CommandElement getArgument() {
            return GenericArguments.seq(
                    new ArgCommandGroup(Text.of("group")),
                    GenericArguments.integer(Text.of("index"))
            );
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            CommandGroup group = args.<CommandGroup>getOne("group").get();
            int index = args.<Integer>getOne("index").get();
            List<String> commands = group.getCommands();
            if(index>commands.size()||index<1){
                throw new CommandException(getMessage("not-find"));
            }
            commands.remove(index-1);
            group.setCommands(commands);
            try {
                GiftCodePlugin.getPlugin().getCommandGroupManager().updateGroup(group);
                src.sendMessage(getMessage("success"));
                return CommandResult.success();
            } catch (IOException | ObjectMappingException e) {
                GiftCodePlugin.getPlugin().getLogger().error("Failed to save command groups.",e);
                throw new CommandException(getMessage("exception"),e);
            }
        }
    }
    public static class InsertCommand extends AbstractCommand{

        InsertCommand() {
            super("insertcommand","insert");
        }

        @Override
        public String getRootPermission() {
            return "giftcode.command.commandgroups." + name;
        }

        @Override
        protected String getMessageKey(String s) {
            return "giftcode.command.commandgroups." + name + "." + s;
        }

        @Override
        public CommandElement getArgument() {
            return GenericArguments.seq(
                    new ArgCommandGroup(Text.of("group")),
                    GenericArguments.integer(Text.of("index")),
                    GenericArguments.remainingJoinedStrings(Text.of("command"))
            );
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            CommandGroup group = args.<CommandGroup>getOne("group").get();
            int index = args.<Integer>getOne("index").get();
            List<String> commands = group.getCommands();
            String command = args.<String>getOne("command").get();
            commands.add(index-1,command);
            group.setCommands(commands);
            try {
                GiftCodePlugin.getPlugin().getCommandGroupManager().updateGroup(group);
                src.sendMessage(getMessage("success"));
                return CommandResult.success();
            } catch (IOException | ObjectMappingException e) {
                GiftCodePlugin.getPlugin().getLogger().error("Failed to save command groups.",e);
                throw new CommandException(getMessage("exception"),e);
            }
        }
    }

    public static class Info extends AbstractCommand{

        Info() {
            super("info");
        }

        @Override
        public String getRootPermission() {
            return "giftcode.command.commandgroups." + name;
        }

        @Override
        protected String getMessageKey(String s) {
            return "giftcode.command.commandgroups." + name + "." + s;
        }

        @Override
        public CommandElement getArgument() {
            return new ArgCommandGroup(Text.of("group"));
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            CommandGroup group = args.<CommandGroup>getOne("group").get();
            Text.Builder builder = Text.builder();
            builder.append(getMessage("group-name","name",group.getName()));
            List<String> commands = group.getCommands();
            for(int i = 0;i<commands.size();i++){
                builder.append(Text.NEW_LINE,Text.of("    ",commands.get(i)),Text.NEW_LINE);
                builder.append(Text.of("    "),
                        Text.builder("Del")
                                .onClick(TextActions.suggestCommand(String.format("/%s cg del %s %d",GiftCodePlugin.getPlugin().getMainCommandAlias(),group.getName(),i+1)))
                                .style(TextStyles.UNDERLINE)
                                .build(),
                        Text.of("    "),
                        Text.builder("Insert")
                                .style(TextStyles.UNDERLINE)
                                .onClick(TextActions.suggestCommand(String.format("/%s cg insert %s %d",GiftCodePlugin.getPlugin().getMainCommandAlias(),group.getName(),i+1)))
                                .build());
            }builder.append(Text.of("    "),
                    Text.builder("Add")
                            .onClick(TextActions.suggestCommand(String.format("/%s cg add %s ",GiftCodePlugin.getPlugin().getMainCommandAlias(),group.getName())))
                            .style(TextStyles.UNDERLINE)
                            .build());
            src.sendMessage(builder.build());
            return CommandResult.success();
        }
    }

    public static class ListAll extends AbstractCommand{

        ListAll() {
            super("list");
        }

        @Override
        public String getRootPermission() {
            return "giftcode.command.commandgroups." + name;
        }

        @Override
        protected String getMessageKey(String s) {
            return "giftcode.command.commandgroups." + name + "." + s;
        }

        @Override
        public CommandElement getArgument() {
            return GenericArguments.none();
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            Text.Builder builder = Text.builder();
            builder.append(getMessage("header"));
            for(String name:GiftCodePlugin.getPlugin().getCommandGroupManager().getCommandGroups()){
                builder.append(Text.NEW_LINE,
                        Text.builder(name)
                                .onClick(TextActions.suggestCommand(String.format("/%s cg info %s",GiftCodePlugin.getPlugin().getMainCommandAlias(),name)))
                                .build()
                );
            }
            src.sendMessage(builder.build());
            return CommandResult.success();
        }
    }

}
