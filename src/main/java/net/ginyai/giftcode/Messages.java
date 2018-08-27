package net.ginyai.giftcode;

import com.google.common.collect.ImmutableMap;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.serializer.TextParseException;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess")
public class Messages {
    private static GiftCodePlugin plugin = GiftCodePlugin.getPlugin();

    private CommentedConfigurationNode node;
    private CommentedConfigurationNode fallback;

    public Messages(){ }

    private Map<String, TextTemplate> cache = new ConcurrentHashMap<>();

    public void reload() throws IOException {
        Path messagePath = GiftCodePlugin.getPlugin().getConfigDir().resolve("messages.conf");
        Asset fallbackAsset = Sponge.getAssetManager().getAsset(plugin, "messages/" + Locale.getDefault().toString() + ".conf")
                .orElse(Sponge.getAssetManager().getAsset(plugin, "messages/zh_CN.conf").get());
        fallbackAsset.copyToFile(messagePath,false);
        node = HoconConfigurationLoader.builder().setPath(messagePath).build().load();
        fallback = HoconConfigurationLoader.builder().setURL(fallbackAsset.getUrl()).build().load();
        cache.clear();
    }

    public String getMessageString(String key){
        Object[] keys = key.split("\\.");
        return node.getNode(keys).getString(fallback.getNode(keys).getString(key));
    }

    public Text getMessage(String key, Map<String, ?> params){
        if (!cache.containsKey(key)) {
            String rawString = getMessageString(key);
            cache.put(key, parseTextTemplate(rawString, params.keySet()));
        }
        return cache.get(key).apply(params).build();
    }

    public Text getMessage(String key) {
        return getMessage(key, Collections.emptyMap());
    }

    public Text getMessage(String key, String k1, Object v1) {
        return getMessage(key, ImmutableMap.of(k1, v1));
    }

    public Text getMessage(String key, String k1, Object v1, String k2, Object v2) {
        return getMessage(key, ImmutableMap.of(k1, v1, k2, v2));
    }

    public static TextTemplate parseTextTemplate(String origin, Set<String> keySet) {
        if(keySet.isEmpty()){
            return TextTemplate.of(parseText(origin));
        }
        List<Object> objects = new ArrayList<>();
        String[] subStrings = origin.split("\\{");
        for (int i = 0; i < subStrings.length; i++) {
            String subString = subStrings[i];
            if (subString.isEmpty()) {
                continue;
            }
            if (i == 0) {
                objects.add(parseFormatText(subString));
                continue;
            }
            String[] muSub = subString.split("}");
            if (muSub.length == 1 && subString.endsWith("}") && keySet.contains(muSub[0])) {
                objects.add(TextTemplate.arg(muSub[0]));
            } else if (muSub.length > 1 && keySet.contains(muSub[0])) {
                objects.add(TextTemplate.arg(muSub[0]));
                StringBuilder left = new StringBuilder(muSub[1]);
                for (int j = 2; j < muSub.length; j++) {
                    left.append("}");
                    left.append(muSub[j]);
                }
                if (subString.endsWith("}")) {
                    left.append("}");
                }
                objects.add(parseFormatText(left.toString()));
            } else {
                objects.add(parseFormatText("{" + subString));
            }
        }
        return TextTemplate.of(objects.toArray());
    }

    public static Text parseFormatText(String in) {
        return TextSerializers.FORMATTING_CODE.deserializeUnchecked(in);
    }

    public static Text parseText(String input){
        try {
            return TextSerializers.JSON.deserialize(input);
        }catch (TextParseException e){
            return TextSerializers.FORMATTING_CODE.deserializeUnchecked(input);
        }
    }

    public static Text adjustLength(Text text,int length){
        int spaces = length - text.toPlain().length();
        if(spaces<=0){
            return text;
        }else {
            return Text.of(text,String.format("%"+spaces+"s",""));
        }
    }
}
