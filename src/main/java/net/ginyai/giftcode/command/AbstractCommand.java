package net.ginyai.giftcode.command;

import net.ginyai.giftcode.GiftCodePlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.*;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractCommand implements ICommand, CommandExecutor {

    protected CommandSpec commandSpec;

    protected Help help;

    protected String name;

    protected String[] alias;

    public AbstractCommand(String name, String... alias) {
        this.name = name;
        this.alias = alias;
    }

    public String getRootPermission() {
        return "giftcode.command." + name;
    }

    protected String getPermission(String s) {
        return getRootPermission() + "." + s;
    }

    protected String getMessageKey(String s) {
        return "giftcode.command." + name + "." + s;
    }

    protected Text getMessage(String s) {
        return GiftCodePlugin.getPlugin().getMessages().getMessage(getMessageKey(s));
    }

    protected Text getMessage(String s, String k1, Object v1) {
        return GiftCodePlugin.getPlugin().getMessages().getMessage(getMessageKey(s), k1, v1);
    }

    protected Text getMessage(String s, String k1, Object v1, String k2, Object v2) {
        return GiftCodePlugin.getPlugin().getMessages().getMessage(getMessageKey(s), k1, v1, k2, v2);
    }

    public Text getDescription() {
        return getMessage("description");
    }

    public Text getExtendedDescription() {
        return getMessage("extended-description");
    }

    public Text getArgHelp(CommandSource source) {
        init();
        Text.Builder builder = Text.builder();
        builder.append(getMessage("giftcode.commands.args"));
        CommandElement element = help.element;
        if(element instanceof CommandFlags){
            try {
                Field field = CommandFlags.class.getDeclaredField("usageFlags");
                field.setAccessible(true);
                Map<List<String>, CommandElement> usageFlags = (Map<List<String>, CommandElement>) field.get(element);
                for(Map.Entry<List<String>,CommandElement> entry:usageFlags.entrySet()){
                    List<String> availableFlags = entry.getKey();
                    CommandElement childElement = entry.getValue();
                    List<Object> objects = new ArrayList<>();
                    objects.add("[");
                    Iterator it = availableFlags.iterator();
                    while(it.hasNext()) {
                        String flag = (String)it.next();
                        objects.add(flag.length() > 1 ? "--" : "-");
                        objects.add(flag);
                        if (it.hasNext()) {
                            objects.add("|");
                        }
                    }
                    Text usage = childElement.getUsage(source);
                    if (usage.toPlain().trim().length() > 0) {
                        objects.add(" ");
                        objects.add(usage);
                    }
                    objects.add("]");
                    objects.add(" ");
                    String id = availableFlags.get(0);
                    builder.append(Text.NEW_LINE,Text.of(objects.toArray()),getMessage("flags."+id));
                }
                Field field1 = CommandFlags.class.getDeclaredField("childElement");
                field1.setAccessible(true);
                element = (CommandElement) field1.get(element);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                GiftCodePlugin.getPlugin().getLogger().error("Failed to parse help for CommandFlags");
            }
        }
        if(element!=null){
            scanArg(element,source,builder);
        }
        return builder.toText();
    }

    private void scanArg(CommandElement commandElement,CommandSource source,Text.Builder builder){
        //todo: PermissionCommandElement
        String id = commandElement.getUntranslatedKey();
        if(id == null){
            Class<? extends CommandElement> clazz = commandElement.getClass();
            try {
                Field field = clazz.getDeclaredField("elements");
                field.setAccessible(true);
                Object elements = field.get(commandElement);
                if(elements instanceof List){
                    for(Object element:(List)elements){
                        if(element instanceof CommandElement){
                            scanArg((CommandElement) element,source,builder);
                        }
                    }
                    return;
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
//            e.printStackTrace();
            }
            try {
                Field field = clazz.getDeclaredField("element");
                field.setAccessible(true);
                Object element = field.get(commandElement);
                if(element instanceof CommandElement){
                    scanArg((CommandElement) element,source,builder);
                    return;
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
//            e.printStackTrace();
            }
        }else {
            builder.append(Text.NEW_LINE,commandElement.getUsage(source),Text.of(" "),getMessage("args."+id));
        }

    }

    public Text getHelpMessage(CommandSource src, CommandContext args) {
        Text.Builder builder = Text.builder();
        StringBuilder alias = new StringBuilder();
        for(String s:getAlias()){
            alias.append(s);
            alias.append(",");
        }
        if(alias.length()>0){
            alias.deleteCharAt(alias.length()-1);
        }
        builder.append(GiftCodePlugin.getPlugin().getMessages()
                .getMessage("giftcode.commands.name",
                        "name",getName(),
                        "alias",alias.toString()
                )
                , Text.NEW_LINE);
        builder.append(getDescription(),Text.NEW_LINE);
        builder.append(GiftCodePlugin.getPlugin().getMessages()
                        .getMessage("giftcode.commands.usage",
                                "usage",getCallable().getUsage(src)
                        )
                , Text.NEW_LINE);
//        builder.append(getExtendedDescription(),Text.NEW_LINE);
        builder.append(getArgHelp(src));
        return builder.build();
    }

    public abstract CommandElement getArgument();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getAlias() {
        return alias;
    }

    @Override
    public CommandSpec getCallable() {
        init();
        return commandSpec;
    }

    private void init(){
        if (commandSpec == null) {
            help = new Help();
            commandSpec = CommandSpec.builder()
                    .permission(getPermission("base"))
                    .description(getDescription())
                    .extendedDescription(getExtendedDescription())
                    .arguments(help)
                    .executor(help)
                    .build();
        }
    }

    @NonnullByDefault
    private class Help extends CommandElement implements CommandExecutor {
        private CommandElement element = getArgument();

        private Help() {
            super(Text.of("help"));
        }

        @Override
        public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
            Object state = args.getState();
            try {
                element.parse(source, args, context);
                if (args.hasNext()) {
                    //avoid too many arguments
                    context.putArg("help", true);
                    while (args.hasNext()) {
                        args.next();
                    }
                }
            } catch (ArgumentParseException e) {
                context.putArg("help", true);
//                args.setState(state);
//                if (args.peek().equalsIgnoreCase("help")) {
//                    context.putArg("help", true);
//                } else {
//                    //temp catch parse exception here
//                    context.putArg("help", true);
//                    //throw e;
//                }
            }
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) {
            //do nothing here
            return null;
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            try {
                if (!args.hasNext() || "help".startsWith(args.peek().toLowerCase())) {
                    List<String> stringList = new ArrayList<>();
                    stringList.add("help");
                    stringList.addAll(element.complete(src, args, context));
                    return stringList;
                } else {
                    return element.complete(src, args, context);
                }
            } catch (ArgumentParseException e) {
                e.printStackTrace();
                return element.complete(src, args, context);
            }
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (args.hasAny("help")) {
                src.sendMessage(getHelpMessage(src, args));
                return CommandResult.success();
            } else {
                return AbstractCommand.this.execute(src, args);
            }
        }

        @Override
        public Text getUsage(CommandSource src) {
            Text usage = element.getUsage(src);
            if (usage.isEmpty()) {
                return Text.of("[help]");
            } else {
                return Text.of(usage, "|help");
            }
        }
    }

}