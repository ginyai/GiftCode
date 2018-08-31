package net.ginyai.giftcode.storage;

import com.google.common.reflect.TypeToken;
import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.exception.DataException;
import net.ginyai.giftcode.object.CommandGroup;
import net.ginyai.giftcode.object.GiftCode;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.entity.living.player.Player;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class ConfigFileStorage implements ICodeStorage,ILogStorage {
    private Map<String,CodeEntry> codeMap;
    private Map<String,Map<UUID,LogEntry>> logMap;

    private ConfigurationLoader<? extends ConfigurationNode> loader;
    private ConfigurationNode node;

    public ConfigFileStorage(ConfigurationLoader<? extends ConfigurationNode> loader) throws IOException, ObjectMappingException {
        this.loader = loader;
        this.node = loader.load();
        codeMap = node.getNode("Codes").getValue(new TypeToken<Map<String, CodeEntry>>(){},new HashMap<>());
        logMap = node.getNode("Log").getValue(new TypeToken<Map<String,Map<UUID,LogEntry>>>(){},new TreeMap<>());
    }

    private void save(){
        try {
            node.getNode("Codes").setValue(new TypeToken<Map<String, CodeEntry>>(){}, codeMap);
            node.getNode("Log").setValue(new TypeToken<Map<String,Map<UUID,LogEntry>>>(){},logMap);
            loader.save(node);
        }catch (Exception e){
            throw new DataException(e);
        }
    }

    @Override
    public boolean hasCode(String code) throws DataException {
        return codeMap.containsKey(code);
    }

    @Override
    public Optional<GiftCode> getCode(String code) throws DataException {
        CodeEntry codeEntry = codeMap.get(code);
        if(codeEntry == null){
            return Optional.empty();
        }else {
            return Optional.ofNullable(toCode(code, codeEntry));
        }
    }

    @Override
    public boolean addCode(GiftCode code) throws DataException {
        if(!codeMap.containsKey(code.getCodeString())){
            codeMap.put(code.getCodeString(), toCodeEntry(code));
            save();
            return true;
        }else {
            return false;
        }
    }

    @Override
    public boolean updateCode(GiftCode code) throws DataException {
        if(codeMap.containsKey(code.getCodeString())){
            codeMap.put(code.getCodeString(), toCodeEntry(code));
            save();
            return true;
        }else {
            return false;
        }
    }

    @Override
    public boolean removeCode(String code) throws DataException {
        if(codeMap.containsKey(code)){
            codeMap.remove(code);
            save();
            return true;
        }else {
            return false;
        }
    }

    @Override
    public Collection<GiftCode> addCode(Collection<GiftCode> codes) throws DataException {
        List<GiftCode> list = new ArrayList<>();
        for(GiftCode code:codes){
            if(!codeMap.containsKey(code.getCodeString())){
                codeMap.put(code.getCodeString(), toCodeEntry(code));
                list.add(code);
            }
        }
        save();
        return list;
    }

    @Override
    public Collection<String> getCodes(CommandGroup group) throws DataException {
        List<String> list = new ArrayList<>();
        for(Map.Entry<String,CodeEntry> entry:codeMap.entrySet()){
            if(entry.getValue().group.equals(group.getName())){
                list.add(entry.getKey());
            }
        }
        return list;
    }

    @Override
    public void log(Player player, String code, CommandGroup group) throws DataException {
        Map<UUID, LogEntry> logEntryMap = logMap.computeIfAbsent(code, k -> new HashMap<>());
        logEntryMap.put(player.getUniqueId(),new LogEntry(group.getName(),System.currentTimeMillis()));
        save();
    }

    @Override
    public boolean isUsed(String code, UUID player) throws DataException {
        Map<UUID, LogEntry> logEntryMap = logMap.get(code);
        if(logEntryMap == null){
            return false;
        }else {
            return logEntryMap.containsKey(player);
        }
    }

    @Override
    public boolean isUsed(String code) throws DataException {
        return logMap.containsKey(code);
    }

    @ConfigSerializable
    private static class CodeEntry {
        @Setting
        String group;
        @Setting
        String context;

        CodeEntry(){}

        CodeEntry(String group, String context){
            this.group = group;
            this.context = context;
        }
    }

    @ConfigSerializable
    private static class LogEntry {
        @Setting
        String group;
        @Setting
        long instant;

        LogEntry(){}

        LogEntry(String group, long instant){
            this.group = group;
            this.instant = instant;
        }
    }
    @Nullable
    private static GiftCode toCode(String codeString,CodeEntry codeEntry){
        CommandGroup group = GiftCodePlugin.getPlugin().getCommandGroupManager().getCommandGroup(codeEntry.group);
        if(group == null){
            return null;
        }
        return new GiftCode(codeString,group, codeEntry.context);
    }

    private static CodeEntry toCodeEntry(GiftCode code){
        return new CodeEntry(code.getCommandGroup().getName(),code.getContextString());
    }
}
