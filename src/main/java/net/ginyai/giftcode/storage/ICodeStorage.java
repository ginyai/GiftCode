package net.ginyai.giftcode.storage;

import net.ginyai.giftcode.exception.DataException;
import net.ginyai.giftcode.object.CommandGroup;
import net.ginyai.giftcode.object.GiftCode;

import java.util.Collection;
import java.util.Optional;

public interface ICodeStorage {
    boolean hasCode(String code) throws DataException;

    Optional<GiftCode> getCode(String code) throws DataException;

    boolean addCode(GiftCode code) throws DataException;

    boolean updateCode(GiftCode code) throws DataException;

    boolean removeCode(String code) throws DataException;

    /**
     * @param codes code-group
     * @return added counts
     */
    Collection<GiftCode> addCode(Collection<GiftCode> codes) throws DataException;

    //Set?
    Collection<String> getCodes(CommandGroup group) throws DataException;

}
