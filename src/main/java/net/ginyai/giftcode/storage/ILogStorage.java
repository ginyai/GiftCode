package net.ginyai.giftcode.storage;

import net.ginyai.giftcode.object.CommandGroup;
import org.spongepowered.api.entity.living.player.Player;

public interface ILogStorage {
    void log(Player player, String code, CommandGroup group);
}
