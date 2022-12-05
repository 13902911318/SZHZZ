package szhzz.sql.database;


import szhzz.sql.jdbcpool.ConnectionManager;
import szhzz.App.AppManager;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;
import szhzz.Utils.Utilities;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

/**
 * Wraper of Connection
 * <p/>
 * todo TBD or TBM
 * <p/>
 * Database 是 Connection (JDBC) 的包装类，提供对 RDB MS 数据库服务器的连接管理
 * 每个Database 实例只提供一个数据库连接 (Connection)。以方便应用程序在多个不同
 * 位置使用数据库，同时又使用尽量少的数据库连接个数。
 * - 如何建立一个 Database
 * -# Database 使用 DBProperties map 作为初始化参数，
 * -# DBProperties 类可以使用一个属性文件读入初始化参数
 * Database 需要的初始化参数有：
 * -# driverClass=oracle.jdbc.driver.OracleDriver
 * -# dbURL=jdbc:oracle:thin:@winxp.cathay:1521:XE
 * -# user=john
 * -# password=tiger
 * -# dbTraceOn=true
 * -# dbTraceToFile=sqlTrace.log
 * - 使用 Database
 * -# 使用 Properties 实例化一个Database： Database(DBProperties map)
 * -# 建立数据库连接 openDB()， 可以用 isOpend() 确认数据库已经连接
 *
 * @see GeneralDAO
 * -# 请求一个 Statement 类型的实例，反复多次执行sql语句 :
 * getStatement(boolean readOnly) 获得一个可更新或不可更新的 Statement
 * -# 使用 Statement
 * -# 如果请求一个可更新的 Statement，在执行 szhzz.sql 查询语句中不可使用包含 * 的格式如:
 * <br>             select * from studen
 * <br>             而必须使用指定字段名称的格式:
 * <br>             select name, id, age from studen
 * -# 请求一个 GeneralDAO 类型的实例
 * <br>      public GeneralDAO getDAO(String szhzz.sql, boolean readOnly)
 * -# 如果请求一个可更新的 GeneralDAO，在执行 szhzz.sql 查询语句中不可使用包含 * 的格式如:
 * <br>        select * from studen
 * <br>        而必须使用指定字段名称的格式
 * <br>         select name, id, age from studen
 * -# 用 dynamicSQL(String szhzz.sql) 动态执行 szhzz.sql 语句
 * -# 在所有程序已经执行完毕后调用 close()
 * 调用 close() 后， 该 Database 与数据库的连接就断开了，因此，与它相关的所有
 * Statement，GeneralDAO 以及 dynamicSQL(String szhzz.sql) 等都不再有效。
 * 为了及时释放资源，同时又不影响程序其它部分继续使用 Database 的服务，应该尽量释放最
 * 底层的资源。
 * <br>     例如： Database 产生 Statement 产生 ResultSet 产生 DataStore，
 * <br>     即 Database->Statement->ResultSet->DataStore,
 * <br>     因此关闭的顺序应该是
 * <br>     DataStore， ResultSet， Statement， Database
 * <br>
 * @see GeneralDAO
 */
public class Database {
    public final static String DRIVER = "driverClass";
    public final static String DBURL = "dbURL";
    public final static String USER = "user";
    public final static String PASSWORD = "password";
    public final static String PORT = "port";
    //    protected static Timer timer = null;  // shared by all pool
    public final static String QUERY = "dbquery";
    public final static String PARAMETERS = "parameters";
    public static String lineSep = System.getProperty("line.separator");
    private static DawLogger logger = DawLogger.getLogger(Database.class);
    private static Vector<Database> opendDB = new Vector<Database>();
    private static JLabel DBConnects = null;
    private static boolean isShutdown = false;
    public boolean usePool = true;
    protected DatabaseMetaData dbMeta;
    protected Connection con;
    protected Statement readOnlyStmt = null;
    protected Statement readWriteStmt = null;
    protected DBProperties map;
    Statement stmtmt = null;
    private SQLException lastError = null;
    private execSqlInback QueryInback = null;
    private String caller = null;
    private timeListener currentTimer = new timeListener();
    private long lastAccessTime;
    private long closeDelay = 0;

    /**
     * Initinalizes the database object with values from properties
     *
     * @param map the property object
     * @see DBProperties
     */
    private Database(DBProperties map, String caller) {
        this.map = map;
        this.caller = caller;
    }

    public static void setConsol(JLabel DBcns) {
        DBConnects = DBcns;
    }

    public static String getVersion() {
        return "1.0";
    }

    public static Database getInstance(DBProperties map, Class caller) {
        return new Database(map, caller.getName());
    }

    public static Vector<Vector> getOpenDBList() {
        Vector list = new Vector();
        for (Database db : opendDB) {
            Vector element = new Vector();
            element.add(db.caller);
            if (db.usePool) {
                element.add(db.get_DBUrl() + " " + db.getConnectionPoolId() + " Close in " + db.IdleTimeLeft() / 1000 + "s");
            } else {
                element.add(db.get_DBUrl());
            }
            list.add(element);
        }
        return list;
    }

    public static int getConnectCount() {
        return opendDB.size();
    }

    public static void Quit() {
        isShutdown = true;
        while (opendDB.size() > 0) {
            opendDB.get(0).close();
        }
        ConnectionManager.shutDown();
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {

            }
        }
    }

    public static void closeResultSet(PreparedStatement rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {

            }
        }
    }

    public static Hashtable<String, Integer> getConnectionInfor() {
        Hashtable<String, Integer> vals = ConnectionManager.getConnectionCount();
        vals.put("DB Conected", opendDB.size());
        return vals;
    }

    public long IdleTimeLeft() {
        if (currentTimer != null) {
            return closeDelay - (System.currentTimeMillis() - lastAccessTime);
        }
        return -1;
    }

    /**
     * Loads the jdbc driver.
     *
     * @param driverClass
     * @throws DBException if specified driver class not found
     * @see
     * @see DBException
     */
    void loadDriverClass(String driverClass) throws DBException {
        try {
            Class.forName(driverClass);

        } catch (ClassNotFoundException e) {
            throw new DBException("Driver class not found! Please specify other driver" + lineSep +
                    " class or check CLASSPATH! Current CLASSPATH is: " + lineSep +
                    System.getProperty("java.class.path"),
                    DBException.CLASSNOTFOUND);
        }
    }

    /**
     * Enables logging if specified in properties. The default queryName for
     * the log file is <em>log.txt</em> (if not specified in properties).
     *
     * @throws DBException if an error occured while opening
     *                     the log file.
     */
    public void prepareLogging() throws DBException {
        String logFile = null;
        try {
            if (map.getBooleanProperty("dbTraceOn")) {
                logFile = map.getProperty("dbTraceToFile");
                if (logFile == null)
                    logFile = "dbTrace.log";
                DriverManager.setLogWriter(new PrintWriter(new FileWriter(logFile)));

            }
        } catch (IOException e) {
            throw new DBException("IO error opening log file " + logFile,
                    DBException.IOEXCEPTION);
        }
    }

    /**
     * This method actually makes the connection to the database. The
     * following steps are performed:
     * <ol>
     * <li>load driver class
     * <li>prepare logging
     * <li>check if user and password properties are supplied
     * <li>if connection exists close it
     * <li>connect to db
     * <li>read meta data
     * <li>create statement object
     * </ol>
     *
     * @throws DBException if something goes wrong
     * @see java.sql.Connection
     */
    public synchronized boolean openDB() throws DBException {

        if (this.isOpened()) {
            cancelTimer();
            return true;
        }
        try {
            loadDriverClass(map.getProperty(DRIVER).trim());
            prepareLogging();
            Properties p = new Properties();


            String user = map.getProperty(USER).trim();
            if (user != null && user.length() > 0) {
                p.put("user", user);
            }

            String password = map.getProperty(PASSWORD).trim();
            if (password != null) {
                p.put("password", password);
            }
            String params = map.getProperty(PARAMETERS);
            if (params != null) {
                StringTokenizer tokenizer = new StringTokenizer(params, ",");
                int equals;
                String token, name, value;

                while (tokenizer.hasMoreTokens()) {
                    token = tokenizer.nextToken();
                    if ((equals = token.indexOf('=')) > -1) {
                        name = token.substring(0, equals).trim();
                        value = token.substring(equals + 1).trim();
                        p.put(name, value);
                    }
                }
            }

            if (con != null && !con.isClosed())
                con.close();

//            String deg = map.getProperty(DBURL);


            con = null;
            String isUsePool = map.getProperty("usePool");
            usePool = (isUsePool != null && (isUsePool.equalsIgnoreCase("true") || isUsePool.equalsIgnoreCase("yes")));
            if (usePool) {
                ConnectionManager.setDebug(false);
                try {
                    con = ConnectionManager.getConnection(map.getProperty(DBURL), map.getProperty(USER), map.getProperty(PASSWORD));
                } catch (Exception e) {
                    logger.error(e);
                }
            }
            if (con == null) {
                con = DriverManager.getConnection(map.getProperty(DBURL), p);
            }

            dbMeta = con.getMetaData();
            // this.dynamicSQL("SET NAMES gbk;");
        } catch (SQLException e) {
            try {
                if (con != null && !con.isClosed())
                    con.close();
                con = null;
            } catch (SQLException ie) {
                System.out.println("This should never happen! Please make a bug report!");
                //e.printStackTrace();
                DBException d = new DBException(e);
                //logger.error(d);
                throw (d);
            }
        } finally {
            if (isOpened()) {
                if (!opendDB.contains(this)) opendDB.add(this);
                if (DBConnects != null) DBConnects.setText("DB=" + opendDB.size());
            }
        }
        return this.isOpened();
    }

    /**
     * Closes the connection (if not already closed) to the database.
     */

    public synchronized void close() {
        cancelTimer();
        try {
            //loger.getLoger().rollbackDBLog();
            if (con != null && !con.isClosed()) {
                con.close();
                con = null;
                stmtmt = null;
            }
        } catch (SQLException e) {

        } finally {
            opendDB.remove(this);
            if (DBConnects != null) DBConnects.setText("DB=" + opendDB.size());
        }
    }

    public synchronized void close(int seconds) {
        //cancelTimer(); 不需， getTimeLitener() 会调用 cancelTimer()
        if (isOpened()) {
            if (!currentTimer.isWaiting()) {
                closeDelay = seconds * 1000;
                lastAccessTime = System.currentTimeMillis();
                currentTimer.setCircleTime(closeDelay);
            } else {
                lastAccessTime = System.currentTimeMillis();
                closeDelay = seconds * 1000;
            }
        }
    }

    public boolean isOpened() {
        if (con != null) {
            try {
                return !con.isClosed();
            } catch (SQLException e) {

            }
        }
        return false;
    }

    /**
     * Access method for meta data
     *
     * @return database meta data
     */
    public DatabaseMetaData getDbMeta() {
        return dbMeta;
    }

    /**
     * Access method for statement object
     *
     * @return statement object
     * @see java.sql.Statement
     */
    public Statement getStatement(boolean readOnly) throws SQLException {
        checkConnection();
        if (readOnly) {
//            if (null == readOnlyStmt)
//                readOnlyStmt = con.createStatement();
//            return readOnlyStmt;
            return con.createStatement();
        } else {
//            if (null == readWriteStmt)
//                readWriteStmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
//            return readWriteStmt;
            return con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        }
    }

    public ResultSet getStreamedResultSet(String query) throws SQLException, DBException {
        checkConnection();
        if (!query.trim().toLowerCase().startsWith("select"))
            throw new DBException("Only select statements are allowed! " +
                    "(" + query + ")", DBException.SQLEXCEPTION);

        if (null != readOnlyStmt) readOnlyStmt.close();

        readOnlyStmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
        readOnlyStmt.setFetchSize(Integer.MIN_VALUE);
        ResultSet resultSet = null;
        try {
            try {
                resultSet = readOnlyStmt.executeQuery(query);

//                    enableStreamingResults()
//                   logger.info(query);
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
            return resultSet;
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
    }

    public PreparedStatement getPreparedStatement(String sql) throws SQLException {
        checkConnection();
        return con.prepareStatement(sql);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        checkConnection();
        con.setAutoCommit(autoCommit);
    }

    public void Commit() throws SQLException {
        checkConnection();
        //if (!map.propertyHasValue("Commit", "Auto Commit"))
        con.commit();
    }

    public void Rollback() throws SQLException {
        con.rollback();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        checkConnection();
        return con.prepareStatement(sql);
    }

    /**
     * get a DBProperties
     *
     * @return DBProperties
     */
    public DBProperties getDBProperties() {
        return this.map;
    }

    /**
     * get a new GeneralDAO instance
     * <p/>
     * <p/>
     * - To get a  GeneralDAO instance
     * -# public GeneralDAO getDAO(String szhzz.sql, boolean readOnly)
     * -# if we want use GeneralDAO(szhzz.sql, false)  for a Updatable Query，
     * can not use a "szhzz.sql" with "*"  format, ie.：
     * <p/>
     * <br>      select * from studen
     * <br>      instead of using specific column names format：
     * <br>      select name, id, age from studen
     *
     * @param sql
     * @param readOnly
     * @return
     * @throws DBException
     */
    public DaoInterface getDAO(String sql, boolean readOnly) throws DBException {
        DaoInterface dao = null;
        try {
            checkConnection();
            dao = new GeneralDAO(sql, this, readOnly);
        } catch (SQLException e) {
            throw new DBException(e);
        }
        return dao;
    }

    public DaoInterface getStreamedDAO(String sql) throws DBException {
        DaoInterface dao = null;
        try {
            checkConnection();
            dao = new PagedDAO(sql, this, true);
        } catch (SQLException e) {
            throw new DBException(e);
        }
        return dao;
    }

    /**
     * @param sql
     * @return
     * @throws DBException
     * @see java.sql.ResultSet
     */
    public ResultSet dynamicSQL(String sql) throws DBException {
        // stmt.setFetchSize(Integer.MIN_VALUE);
        ResultSet resultSet = null;

        try {
            checkConnection();
            //
            if (stmtmt != null) {
                stmtmt.close();
            }
            stmtmt = con.createStatement();

            resultSet = stmtmt.executeQuery(sql);
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
        // Caller remenbe to close resultSet
        return resultSet;
    }

    public ResultSet dynamicSQL(String sql, boolean shareSTM) throws Exception {
        // stmt.setFetchSize(Integer.MIN_VALUE);
        ResultSet resultSet = null;
        Statement st = null;
//        try {
        checkConnection();
        if (shareSTM) {
            if (stmtmt != null) {
                stmtmt.close();
            }
            stmtmt = con.createStatement();
            st = stmtmt;
        } else {
            st = con.createStatement();
        }
        resultSet = st.executeQuery(sql);
//        } catch (SQLException ex) {
//            throw new DBException("The query:         " + szhzz.sql + "\n"
//                    + "could not be executed! Probably "
//                    + "you have no permission to access "
//                    + "this \n"
//                    + "table or you are not connected "
//                    + "to this catalog. Change schema or "
//                    + "catalog selection!",
//                    DBException.UNSUPPORTEDFEATURE);
//        }
        // Caller remenbe to close resultSet
        return resultSet;
    }

    public String startTransaction() throws SQLException {
//        con.commit();
        checkConnection();

        con.setAutoCommit(false);
        String sql = "START ";
        sql += "TRANSACTION";
        return con.nativeSQL(sql);
//        con.nativeSQL("begin");
    }

    public int executeUpdate(String sql) throws DBException {
        int rows = 0;

        try {
            checkConnection();

            if (stmtmt != null) {
                stmtmt.close();
            }
            stmtmt = con.createStatement();

            rows = stmtmt.executeUpdate(sql);
            return rows;
        } catch (SQLException ex) {
            //lastError = ex;
            Utilities.playSound("/resources/AlertSound/alert.wav");
            throw new DBException(ex);
//            throw new DBException("The query: " + szhzz.sql + "\n"
//                    + "could not be executed! Probably "
//                    + "you have no permission to access "
//                    + "this \n"
//                    + "table or you are not connected "
//                    + "to this catalog. Change schema or "
//                    + "catalog selection!",
//                    DBException.UNSUPPORTEDFEATURE);

        } finally {
            try {
                if (stmtmt != null) {
                    stmtmt.close();
                    stmtmt = null;
                }
            } catch (SQLException e) {
                logger.error(e);
            }
        }
        // Caller remenbe to close resultSet
    }


//    public Hashtable<String,LinkedList<String>> getTableColumns(String getTableName) throws SQLException {
//        LinkedList<String> columns = new LinkedList<String>();
//        LinkedList<String> Types = new LinkedList<String>();
//        Hashtable<String,LinkedList<String>> colInf = new Hashtable<String,LinkedList<String>>();
//        colInf.put("Names", columns);
//        colInf.put("Types", Types);
//
//        ResultSet resultSet = dbMeta.getColumns(null, null, getTableName, null);
//        while (resultSet.CheckData()) {
//            String name = resultSet.getDataString("COLUMN_NAME");
//            columns.add(name);
//            String type = resultSet.getDataString("TYPE_NAME");
//            Types.add(type);
////            int size = resultSet.getInt("COLUMN_SIZE");
//        }
//        return colInf;
//    }

    public int getErrorCode() {
        if (lastError != null) lastError.getErrorCode();
        return 0;
    }

    public boolean hasView(String vName) {
        ResultSet rs = null;
        String sql = "SELECT TABLE_NAME FROM information_schema.VIEWS " +
                " where TABLE_NAME = '" + vName + "'";
        try {
            rs = dynamicSQL(sql);
            if (rs.next()) {
                String name = null;
                name = rs.getString(1);
                return (name.equalsIgnoreCase(vName));
            }
        } catch (DBException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResultSet(rs);
        }
        return false;
    }

    public boolean hasTable(String tName) {
        String schma = null;
        String table = tName;
        ResultSet rs = null;

        int index = tName.indexOf(".");
        if (index > 0) {
            schma = tName.substring(0, index);
            table = tName.substring(index + 1);
        }
        String sql = "SELECT TABLE_NAME FROM information_schema.`TABLES` " +
                " where TABLE_NAME = '" + table + "'";
        if (schma != null) {
            sql += " and TABLE_SCHEMA = '" + schma + "'";
        }
        try {
            rs = dynamicSQL(sql);
            if (rs.next()) {
                String name = null;
                name = rs.getString(1);
                return (name.equalsIgnoreCase(table));
            }
        } catch (DBException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResultSet(rs);
        }
        return false;
    }

    public boolean hasTable_(String tName) {
        boolean retval = false;
//        Vector queries = new Vector();
        String types[] = {"TABLE"};


        ResultSet rs = null;
        try {
            rs = dbMeta.getTables(null, "", tName, types); //"%"
            if (rs == null) {
//                loger.getLoger().showError(
//                        new DBException("Your driver does not support the " +
//                                "method getTables. ", DBException.SQLEXCEPTION));
            }
            while (rs.next()) {
                String name = rs.getString(3);
                if (name.equalsIgnoreCase(tName)) return true;
            }
        } catch (SQLException e) {
//            loger.getLoger().showError(new DBException(e));
        } finally {
            closeResultSet(rs);
        }
        return retval;
    }

    public LinkedList<String> getTables() {
        String types[] = {"TABLE"};
        LinkedList<String> tables = new LinkedList<String>();


        ResultSet rs = null;
        try {
            checkConnection();
            //rs = dbMeta.getTables("%", "%", "%", types);
            rs = dbMeta.getTables(null, "", "%", types);
            if (rs != null) {
                while (rs.next()) {
                    String name = rs.getString(3);
                    tables.add(name);
                }
            } else {
//                loger.getLoger().showError(
//                        new DBException("Your driver does not support the " +
//                                "method getTables. ", DBException.SQLEXCEPTION));
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            closeResultSet(rs);
        }
        return tables;
    }

    public boolean tableHasColumn(String tableName, String col) {
        try {
            LinkedList<String> cols = getTableColumns(tableName);
            if (cols != null) {
                return cols.contains(col);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return false;
    }

    public LinkedList<String> getTableColumns(String tableName) throws SQLException {
        LinkedList<String> columns = new LinkedList<String>();
        ResultSet resultSet = null;
        checkConnection();
        try {
            resultSet = dbMeta.getColumns(null, null, tableName, null);
            while (resultSet.next()) {
                String name = resultSet.getString("COLUMN_NAME");
                columns.add(name);
//            String type = resultSet.getDataString("TYPE_NAME");
//            int size = resultSet.getInt("COLUMN_SIZE");
            }
        } finally {
            Database.closeResultSet(resultSet);
        }
        return columns;
    }

    public LinkedList<String> getTableColumnTypes(String tableName) throws SQLException {
        LinkedList<String> types = new LinkedList<String>();
        ResultSet resultSet = null;
        checkConnection();
        try {
            resultSet = dbMeta.getColumns(null, null, tableName, null);
            while (resultSet.next()) {
                String type = resultSet.getString("TYPE_NAME");
                types.add(type.toLowerCase());
            }
        } finally {
            Database.closeResultSet(resultSet);
        }

        return types;
    }

    public String getTableComment(String tableName) {
        ResultSet rs = null;


        String comment = "";
        String sql = "SELECT TABLE_COMMENT FROM information_schema.TABLES " +
                " WHERE TABLE_NAME = '" + tableName + "'";
        try {
            checkConnection();
            rs = dynamicSQL(sql);
            if (rs.next()) {
                comment = rs.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResultSet(rs);
        }
        return comment;
    }

    public LinkedList<String> getPrimaryKeys(String tableName) throws SQLException {
        LinkedList<String> types = new LinkedList<String>();
        ResultSet resultSet = null;
        checkConnection();
        try {
            resultSet = dbMeta.getPrimaryKeys(null, null, tableName);
            while (resultSet.next()) {
                String type = resultSet.getString("COLUMN_NAME");
                types.add(type);
            }
        } finally {
            Database.closeResultSet(resultSet);
        }

        return types;
    }

    public String getConnectionPoolId() {
        if (usePool)
            return ConnectionManager.getId(con);

        return "";
    }

    public String get_DBUrl() {

        return map.getProperty(DBURL);
    }

    public String getHost() {
        return map.getProperty("host");
    }

    public String getPort() {
        return map.getProperty("tcpPort");
    }

    private Database Parent() {
        return this;
    }

    private void checkConnection() throws SQLException {
        cancelTimer();
        try {
            openDB();
        } catch (Exception e) {
            logger.error(e);
        }
        if (!this.isOpened()) {
            throw new SQLException(caller, " connection is closed", DBException.RUNTIMEERROR);
        }
    }

    public void executeInback(DatabaseEvent lisener) throws InterruptedException {
        if (QueryInback == null) {
            QueryInback = new execSqlInback();
        }
        QueryInback.executeInback(lisener);
    }

    public boolean executeScriptFile(String file) throws DBException {
        boolean success = false;

        String sqls = Utilities.File2String(file);
        StringTokenizer tok = new StringTokenizer(sqls, ";");
        while (tok.hasMoreTokens()) {
            String sql = tok.nextToken();
            if (sql.trim().length() > 5) {
                executeUpdate(sql);
            }
        }
        success = true;
        return success;
    }

    /**
     * incase any unexpect error stoped the program,
     * Automaticlly force close the connection
     * log any potential error
     *
     * @throws Throwable
     */
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    void cancelTimer() {
        if (isOpened() && closeDelay > 0 && currentTimer.isWaiting()) {
            lastAccessTime = System.currentTimeMillis();
        }
//        if (currentTimer != null && currentTimer.isWaiting()) {
//            currentTimer.stopTimer();
//        }
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    private class execSqlInback implements Runnable {
        String sql = null;
        DatabaseEvent lisener = null;
        String ID = null;
        Exception ex = null;
        ResultSet rs = null;
        Object object = null;

        public void executeInback(DatabaseEvent lisener) throws InterruptedException {
            this.lisener = lisener;
            AppManager.executeInBack(this);
        }

        public void run() {
            ex = null;
            rs = null;
            int rowcount = 0;
            try {
                // inform lisener
                if (lisener.TriggerEvent(Parent())) {
                    checkConnection();
                    this.sql = lisener.getSQL();

                    if (sql != null) {
                        if (lisener.getExcuteType() == DatabaseEvent.QUERY) {
                            rs = dynamicSQL(sql);
                        } else if (lisener.getExcuteType() == DatabaseEvent.UPDATE) {
                            rowcount = executeUpdate(sql);
                        }
                    }
                }
                if (lisener != null) lisener.TaskFinished(Parent(), rs, rowcount, ex);

            } catch (Exception e) {
                ex = e;
            } finally {
                closeResultSet(rs);
            }

        }
    }

    private class dumLitener extends TimerTask {
        public void run() {
        }
    }

    /**
     * 在连接包装内部的时间控制任务
     */
    protected class timeListener extends CircleTimer {
//    protected class timeLitener extends TimerTask {

        @Override
        public void execTask() {
            long timeLeft = closeDelay - (System.currentTimeMillis() - lastAccessTime);
            if (timeLeft > 0) {
                closeDelay = timeLeft;
                lastAccessTime = System.currentTimeMillis();
                setCircleTime(closeDelay);
            } else {
                closeDelay = 0;
                close();
            }
        }


//        /**
//         * 该方法将由Timer触发
//         */
//        public void run() {
//            try {
//
//                long timeLeft = (System.currentTimeMillis() - lastAccessTime);
//                // 在指定时间内，曾经操作过数据库
//                if (!isShutdown && timeLeft < closeDelay) {
//                    timer.schedule(getTimeLitener(), closeDelay - timeLeft);
//                    logger.debug(caller + " will be close after " + (closeDelay - timeLeft) / 1000 + " seconds");
//                } else {
//                    logger.debug(caller + " Time up Closed, timeLeft=" + (closeDelay - timeLeft));
//
//                }
//            } catch (Exception e) {
//                logger.error("TimerTask error ", e);
//            }
//        }
    }
}

