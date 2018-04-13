package net.ginyai.giftcode.storage;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.object.CommandGroup;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.sql.SqlService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class SqlStorage implements ICodeStorage,ILogStorage {


    private String GET_CODE = "SELECT * FROM %s WHERE CODE=?";
    private String GET_COMMAND = "SELECT * FROM %s WHERE COMMAND=?";
    private String INSERT_CODE = "INSERT INTO %s (CODE,COMMAND,CONTEXT) VALUES(?,?,?)";
    private String DELETE_CODE = "DELETE FROM %s WHERE CODE=?";
    private String CREATE_CODE_TABLE = "CREATE TABLE IF NOT EXISTS %s (CODE VARCHAR(255) NOT NULL,COMMAND VARCHAR(255) NOT NULL,CONTEXT VARCHAR(255),PRIMARY KEY(CODE))";


    private String CREATE_LOG_TABLE = "CREATE TABLE IF NOT EXISTS %s (ID INT NOT NULL AUTO_INCREMENT,PLAYER CHAR(36) NOT NULL,CODE VARCHAR(255) NOT NULL,COMMAND VARCHAR(255) NOT NULL,PRIMARY KEY(ID))";
    private String INSERT_LOG = "INSERT INTO %s (PLAYER,CODE,COMMAND) VALUES(?,?,?)";

    private GiftCodePlugin plugin = GiftCodePlugin.getInstance();
    private String codeTableName;
    private String logTableName;
    private SqlService sqlService;
    private String url;

    int retry = 10;


    public SqlStorage(String url,String prefix){
        this.url = url;
        this.codeTableName = prefix+"codes";
        this.logTableName = prefix+"logs";
        this.sqlService = Sponge.getServiceManager().provide(SqlService.class).get();

        CREATE_CODE_TABLE = String.format(CREATE_CODE_TABLE,codeTableName);
        GET_CODE = String.format(GET_CODE,codeTableName);
        GET_COMMAND = String.format(GET_COMMAND,codeTableName);
        INSERT_CODE = String.format(INSERT_CODE,codeTableName);
        DELETE_CODE = String.format(DELETE_CODE,codeTableName);

        CREATE_LOG_TABLE = String.format(CREATE_LOG_TABLE,logTableName);
        INSERT_LOG = String.format(INSERT_LOG,logTableName);

        createTable();
    }

    private Connection getConnection() throws SQLException{
        return sqlService.getDataSource(plugin,url).getConnection();
    }

    public void createTable(){
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
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(GET_CODE)){
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
    public Optional<CommandGroup> useCode(String code) {
        String command = null;
        int count = 0;
        boolean failed;
        do {
            count++;
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(GET_CODE)){
                statement.setString(1,code);
                ResultSet resultSet = statement.executeQuery();
                if(resultSet.next()){
                    command = resultSet.getString("COMMAND");
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
        count = 0;
        do {
            count++;
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_CODE)){
                statement.setString(1,code);
                statement.execute();
                failed = false;
            }catch (SQLException e) {
                plugin.getLogger().error("Filed to lookup code.Try "+ count+" times.",e);
                failed = true;
            }
        }while (failed && count<retry);
        if(failed){
            return Optional.empty();
        }else {
            return Optional.of(group);
        }
    }

    @Override
    public boolean addCode(String code, CommandGroup group) {
        boolean hasCode = false;
        int count = 0;
        boolean failed;
        do {
            count++;
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(GET_CODE)){
                statement.setString(1,code);
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
                statement.setString(1,code);
                statement.setString(2,group.getName());
                //todo:
                statement.setString(3,null);
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
    public int addCode(Map<String, CommandGroup> codes) {
        int count=0;
        for(Map.Entry<String,CommandGroup> entry:codes.entrySet()){
            if(addCode(entry.getKey(),entry.getValue())){
                count++;
            }
        }
        return count;
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
                statement.execute();
                failed = false;
            }catch (SQLException e) {
                plugin.getLogger().error("Filed to log code use.Try "+ count+" times.",e);
                failed = true;
            }
        }while (failed && count<retry);
    }
}
