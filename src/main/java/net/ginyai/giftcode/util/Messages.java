package net.ginyai.giftcode.util;

import net.ginyai.giftcode.GiftCodePlugin;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextParseException;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public class Messages {
    private static GiftCodePlugin plugin = GiftCodePlugin.getInstance();

    private static Messages instance;

    public static Messages getInstance() throws IOException {
        if(instance == null){
            instance = new Messages();
        }
        return instance;
    }

    private CommentedConfigurationNode node;

    private Messages() throws IOException {
        Path messagePath = GiftCodePlugin.getInstance().getConfigDir().resolve("messages.conf");
        if(!messagePath.toFile().exists()){
            Sponge.getAssetManager().getAsset(plugin,"messages/"+Locale.getDefault().toString()+".conf")
                    .orElse(Sponge.getAssetManager().getAsset(plugin,"messages/zh_CN.conf").get())
                    .copyToFile(messagePath);
        }
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(messagePath).build();
        node = loader.load();
    }

    public String getMessageString(String key){
        return node.getNode((Object[]) key.split("\\.")).getString(key);
    }

    public static Text getText(String key, Object... o){
        //todo:占位符
        try {
            return parseText(getInstance().getMessageString(key));
        } catch (IOException e) {
            plugin.getLogger().error("Failed to init messages util",e);
            return Text.of(key,o);
        }
    }

    public static Text parseText(String input){
        try {
            return TextSerializers.JSON.deserialize(input);
        }catch (TextParseException e){
            return TextSerializers.FORMATTING_CODE.deserializeUnchecked(input);
        }
    }
}
