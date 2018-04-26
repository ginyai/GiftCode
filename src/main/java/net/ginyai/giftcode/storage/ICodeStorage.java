package net.ginyai.giftcode.storage;

import net.ginyai.giftcode.object.CommandGroup;
import net.ginyai.giftcode.object.GiftCode;

import java.util.Collection;
import java.util.Optional;

public interface ICodeStorage {
    boolean hasCode(String code);

    Optional<GiftCode> getCode(String code);

    boolean addCode(GiftCode code);

    boolean updateCode(GiftCode code);

    boolean removeCode(String code);

    /**
     * @param codes code-group
     * @return added counts
     */
    Collection<GiftCode> addCode(Collection<GiftCode> codes);

    //Set?
    Collection<String> getCodes(CommandGroup group);

}
