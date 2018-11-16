package net.ginyai.giftcode.command;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.Messages;
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
import java.util.*;

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
        CommandElement element = help.element;
        if(element.equals(GenericArguments.none())){
            return Text.EMPTY;
        }
        Text.Builder builder = Text.builder();
        builder.append(GiftCodePlugin.getMessage("giftcode.commands.args"));
        scanArg(element, source, builder);
        return builder.toText();
    }

    private void scanArg(CommandElement commandElement,CommandSource source,Text.Builder builder){
        //todo: PermissionCommandElement
        if (commandElement == null){
            return;
        }
        if (commandElement instanceof CommandFlags) {
            try {
                Field field = CommandFlags.class.getDeclaredField("usageFlags");
                field.setAccessible(true);
                Map<?, ?> usageFlags = (Map<?, ?>) field.get(commandElement);
                for (Map.Entry<?, ?> entry : usageFlags.entrySet()) {
                    List<?> availableFlags = (List<?>) entry.getKey();
                    CommandElement childElement = (CommandElement) entry.getValue();
                    List<Object> objects = new ArrayList<>();
                    objects.add("[");
                    Iterator<?> it = availableFlags.iterator();
                    while (it.hasNext()) {
                        String flag = (String) it.next();
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
                    String id = availableFlags.get(0).toString();
                    builder.append(Text.NEW_LINE,
                            Text.of("    "), Messages.adjustLength(Text.of(objects.toArray()), 30),
                            getMessage("flags." + id));
                }
                Field field1 = CommandFlags.class.getDeclaredField("childElement");
                field1.setAccessible(true);
                scanArg((CommandElement) field1.get(commandElement),source,builder);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                GiftCodePlugin.getPlugin().getLogger().error("Failed to parse help for CommandFlags");
            }
        }
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
            builder.append(Text.NEW_LINE,
                    Text.of("    "),Messages.adjustLength(commandElement.getUsage(source),30),
                    getMessage("args."+id));
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
            //parse "help" first
            Object state = args.getState();
            if(args.hasNext() && args.next().equalsIgnoreCase("help") && !args.hasNext()){
                context.putArg("help",true);
                return;
            }
            args.setState(state);
            //normal args
            try {
                element.parse(source, args, context);
                if (args.hasNext()) {
                    throw args.createError(GiftCodePlugin.getPlugin().getMessages().getMessage("giftcode.commands.too-many-args"));
                }
            } catch (ArgumentParseException e) {
                context.putArg("exception", e);
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
            Optional<ArgumentParseException> optionalException = args.getOne("exception");
            Text exceptionText = optionalException.map(ArgumentParseException::getText).orElse(null);
            if (exceptionText!=null||args.hasAny("help")) {
                if(exceptionText!=null){
                    src.sendMessage(exceptionText);
                }
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