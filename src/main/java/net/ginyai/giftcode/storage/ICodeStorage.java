package net.ginyai.giftcode.storage;

import net.ginyai.giftcode.object.CommandGroup;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface ICodeStorage {
    boolean hasCode(String code);

    Optional<CommandGroup> useCode(String code);

    boolean addCode(String code,CommandGroup group);
    /**
     * @param codes code-group
     * @return added counts
     */
    int addCode(Map<String,CommandGroup> codes);

    //Set?
    Collection<String> getCodes(CommandGroup group);

    //todo:remove
}
