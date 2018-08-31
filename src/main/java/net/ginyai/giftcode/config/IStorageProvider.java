package net.ginyai.giftcode.config;

import net.ginyai.giftcode.exception.DataException;
import net.ginyai.giftcode.storage.ICodeStorage;
import net.ginyai.giftcode.storage.ILogStorage;

public interface IStorageProvider {
    ICodeStorage getCodeStorage() throws DataException;
    ILogStorage getLogStorage() throws DataException;
}
