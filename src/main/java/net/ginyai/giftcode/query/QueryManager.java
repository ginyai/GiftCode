package net.ginyai.giftcode.query;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.TreeMultimap;
import net.ginyai.giftcode.Config;
import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.object.GiftCode;
import net.ginyai.giftcode.storage.ICodeStorage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class QueryManager {
    private GiftCodePlugin plugin = GiftCodePlugin.getPlugin();
    private SpongeExecutorService sync = plugin.getSyncExecutor();
    private SpongeExecutorService async = plugin.getAsyncExecutor();

    //server thread
    private Set<UUID> ongoingUsers = new HashSet<>();

    //server thread
    private TreeMultimap<Long,Query> queryTreeMultimap = TreeMultimap.create(Comparator.naturalOrder(),Comparator.naturalOrder());


    private long intervalMin = plugin.getConfig().getGlobalQueryMin();
    private long interval = intervalMin;
    private long nextTick = 0;

    private int playerQueryMin = plugin.getConfig().getPlayerQueryMin();

    private Cache<UUID,Long> playerPunish = Caffeine.newBuilder()
            .expireAfterWrite(10,TimeUnit.MINUTES)
            .build();

    /**
     * Start a query with the code for the player
     * @param player the player query for
     * @param code the code to check
     * @return false,If there is other query unfinished for that player.
     */
    public boolean query(Player player,String code){
        UUID uuid = player.getUniqueId();
        if(ongoingUsers.contains(uuid)){
            return false;
        }
        long now = System.currentTimeMillis();
        ongoingUsers.add(uuid);
        Query query = new Query(now,uuid,code);
        long time = now + playerPunish.get(uuid,uuid1 -> (long)playerQueryMin);
        queryTreeMultimap.put(time,query);
        return true;
    }

    public void tick(){
        long now = System.currentTimeMillis();
        if(now<nextTick){
            return;
        }
        nextTick = now+interval;
        if(queryTreeMultimap.isEmpty()){
            interval = Math.max(intervalMin,interval-plugin.getConfig().getGlobalQueryPunish());
            return;
        }
        Map.Entry<Long, Query> entry = queryTreeMultimap.entries().iterator().next();
        if(entry.getKey()>now){
            return;
        }
        queryTreeMultimap.remove(entry.getKey(),entry.getValue());
        //
        query(entry.getValue()).thenAcceptAsync(this::applyResult,sync);

    }

    private  CompletableFuture<QueryResult> query(Query query){
        ICodeStorage storage = plugin.getCodeStorage();
        return CompletableFuture.supplyAsync(()->storage.getCode(query.getCode()),async)
                .thenApply(o->o.map(code -> completeResult(code,query))
                        .orElse(new QueryResult(query,null,QueryResult.Result.NOT_FOUND)));
    }

    private QueryResult completeResult(GiftCode giftCode,Query query){
        QueryResult.Result result;
        if(plugin.getLogStorage().isUsed(giftCode.getCodeString(),query.getSource())){
            result = QueryResult.Result.USED;
        }else {
            result = QueryResult.Result.SUCCESS;
        }
        return new QueryResult(query,giftCode,result);
    }

    private void applyResult(QueryResult result){
        UUID uuid = result.getQuery().getSource();
        ongoingUsers.remove(uuid);
        switch (result.getResult()){
            case SUCCESS:
                Sponge.getServer().getPlayer(uuid).ifPresent(player -> result.getGiftCode().process(player));
                break;
            case NOT_FOUND:
                Config config = plugin.getConfig();
                interval = Math.min(config.getGlobalQueryMax(),interval+config.getGlobalQueryPunish());
                long punish = playerPunish.get(uuid,uuid1 -> (long)playerQueryMin) + config.getPlayerQueryPunish();
                playerPunish.put(uuid,Math.min(config.getPlayerQueryMax(),punish));
                Sponge.getServer().getPlayer(uuid).ifPresent(player -> player.sendMessage(GiftCodePlugin.getMessage("giftcode.code.wrong-code")));
                break;
            case USED:
                Sponge.getServer().getPlayer(uuid).ifPresent(player -> player.sendMessage(GiftCodePlugin.getMessage("giftcode.code.used-by-player")));
                break;
            case ERROR:
                //暂时还没有做好
                //todo:提醒
                break;
            default:
                //应该暂时不存在
        }
    }

    public void reload(){
        this.interval = plugin.getConfig().getGlobalQueryMin();
        this.intervalMin = plugin.getConfig().getGlobalQueryMin();
        this.playerPunish.cleanUp();
    }

}
