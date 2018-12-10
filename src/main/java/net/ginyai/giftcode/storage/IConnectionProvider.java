package net.ginyai.giftcode.storage;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface IConnectionProvider {
    Connection getConnection() throws SQLException;
}
