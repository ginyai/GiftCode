package net.ginyai.giftcode.storage;

import net.ginyai.giftcode.exception.DataException;
import net.ginyai.giftcode.object.CommandGroup;
import org.spongepowered.api.entity.living.player.Player;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SqlLogStorage implements ILogStorage {
    private DataSource dataSource;
    private String tableName;

    private String CREATE_LOG_TABLE = "CREATE TABLE IF NOT EXISTS %s (ID INT NOT NULL AUTO_INCREMENT,PLAYER CHAR(36) NOT NULL,CODE VARCHAR(255) NOT NULL,COMMAND VARCHAR(255) NOT NULL,TIME BIGINT NOT NULL,PRIMARY KEY(ID))";
    private String INSERT_LOG = "INSERT INTO %s (PLAYER,CODE,COMMAND,TIME) VALUES(?,?,?,?)";
    private String LOOK_UP_LOG_BY_CODE = "SELECT * FROM %s WHERE CODE=?";
    private String LOOK_UP_LOG_BY_CODE_AND_PLAYER = "SELECT * FROM %s WHERE CODE=? AND PLAYER=?";

    public SqlLogStorage(DataSource dataSource, String tableName) throws SQLException {
        this.dataSource = dataSource;
        this.tableName = tableName;

        CREATE_LOG_TABLE = String.format(CREATE_LOG_TABLE,tableName);
        INSERT_LOG = String.format(INSERT_LOG,tableName);
        LOOK_UP_LOG_BY_CODE = String.format(LOOK_UP_LOG_BY_CODE,tableName);
        LOOK_UP_LOG_BY_CODE_AND_PLAYER = String.format(LOOK_UP_LOG_BY_CODE_AND_PLAYER,tableName);

        createTable();
    }

    public String getTableName() {
        return tableName;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void createTable() throws SQLException {
        try(Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(CREATE_LOG_TABLE);
            statement.execute();
        }
    }

    @Override
    public void log(Player player, String code, CommandGroup group) throws DataException {
        try (Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(INSERT_LOG);
            statement.setString(1,player.getUniqueId().toString());
            statement.setString(2,code);
            statement.setString(3,group.getName());
            statement.setLong(4,System.currentTimeMillis());
            statement.execute();
        }catch (SQLException e) {
            throw new DataException(e);
        }
    }

    @Override
    public boolean isUsed(String code, UUID player) throws DataException {
        try (Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(LOOK_UP_LOG_BY_CODE_AND_PLAYER);
            statement.setString(1,code);
            statement.setString(2,player.toString());
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }catch (SQLException e) {
            throw new DataException(e);
        }
    }

    @Override
    public boolean isUsed(String code) throws DataException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(LOOK_UP_LOG_BY_CODE)){
            statement.setString(1,code);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }catch (SQLException e) {
            throw new DataException(e);
        }
    }
}
