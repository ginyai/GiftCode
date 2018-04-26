package net.ginyai.giftcode.storage;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.object.CommandGroup;
import net.ginyai.giftcode.object.GiftCode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.sql.SqlService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SqlStorage implements ICodeStorage,ILogStorage {


    private String GET_BY_CODE = "SELECT * FROM %s WHERE CODE=?";
    private String GET_BY_COMMAND = "SELECT * FROM %s WHERE COMMAND=?";
    private String INSERT_CODE = "INSERT INTO %s (CODE,COMMAND,CONTEXT) VALUES(?,?,?)";
    private String UPDATE_CODE = "UPDATE %s SET COMMAND = ?,CONTEXT = ? WHERE CODE=?";
    private String DELETE_CODE = "DELETE FROM %s WHERE CODE=?";
    private String CREATE_CODE_TABLE = "CREATE TABLE IF NOT EXISTS %s (CODE VARCHAR(255) NOT NULL,COMMAND VARCHAR(255) NOT NULL,CONTEXT VARCHAR(255),PRIMARY KEY(CODE))";


    private String CREATE_LOG_TABLE = "CREATE TABLE IF NOT EXISTS %s (ID INT NOT NULL AUTO_INCREMENT,PLAYER CHAR(36) NOT NULL,CODE VARCHAR(255) NOT NULL,COMMAND VARCHAR(255) NOT NULL,TIME BIGINT NOT NULL,PRIMARY KEY(ID))";
    private String INSERT_LOG = "INSERT INTO %s (PLAYER,CODE,COMMAND,TIME) VALUES(?,?,?,?)";
    private String LOOK_UP_LOG_BY_CODE = "SELECT * FROM %s WHERE CODE=?";
    private String LOOK_UP_LOG_BY_CODE_AND_PLAYER = "SELECT * FROM %s WHERE CODE=? AND PLAYER=?";

    private GiftCodePlugin plugin = GiftCodePlugin.getInstance();
    private String codeTableName;
    private String logTableName;
    private SqlService sqlService;
    private String url;

    private int retry = 10;


    public SqlStorage(String url,String prefix){
        this.url = url;
        this.codeTableName = prefix+"codes";
        this.logTableName = prefix+"log";
        this.sqlService = Sponge.getServiceManager().provide(SqlService.class).get();

        CREATE_CODE_TABLE = String.format(CREATE_CODE_TABLE,codeTableName);
        GET_BY_CODE = String.format(GET_BY_CODE,codeTableName);
        GET_BY_COMMAND = String.format(GET_BY_COMMAND,codeTableName);
        INSERT_CODE = String.format(INSERT_CODE,codeTableName);
        UPDATE_CODE = String.format(UPDATE_CODE,codeTableName);
        DELETE_CODE = String.format(DELETE_CODE,codeTableName);

        CREATE_LOG_TABLE = String.format(CREATE_LOG_TABLE,logTableName);
        INSERT_LOG = String.format(INSERT_LOG,logTableName);
        LOOK_UP_LOG_BY_CODE = String.format(LOOK_UP_LOG_BY_CODE,logTableName);
        LOOK_UP_LOG_BY_CODE_AND_PLAYER = String.format(LOOK_UP_LOG_BY_CODE_AND_PLAYER,logTableName);

        createTable();
    }

    public String getCodeTableName() {
        return codeTableName;
    }

    public String getLogTableName() {
        return logTableName;
    }

    private Connection getConnection() throws SQLException{
        return sqlService.getDataSource(plugin,url).getConnection();
    }

    private void createTable(){
        int count = 0;
        boolean failed;
        do {
            count++;
            try(Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(CREATE_CODE_TABLE)) {
                statement.execute();
                failed = false;
            }catch (SQLException e) {
                plugin.getLogger().error("Failed to create code table.Try "+ count+" times.",e);
                failed = true;
            }
        }while (failed && count<retry);
        count = 0;
        do {
            count++;
            try(Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(CREATE_LOG_TABLE)) {
                statement.execute();
                failed = false;
            }catch (SQLException e) {
                plugin.getLogger().error("Failed to create log table.Try "+ count+" times.",e);
                failed = true;
            }
        }while (failed && count<retry);
    }

    @Override
    public boolean hasCode(String code) {
        boolean hasCode = false;
        int count = 0;
        boolean failed;
        do {
            count++;
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(GET_BY_CODE)){
                statement.setString(1,code);
                ResultSet resultSet = statement.executeQuery();
                hasCode = resultSet.next();
                failed = false;
            }catch (SQLException e) {
                plugin.getLogger().error("Filed to lookup code.Try "+ count+" times.",e);
                failed = true;
            }
        }while (failed && count<retry);
        return hasCode;
    }

    @Override
    public Optional<GiftCode> getCode(String code) {
        String command = null;
        String context = null;
        int count = 0;
        boolean failed;
        do {
            count++;
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(GET_BY_CODE)){
                statement.setString(1,code);
                ResultSet resultSet = statement.executeQuery();
                if(resultSet.next()){
                    command = resultSet.getString("COMMAND");
                    context = resultSet.getString("CONTEXT");
                }
                failed = false;
            }catch (SQLException e) {
                plugin.getLogger().error("Filed to lookup code.Try "+ count+" times.",e);
                failed = true;
            }
        }while (failed && count<retry);
        if(command == null){
            return Optional.empty();
        }
        CommandGroup group = plugin.getConfig().getCommandGroup(command);
        if(group == null){
            return Optional.empty();
        }
        return Optional.of(new GiftCode(code,group,context));
    }

    @Override
    public boolean addCode(GiftCode code) {
        boolean hasCode = false;
        int count = 0;
        boolean failed;
        do {
            count++;
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(GET_BY_CODE)){
                statement.setString(1,code.getCodeString());
                ResultSet resultSet = statement.executeQuery();
                hasCode = resultSet.next();
                failed = false;
            }catch (SQLException e) {
                plugin.getLogger().error("Filed to lookup code.Try "+ count+" times.",e);
                failed = true;
            }
        }while (failed && count<retry);
        if(hasCode || failed){
            return false;
        }
        count = 0;
        do {
            count++;
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(INSERT_CODE)){
                statement.setString(1,code.getCodeString());
                statement.setString(2,code.getCommandGroup().getName());
                statement.setString(3,code.getContextString());
                statement.execute();
                failed = false;
            }catch (SQLException e) {
                plugin.getLogger().error("Filed to add code.Try "+ count+" times.",e);
                failed = true;
            }
        }while (failed && count<retry);
        return !failed;
    }

    @Override
    public boolean updateCode(GiftCode code) {
        int count = 0;
        boolean failed;
        boolean updated = false;
        do {
            count++;
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE_CODE)){
                statement.setString(1,code.getCommandGroup().getName());
                statement.setString(2,code.getContextString());
                statement.setString(3,code.getCodeString());
                updated = statement.executeUpdate()>0;
                failed = false;
            }catch (SQLException e) {
                plugin.getLogger().error("Filed to update code.Try "+ count+" times.",e);
                failed = true;
            }
        }while (failed && count<retry);
        return updated;
    }

    @Override
    public boolean removeCode(String code) {
        int count = 0;
        boolean failed;
        do {
            count++;
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_CODE)){
                statement.setString(1,code);
                statement.execute();
                failed = false;
            }catch (SQLException e) {
                plugin.getLogger().error("Filed to remove code.Try "+ count+" times.",e);
                failed = true;
            }
        }while (failed && count<retry);
        return !failed;
    }

    @Override
    public Collection<GiftCode> addCode(Collection<GiftCode> codes) {
        return codes.stream().filter(this::addCode).collect(Collectors.toList());
    }

    @Override
    public Collection<String> getCodes(CommandGroup group) {
        List<String> codes = null;
        int count = 0;
        boolean failed;
        do {
            count++;
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(GET_BY_COMMAND)){
                statement.setString(1,group.getName());
                ResultSet resultSet = statement.executeQuery();
                codes = new ArrayList<>();
                while (resultSet.next()){
                    codes.add(resultSet.getString("CODE"));
                }
                failed = false;
            }catch (SQLException e) {
                plugin.getLogger().error("Filed to lookup code.Try "+ count+" times.",e);
                failed = true;
            }
        }while (failed && count<retry);
        return codes;
    }

    @Override
    public void log(Player player, String code, CommandGroup group) {

        int count = 0;
        boolean failed;
        do {
            count++;
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(INSERT_LOG)){
                statement.setString(1,player.getUniqueId().toString());
                statement.setString(2,code);
                statement.setString(3,group.getName());
                statement.setLong(4,System.currentTimeMillis());
                statement.execute();
                failed = false;
            }catch (SQLException e) {
                plugin.getLogger().error("Filed to log code use.Try "+ count+" times.",e);
                failed = true;
            }
        }while (failed && count<retry);
    }

    @Override
    public boolean isUsed(String code,User user) {
        boolean isUsed = false;
        int count = 0;
        boolean failed;
        do {
            count++;
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(LOOK_UP_LOG_BY_CODE_AND_PLAYER)){
                statement.setString(1,code);
                statement.setString(2,user.getUniqueId().toString());
                ResultSet resultSet = statement.executeQuery();
                isUsed = resultSet.next();
                failed = false;
            }catch (SQLException e) {
                plugin.getLogger().error("Filed to lookup log.Try "+ count+" times.",e);
                failed = true;
            }
        }while (failed && count<retry);
        return isUsed;
    }

    @Override
    public boolean isUsed(String code) {
        boolean isUsed = false;
        int count = 0;
        boolean failed;
        do {
            count++;
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(LOOK_UP_LOG_BY_CODE)){
                statement.setString(1,code);
                ResultSet resultSet = statement.executeQuery();
                isUsed = resultSet.next();
                failed = false;
            }catch (SQLException e) {
                plugin.getLogger().error("Filed to lookup log.Try "+ count+" times.",e);
                failed = true;
            }
        }while (failed && count<retry);
        return isUsed;
    }
}
