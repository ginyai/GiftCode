package net.ginyai.giftcode.object;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.storage.ICodeStorage;
import org.spongepowered.api.entity.living.player.Player;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GiftCode {
    private static GiftCodePlugin plugin = GiftCodePlugin.getPlugin();

    private final String codeString;
    private CommandGroup commandGroup;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer useCount;

    public GiftCode(String codeString,CommandGroup commandGroup){
        this.codeString = codeString;
        this.commandGroup = commandGroup;
    }

    public GiftCode(String codeString,CommandGroup commandGroup,String context){
        this(codeString,commandGroup);
        if(context!=null && !context.isEmpty()){
            for(String subString:context.split(",")){
                String[] s = subString.split("=");
                if(s.length!=2){
                    throw new IllegalArgumentException("Wrong context:"+context);
                }
                switch (s[0]){
                    case "start":
                        startTime = LocalDateTime.parse(s[1]);
                        break;
                    case "end":
                        endTime = LocalDateTime.parse(s[1]);
                        break;
                    case "use":
                        useCount = Integer.valueOf(s[1]);
                        break;
                    default:
                        throw  new IllegalArgumentException("Wrong context:"+context);
                }
            }
        }
    }

    public void process(Player player){
        final ICodeStorage codeStorage = plugin.getCodeStorage();
        if(startTime!=null && LocalDateTime.now().isBefore(startTime)){
            player.sendMessage(plugin.getMessages().getMessage("giftcode.code.before-start"));
            return;
        }
        if(endTime!=null && LocalDateTime.now().isAfter(endTime)){
            player.sendMessage(plugin.getMessages().getMessage("giftcode.code.after-end"));
            if(plugin.getConfig().isRemoveOutdatedCode()){
                plugin.getAsyncExecutor().execute(()->codeStorage.removeCode(codeString));
            }
            return;
        }
        CompletableFuture<Boolean> future;
        if(useCount!=null){
            if(useCount<0){
                future = CompletableFuture.supplyAsync(()->true,plugin.getAsyncExecutor());
            }else if(useCount>=1){
                useCount -= 1;
                future = CompletableFuture.supplyAsync(()->codeStorage.updateCode(this),plugin.getAsyncExecutor());
            }else {
                player.sendMessage(plugin.getMessages().getMessage("giftcode.code.used-up-count"));
                if(plugin.getConfig().isRemoveUsedUpCode()){
                    CompletableFuture.supplyAsync(()->codeStorage.updateCode(this),plugin.getAsyncExecutor());
                }
                return;
            }
        }else {
            future = CompletableFuture.supplyAsync(()->codeStorage.removeCode(codeString),plugin.getAsyncExecutor());
        }
        future.thenAcceptAsync(b->give(b,player),plugin.getSyncExecutor())
                .exceptionally(throwable -> {plugin.getLogger().error("Error",throwable);return null;});
    }

    public void give(boolean succeed,Player player){
        if(succeed){
            commandGroup.process(player);
            plugin.log(player, codeString, commandGroup);
        }
    }

    public String getCodeString() {
        return codeString;
    }

    public CommandGroup getCommandGroup(){
        return commandGroup;
    }

    public Optional<Integer> getUseCount() {
        return Optional.ofNullable(useCount);
    }

    public Optional<LocalDateTime> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    public Optional<LocalDateTime> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    public void setUseCount(Integer useCount) {
        this.useCount = useCount;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getContextString(){
        StringBuilder builder = new StringBuilder();
        if(startTime!=null){
            builder.append("start=");
            builder.append(startTime.toString());
            builder.append(',');
        }
        if(endTime!=null){
            builder.append("end=");
            builder.append(endTime.toString());
            builder.append(',');
        }
        if(useCount!=null){
            builder.append("use=");
            builder.append(useCount.toString());
            builder.append(',');
        }
        if(builder.length()>0){
            builder.substring(0,builder.length()-1);
        }
        return builder.toString();
    }
}
