package net.ginyai.giftcode.storage;

import net.ginyai.giftcode.object.CommandGroup;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

public interface ILogStorage {
    void log(Player player, String code, CommandGroup group);
    boolean isUsed(String code,User user);
    boolean isUsed(String code);
}
