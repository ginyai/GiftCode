package net.ginyai.giftcode.storage;

import net.ginyai.giftcode.object.CommandGroup;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.UUID;

public interface ILogStorage {
    void log(Player player, String code, CommandGroup group);
    boolean isUsed(String code,UUID player);
    boolean isUsed(String code);
}
