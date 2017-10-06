/*
 * ConnectionPool.java
 *
 * Created on 2003年7月9日, 下午3:43
 */

package szhzz.sql.jdbcpool;


import javax.naming.NameAlreadyBoundException;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;

/**
 * ConnectionPool 的 xmlParam 初始化参数适配器
 *
 * @author HuangFang
 */
public class ConnectionAdaptor {
    private static Hashtable store = new Hashtable();

    /**
     * Creates a new instance of ConnectionPool
     */
    public ConnectionAdaptor() {
    }

    public static void setParm(Properties xparm)
            throws NameAlreadyBoundException,
            ClassNotFoundException,
            IllegalAccessException,
            InstantiationException,
            SQLException,
            IllegalArgumentException, szhzz.sql.jdbcpool.NameAlreadyBoundException {

        String var = null;
        String url = xparm.getProperty("URL");
        if (url == null) throw new IllegalArgumentException("URL cannot be empty!");

        String user = xparm.getProperty("User");

        ConnectionParam param = new ConnectionParam(url, user);

        var = xparm.getProperty("Connection");
        if (var == null)
            throw new IllegalArgumentException("Invalid Param Node Type");

        var = xparm.getProperty("Driver");
        if (var != null && var.length() > 0)
            param.setDrive(var);

        var = xparm.getProperty("PoolSize");
        if (var == null) var = "5";
        param.setPoolSize(Integer.parseInt(var));

        var = xparm.getProperty("MaxConnection");
        if (var == null) var = "5";
        param.setMaxConnection(Integer.parseInt(var));

        var = xparm.getProperty("WaitTime");
        if (var == null) var = "300";
        param.setWaitTime(Long.parseLong(var));

        var = xparm.getProperty("Timeout");
        if (var == null) var = "300000";
        param.setTimeoutValue(Long.parseLong(var));


        var = xparm.getProperty("Password");
        param.setPassword(var);

        param.setDebug(true);

        var = xparm.getProperty("WarmUp");
        param.setWarmUp("yes".equals(var));


        if (!store.containsKey(param.getName())) {
            store.put(param.getName(), param);
            ConnectionManager.bind(param);
        }
    }

    public static void shutDown() {
        ConnectionManager.shutDown();
    }
}
