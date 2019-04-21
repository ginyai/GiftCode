package net.ginyai.giftcode.storage;

import net.ginyai.giftcode.GiftCodePlugin;
import net.ginyai.giftcode.exception.DataException;
import net.ginyai.giftcode.object.CommandGroup;
import net.ginyai.giftcode.object.GiftCode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SqlCodeStorage implements ICodeStorage {
    private final IConnectionProvider connectionProvider;
    private final String tableName;

    private String GET_BY_CODE = "SELECT * FROM %s WHERE CODE=?";
    private String GET_BY_COMMAND = "SELECT * FROM %s WHERE COMMAND=?";
    private String INSERT_CODE = "INSERT INTO %s (CODE,COMMAND,CONTEXT) VALUES(?,?,?)";
    private String UPDATE_CODE = "UPDATE %s SET COMMAND = ?,CONTEXT = ? WHERE CODE=?";
    private String DELETE_CODE = "DELETE FROM %s WHERE CODE=?";
    private String CREATE_CODE_TABLE = "CREATE TABLE IF NOT EXISTS %s (CODE VARCHAR(255) NOT NULL,COMMAND VARCHAR(255) NOT NULL,CONTEXT VARCHAR(255),PRIMARY KEY(CODE))";


    public SqlCodeStorage(IConnectionProvider connectionProvider, String tableName) {
        this.connectionProvider = connectionProvider;
        this.tableName = tableName;
        CREATE_CODE_TABLE = String.format(CREATE_CODE_TABLE,tableName);
        GET_BY_CODE = String.format(GET_BY_CODE,tableName);
        GET_BY_COMMAND = String.format(GET_BY_COMMAND,tableName);
        INSERT_CODE = String.format(INSERT_CODE,tableName);
        UPDATE_CODE = String.format(UPDATE_CODE,tableName);
        DELETE_CODE = String.format(DELETE_CODE,tableName);
    }

    public String getTableName() {
        return tableName;
    }

    private Connection getConnection() throws SQLException {
        return connectionProvider.getConnection();
    }

    private void createTable() throws SQLException {
        try (Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(CREATE_CODE_TABLE);
            statement.execute();
        }
    }

    @Override
    public void init() throws DataException {
        try {
            createTable();
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }

    @Override
    public boolean hasCode(String code)  throws DataException {
        try (Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(GET_BY_CODE);
            statement.setString(1,code);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }

    @Override
    public Optional<GiftCode> getCode(String code)  throws DataException{
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(GET_BY_CODE)){
            statement.setString(1,code);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                String command = resultSet.getString("COMMAND");
                String context = resultSet.getString("CONTEXT");
                CommandGroup group = GiftCodePlugin.getPlugin().getCommandGroupManager().getCommandGroup(command);
                if(group == null){
                    return Optional.empty();
                }
                return Optional.of(new GiftCode(code,group,context));
            }
        }catch (SQLException e) {
            throw new DataException(e);
        }
        return Optional.empty();
    }

    @Override
    public boolean addCode(GiftCode code)  throws DataException{
        try (Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(INSERT_CODE);
            statement.setString(1,code.getCodeString());
            statement.setString(2,code.getCommandGroup().getName());
            statement.setString(3,code.getContextString());
            statement.execute();
        }catch (SQLException e) {
            throw new DataException(e);
        }
        return true;
    }

    @Override
    public boolean updateCode(GiftCode code)  throws DataException{
        try (Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(UPDATE_CODE);
            statement.setString(1,code.getCommandGroup().getName());
            statement.setString(2,code.getContextString());
            statement.setString(3,code.getCodeString());
            return statement.executeUpdate()>0;
        }catch (SQLException e) {
            throw new DataException(e);
        }
    }

    @Override
    public boolean removeCode(String code)  throws DataException{
        try (Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(DELETE_CODE);
            statement.setString(1,code);
            return statement.executeUpdate()>0;
        }catch (SQLException e) {
            throw new DataException(e);
        }
    }

    @Override
    public Collection<GiftCode> addCode(Collection<GiftCode> codes)  throws DataException{
        List<GiftCode> codeList = new ArrayList<>();
        for(GiftCode code:codes){
            //todo:better check
            if(!hasCode(code.getCodeString())){
                codeList.add(code);
            }
        }
        try (Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(INSERT_CODE);
            for(GiftCode code:codeList){
                statement.setString(1,code.getCodeString());
                statement.setString(2,code.getCommandGroup().getName());
                statement.setString(3,code.getContextString());
                statement.addBatch();
            }
            statement.executeBatch();
        }catch (SQLException e) {
            throw new DataException(e);
        }
        return codeList;
    }

    @Override
    public Collection<String> getCodes(CommandGroup group)  throws DataException{
        try (Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(GET_BY_COMMAND);
            statement.setString(1,group.getName());
            ResultSet resultSet = statement.executeQuery();
            List<String> codes = new ArrayList<>();
            while (resultSet.next()){
                codes.add(resultSet.getString("CODE"));
            }
            return codes;
        }catch (SQLException e) {
            throw new DataException(e);
        }
    }
}
