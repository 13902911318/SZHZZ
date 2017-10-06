package szhzz.sql.database;


import szhzz.Utils.Utilities;

import java.sql.*;
import java.util.Vector;


/**
 * General data Object
 * <p/>
 * <p>Title: INFO2820</p>
 * <p>Description: home work INFO2820</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: </p>
 * <br>
 * TODO TBD
 * <br>
 *
 * @author John
 * @version 1.0
 *          see unittest.QueryTest
 *          <p/>
 *          - GeneralDAO 包装了某个特定 szhzz.sql 查询所产生的数据集。
 *          通过 GeneralDAO 可以把所提取的数据写入 ToProxy
 *          （一个可以记录用户修改编辑数据状态的数据集）。
 *          <br> 使用方法:
 *          -# GeneralDAO 的构建函数是 protected 的，也就是说，
 *          用户不应该用 new GeneralDAO() 直接产生一个 GeneralDAO
 *          实例。而应该向一个已经建立了连接的（opended）Database 实例 db
 *          请求一个特定的 GeneralDAO，例如：
 *          <br> db.getDAO(String szhzz.sql, boolean readOnly)
 *          <p/>
 *          -# 取得 GeneralDAO 实例后，调用 retriev() 方法（可反复使用），
 *          GeneralDAO 将自动把数据写入一个 ToProxy，并会根据数据库对
 *          各字段的定义，自动确定数据类型，进行jdbc/java 数据类型转换，
 *          确定字段值域，以及设置检查数据类型和值域的适当方法等，
 *          都将写入DataStore。
 *          <br>
 *          当用户对 ToProxy 的数据进行修改操作时，
 *          ToProxy 将自动进行类型检查，如果有错误，会提示错误信息。
 *          <p/>
 *          更新数据库时， 我们使用了 loger.getLoger().logToDB(delDs.getUpdateScript());
 *          记录各种更新动作和数据变化的情况。
 *          <p/>
 *          更新失败时，将完全 rollback， 放弃整批更新， loger.getLoger().logToDB 的数据也
 *          不会记录到数据库而是打印到 system.out
 * @see JDBCType
 * @see JDBCTypeValidater
 * <br>
 * -# 调用 getDataStore() 方法，获取一个 ToProxy 结果集
 * see QueryTest   里所有实现的方法
 * <br>
 * <br>
 */
public class GeneralDAO implements DaoInterface {

    public static String lineSep = System.getProperty("line.separator");
    protected DBProperties map;
    protected Database db;
    protected String query;
    protected DatabaseMetaData dbMeta;
    protected Statement stmt;
    protected ResultSetMetaData rsMeta;
    protected ResultSet resultSet;
    boolean readOnly = false;
    DataStore ds = null;
    Vector UpdateListeners = new Vector();
    boolean logDBQuery = false;
    private boolean updateUseScript = true;
    private int updateRows = 0;
    private DataBuffer dataBuffer;

    GeneralDAO() {
    }

    /**
     * Generates a DAO object
     *
     * @param query the SQL query
     * @param db    the database object
     */
    public GeneralDAO(String query, Database db, boolean readOnly) throws SQLException {
        this.readOnly = readOnly;
        this.query = query;
        this.db = db;
        this.map = db.getDBProperties();
        logDBQuery = map.getBooleanProperty("logDB4Query");
        dbMeta = db.getDbMeta();
        stmt = db.getStatement(readOnly);

    }

    public static String getVersion() {
        return "1.0";
    }

    /**
     * addUpdateListener(UpdateListener ul)
     * <br>
     * <br>
     * TODO TBD
     * <br>
     * addUpdateListener 添加一个或多个更新监视器。
     * <p/>
     * 目地：当我们调用 applyUpdates() 时，程序自动检查已更新的记录和字段
     * 然后对已更新的记录进行写数据库的工作，把更新数据反映到数据库，
     * 真正实现数据库的数据更新。
     * <br>但是，有时候，我们需要进行其它的一些
     * 预备工作来保证更新可以实现。例如，更新，添加含有 foren key 的记
     * 录时可能需要更新，添加 master table 的相关记录，或者，当删除具有子
     * 表关联记录的记录时，先删除子表相关的记录等等。
     * <p/>
     * 监视器类 UpdateListener 可以在实施数据库更新之前，进行必要的额外
     * 工作，并可以决定applyUpdates()是否应该继续后续的更新动作。
     * <p/>
     *
     * @param ul
     * @see UpdateListener
     * @see this.applyUpdates()
     */
    public void addUpdateListener(UpdateListener ul) {
        UpdateListeners.remove(ul);
        UpdateListeners.add(ul);
    }

    /**
     * include delete row, update row, new row
     *
     * @param row
     * @return boolean
     * @see UpdateListener
     * see jBgui.Panel_D
     */
    boolean triggerUpdateListeners(DataStore ds, int row, boolean post) {
        for (int i = 0; i < UpdateListeners.size(); i++) {
            UpdateListener ul = (UpdateListener) UpdateListeners.get(i);
            if (!ul.applyUpdate(db, ds, row, post)) return false;
        }
        return true;
    }

    /**
     * Resets internal counters. These counters are used to ensure
     * unique element type names.
     */
    public void close() {
        clearData();
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignored) {

            }
        }
    }

    private void clearData() {
        if (dataBuffer != null) {
            dataBuffer.reset();
        }

        if (null != resultSet)
            try {
                resultSet.close();
                resultSet = null;
                rsMeta = null;
            } catch (SQLException e) {
//                loger.getLoger().errorOut(e);
            }
        if (ds != null) {
            ds.clear();
        }
    }

    /**
     * @return result set for this query.
     * @throws DBException in case of an db error or if query
     *                     is not a selection (i.e. inserts are not allowed).
     */
    ResultSet execute() throws DBException {
        try {
            if (resultSet == null) {
                //resultSet = new CachedRowSetImpl();
                if (!query.trim().toLowerCase().startsWith("select"))
                    throw new DBException("Only select statements are allowed! " +
                            "(" + query + ")", DBException.SQLEXCEPTION);
                try {
//                      Error
//                    if(stmt.isConnect() && db.isOpened()){
//                        stmt = db.getStatement(readOnly);
//                    }
                    if (!db.isOpened()) {
                        db.openDB();
                    }
                    stmt.close();
                    stmt = db.getStatement(readOnly);
                    resultSet = stmt.executeQuery(query);
                    if (logDBQuery) log2Db_(query);
                } catch (SQLException ex) {
                    if (map.propertyHasValue("document", "complete"))
                        throw new DBException("The query:         " + query + "\n"
                                + "could not be executed! Probably "
                                + "you have no permission to access "
                                + "this \n"
                                + "table or you are not connected "
                                + "to this catalog. Change schema or "
                                + "catalog selection!",
                                DBException.UNSUPPORTEDFEATURE);
                    else
                        throw ex;
                }
            }
            return resultSet;
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
    }

    public DataBuffer retrievAsDataBuffer() throws DBException {
        commitDbLog_();
        try {
            clearData();
            execute();
            if (dataBuffer == null) dataBuffer = new DataBuffer();
            rsMeta = getResultSetMetaData();
            int numberOfColumns = rsMeta.getColumnCount();
            dataBuffer.setColumnCount(numberOfColumns);
            while (resultSet.next()) {
                dataBuffer.appendRow();
                for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
                    dataBuffer.addObject(JDBCType.getColumnDataObject(i, resultSet));
                }
            }

        } catch (SQLException e) {
            DBException dbe = new DBException(e);

            markFalseDbLog_();
            if (logDBQuery) log2Db_("SQLException " + dbe.getMessage());

            throw dbe;
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            commitDbLog_();
        }
        return dataBuffer;
    }


    public DataStore retriev() throws DBException {
        commitDbLog_();
        try {
            clearData();
            execute();
            rsMeta = getResultSetMetaData();

            if (ds == null) {
                ds = new DataStore();
                Vector tableName = new Vector();
                int numberOfColumns = rsMeta.getColumnCount();
                for (int i = 1; i <= numberOfColumns; i++) {
                    //String t = rsMeta.getTableName(i);
                    String t = rsMeta.getSchemaName(i);
                    if (!tableName.contains(t)) tableName.add(t);

                    //String dbg = rsMeta.getColumnName(i);
                    ds.setColName(rsMeta.getColumnLabel(i), i - 1);

//                    rsMeta.getColumnName(i);
                    ds.setColTypeName(rsMeta.getColumnTypeName(i), i - 1);
                    ds.setColLength(i - 1, rsMeta.getColumnDisplaySize(i));

                    // Add validater depend on database defination
                    if (!readOnly) JDBCTypeValidater.setValidater(ds, i, rsMeta);
                }
                ds.setName(Utilities.getTableName(query));
                ds.setReadOnly(readOnly);
            }

            ds.clear();
            while (resultSet.next()) {
                ds.addRow(resultSet.getRow());
//                int r = rsMeta.getColumnCount();
                for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
                    ds.addObject(JDBCType.getColumnDataObject(i, resultSet));
                }
            }
            if (logDBQuery) log2Db_("retrieved " + ds.getRowCount() + " rows");
        } catch (SQLException e) {
            DBException dbe = new DBException(e);

            markFalseDbLog_();
            if (logDBQuery) log2Db_("SQLException " + dbe.getMessage());


            throw dbe;
        } finally {
            try {
                if (readOnly && resultSet != null)
                    resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            commitDbLog_();
        }
        return ds;
    }


    /**
     * Access method for the meta data of the result set.
     *
     * @return result set meta data for this query.
     * @throws SQLException if error occurs during access of meta data
     * @throws DBException  in case of another error
     */
    public ResultSetMetaData getResultSetMetaData() throws DBException, SQLException {
        if (rsMeta == null)
            rsMeta = execute().getMetaData();
        return rsMeta;
    }

    /**
     * @return ds
     */
    public DataStore getDataStore() {
        return ds;
    }

    /**
     * @param row
     * @throws DBException
     */
    private void deleteRow(int row) throws DBException {
        try {
            resultSet.absolute(row);
            resultSet.deleteRow();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    private int deleteRows() throws DBException {
        int deleteRows = 0;
        DataStore delDs = ds.getDeleteRows();
        if (null != delDs) {
//            try {
            for (int row = 0; row < delDs.getRowCount(); row++) {
                //  ! triggerUpdateListeners(row) will prevent following update
                if (!triggerUpdateListeners(delDs, row, false)) continue;

                delDs.scrollToRow(row);
                // Log to database
                //log2Db_(delDs.getUpdateScript());

                int delrow = delDs.getJDBCRowNumber();
                deleteRow(delrow);

                if (!triggerUpdateListeners(delDs, row, true)) continue;

                deleteRows++;
            }
//            } catch (SQLException e) {
//                loger.getLoger().logToDB("SQLException! " + e.getSQLState());
//                throw e;
//            } catch (DBException e) {
//                loger.getLoger().logToDB("DBException! " + e.getOriginalException());
//                throw e;
//            }
        }
        return deleteRows;
    }


    public int applyUpdates() throws DBException {
        updateRows = 0;
        if (readOnly) return 0;
        if (updateUseScript) return applyUpdatesSCR();
        // Bellow is not used!


        int updateCol = 0;

        // flash all any posibel unsaved dblog
        // addFrom10JQKA a new log section
        commitDbLog_();

        try {
            db.setAutoCommit(false);
            // First, delete rows
            updateRows += deleteRows();

            for (int row = 0; row < ds.getRowCount(); row++) {
                // Update only rows those need update
                // tableModel.isModified() included modified and new row
                if (ds.isModified(row)) {
                    //  ! triggerUpdateListeners(row) will prevent following update
                    if (!triggerUpdateListeners(ds, row, false)) continue;

                    // UpdateListeners may change the update status
                    if (!ds.isModified(row)) {
                        updateRows++;
                        continue;
                    }

                    // synchroniz Scolling resultSet and tableModel
                    ds.scrollToRow(row);
                    // Log to database
                    //log2Db_(ds.getUpdateScript());

                    if (ds.isNewRow(row)) {
                        resultSet.moveToInsertRow();
                    } else {
                        resultSet.absolute(ds.getJDBCRowNumber());
                    }

                    updateCol = 0;
                    for (int col = 0; col < ds.getColumnCount(); col++) {
                        // Update only columns those  need update
                        if (ds.isModified(row, col)) {
                            resultSet.updateObject(col + 1, ds.getObject(col));
                            updateCol++;
                        } else if (ds.isNewRow(row) && rsMeta.isNullable(col) == rsMeta.columnNoNulls) {
                            // Null value may not be edited,
                            // so we need a Not NULL check here.
                            // do not throw Exception, we may need update flowing rows

                            DBException dbe = new DBException(ds.getColumnName(col) + " can not be NULL!",
                                    DBException.ILLEGALVALUE);

//                            markFalseDbLog_();
//                            log2Db_("DBException! " + dbe.getMessage());
//                            loger.getLoger().showError(dbe);

                            updateCol = 0;   // aband update
                            break;
                        }
                    }
                    if (updateCol > 0) { // Important for Insert rows
                        if (ds.isNewRow(row)) {
                            resultSet.insertRow();
                            updateRows++;
                        } else {
                            resultSet.updateRow();
                            updateRows++;
                        }
                        triggerUpdateListeners(ds, row, true);
                    }
                }
            }
        } catch (SQLException e) {
            // mark status false
            markFalseDbLog_();
            log2Db_("SQLException! " + e.getSQLState());
            updateRows = 0;
            try {
                log2Db_("All Update Rollbacked!");
                db.Rollback();
            } catch (SQLException e1) {
                throw new DBException(e1);
            }
            throw new DBException(e);
        } catch (Exception e) {
            // mark status false
            markFalseDbLog_();
            log2Db_("Exception! " + e.getMessage());
            updateRows = 0;

            // UNEXCEPTID RUNTIM ERROR
            e.printStackTrace();
            throw new DBException(e.getMessage(), DBException.RUNTIMEERROR);
        } finally {
            if (updateRows > 0) {
                try {
                    db.Commit();
                    // resetIndex datastore's status
                    clearUpdate();
                } catch (SQLException e) {
                    updateRows = 0;
                    markFalseDbLog_();
                    log2Db_("SQLException! " + e.getSQLState());
                    throw new DBException(e);
                }
            }
            // Write Log to Database
            commitDbLog_();
        }
        return updateRows;
    }

    private int applyUpdatesSCR() throws DBException {
        int upd = 0;
        commitDbLog_();
        try {
            db.setAutoCommit(false);

            DataStore delDs = ds.getDeleteRows();
            upd = applyUpdatesSCR(delDs);
            updateRows += upd;
            upd = applyUpdatesSCR(ds);
            updateRows += upd;
        } catch (SQLException e) {
            // mark status false
            markFalseDbLog_();
            log2Db_("SQLException! " + e.getSQLState());
            updateRows = 0;
            try {
                log2Db_("All Update Rollbacked!");
                db.Rollback();
            } catch (SQLException e1) {
                throw new DBException(e1);
            }
            throw new DBException(e);
        } catch (Exception e) {
            // mark status false
            markFalseDbLog_();
            log2Db_("Exception! " + e.getMessage());
            updateRows = 0;

            // UNEXCEPTID RUNTIM ERROR
            e.printStackTrace();
            throw new DBException(e.getMessage(), DBException.RUNTIMEERROR);
        } finally {
            if (updateRows > 0) {
                try {
                    db.Commit();
                    // resetIndex datastore's status
                    clearUpdate();
                } catch (SQLException e) {
                    updateRows = 0;
                    markFalseDbLog_();
                    log2Db_("SQLException! " + e.getSQLState());
                    throw new DBException(e);
                }
            }
            // Write Log to Database
            commitDbLog_();
        }
        return updateRows;
    }

    private int applyUpdatesSCR(DataStore updateDs) throws SQLException, DBException {
        String updateSQL;
        if (readOnly) return 0;
        if (null == updateDs) return 0;
        int uprow = 0;

        // flash all any posibel unsaved dblog
        // addFrom10JQKA a new log section


        for (int row = 0; row < updateDs.getRowCount(); row++) {
            if (updateDs.isDeletrow(row)) {
                //  ! triggerUpdateListeners(row) will prevent following update
                if (!triggerUpdateListeners(updateDs, row, false)) continue;

                // synchroniz Scolling resultSet and tableModel
                updateDs.scrollToRow(row);
                updateSQL = updateDs.getUpdateScript();
                // Log to database
                log2Db_(updateSQL);
                uprow += db.executeUpdate(updateSQL);
                triggerUpdateListeners(updateDs, row, true);
            }
            // Update only rows those need update
            // tableModel.isModified() included modified and new row
            else if (updateDs.isModified(row)) {
                //  ! triggerUpdateListeners(row) will prevent following update
                if (!triggerUpdateListeners(updateDs, row, false)) continue;

                // UpdateListeners may change the update status
                if (!updateDs.isModified(row)) {
                    uprow++;
                    continue;
                }

                // synchroniz Scolling resultSet and tableModel
                updateDs.scrollToRow(row);
                updateSQL = updateDs.getUpdateScript();
                // Log to database
                log2Db_(updateSQL);
                // Update only columns those  need update
                for (int col = 0; col < updateDs.getColumnCount(); col++) {
                    // new and not modified col is a NULL col
//                    if (!ds.isModified(row, col) &&
//                            updateDs.isNewRow(row) &&
//                            rsMeta.isNullable(col) == rsMeta.columnNoNulls) {
//                        // Null value may not be edited,
//                        // so we need a Not NULL check here.
//                        // do not throw Exception, we may need update flowing rows
//                        throw new DBException(updateDs.getColumnName(col) + " can not be NULL!",
//                                DBException.ILLEGALVALUE);
//                    }
                }
                uprow += db.executeUpdate(updateSQL);
                triggerUpdateListeners(updateDs, row, true);
            }
        }
        return uprow;
    }


    public int executeUpdate(String sql) throws DBException {
        int retv = db.executeUpdate(sql);

        updateRows += retv;
        return retv;
    }

    public void clearUpdate() {
        ds.clearUpdate();
    }

    public void setUpdateUseScript(boolean updateUseScript) {
        this.updateUseScript = updateUseScript;
    }


    private void commitDbLog_() {
//        loger.getLoger().commitDBLog();
    }

    private void markFalseDbLog_() {
//        loger.getLoger().markFalseDBLog();
    }

    void log2Db_(String msg) {
//        loger.getLoger().logToDB(msg);
    }

    public void setLog2DB(boolean log2DB) {
        this.logDBQuery = log2DB;
    }
}
