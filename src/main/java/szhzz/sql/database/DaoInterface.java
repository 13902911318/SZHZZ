package szhzz.sql.database;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-7-21
 * Time: 17:10:59
 * To change this template use File | Settings | File Templates.
 */
public interface DaoInterface {
    void addUpdateListener(UpdateListener ul);

    void close();


//    public ToProxy retriev(String szhzz.sql) throws DBException;

    //    public ToProxy retriev(String szhzz.sql, boolean newDataStore) throws DBException;
    public DataStore retriev() throws DBException;
//    public ToProxy retriev(boolean newDataStore)throws DBException;

    public DataBuffer retrievAsDataBuffer() throws DBException;

    ResultSetMetaData getResultSetMetaData() throws DBException, SQLException;

    DataStore getDataStore();

    int applyUpdates() throws DBException;

    int executeUpdate(String sql) throws DBException;

    void clearUpdate();

    void setUpdateUseScript(boolean updateUseScript);

    void setLog2DB(boolean log2DB);
}
