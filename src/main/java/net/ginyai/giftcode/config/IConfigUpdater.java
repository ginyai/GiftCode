package net.ginyai.giftcode.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public interface IConfigUpdater {
    boolean canRead(int version);
    int getVersion();
    CommentedConfigurationNode update(CommentedConfigurationNode node) throws ObjectMappingException;
}
