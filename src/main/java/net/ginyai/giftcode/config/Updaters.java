package net.ginyai.giftcode.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.util.TypeTokens;

import java.util.Collections;

import static net.ginyai.giftcode.GiftCodePlugin.getMessage;

public class Updaters {
    public static final IConfigUpdater V4 = new UpdaterV4();

    private Updaters(){}

    private static String getComment(String key){
        return getMessage("giftcode.config.comment."+key).toPlain();
    }

    private static class UpdaterV4 implements IConfigUpdater{



        @Override
        public boolean canRead(int version) {
            return version>0 && version<4;
        }

        @Override
        public int getVersion() {
            return 4;
        }

        @Override
        public CommentedConfigurationNode update(CommentedConfigurationNode node) throws ObjectMappingException {
            CommentedConfigurationNode out = SimpleCommentedConfigurationNode.root(Config.OPTIONS);
            out.getNode("version").setValue(4).setComment(getComment("version"));
            CommentedConfigurationNode giftcode = out.getNode("giftcode");
            giftcode.getNode("remove","outdated")
                    .setValue(node.getNode("remove_outdated_code").getBoolean(false))
                    .setComment(getComment("remove.outdated"));
            giftcode.getNode("remove","used-up")
                    .setValue(node.getNode("remove_used_up_code").getBoolean(false))
                    .setComment(getComment("remove.used-up"));
            giftcode.getNode("command-alias","use")
                    .setValue(node.getNode("use_command_alias").getList(TypeTokens.STRING_TOKEN, Collections.singletonList("code")))
                    .setComment(getComment("command-alias.use"));
            giftcode.getNode("query","player","min")
                    .setValue(node.getNode("query","player","min").getInt(1000))
                    .setComment(getComment("query.player.min"));
            giftcode.getNode("query","player","max")
                    .setValue(node.getNode("query","player","max").getInt(100000))
                    .setComment(getComment("query.player.max"));
            giftcode.getNode("query","player","punish")
                    .setValue(node.getNode("query","player","punish").getInt(1000))
                    .setComment(getComment("query.player.punish"));
            giftcode.getNode("query","global","min")
                    .setValue(node.getNode("query","global","min").getInt(0))
                    .setComment(getComment("query.global.min"));
            giftcode.getNode("query","global","max")
                    .setValue(node.getNode("query","global","max").getInt(1000))
                    .setComment(getComment("query.global.max"));
            giftcode.getNode("query","global","punish")
                    .setValue(node.getNode("query","global","punish").getInt(10))
                    .setComment(getComment("query.global.punish"));
            giftcode.getNode("storage","database")
                    .setValue(node.getNode("database").getValue())
                    .getNode("type").setValue("sql");
            giftcode.getNode("storage-uasge","code")
                    .setValue("database")
                    .setComment(getComment("storage-uasge.code"));
            giftcode.getNode("storage-uasge","log")
                    .setValue("database")
                    .setComment(getComment("storage-uasge.log"));
            giftcode.getNode("random-char-set")
                    .setValue(node.getNode("random_char_set").getValue())
                    .setComment(getComment("random-char-set"));
            giftcode.getNode("code-formats")
                    .setValue(node.getNode("code_formats").getValue())
                    .setComment(getComment("code-formats"));
            return out;
        }
    }
}
