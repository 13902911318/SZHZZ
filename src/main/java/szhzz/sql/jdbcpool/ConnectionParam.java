/*
 * ConnectionParm.java
 *
 * Created on 2003年7月7日, 下午10:37
 */

package szhzz.sql.jdbcpool;

import java.io.Serializable;
import java.util.Properties;

/**
 * @author HuangFang
 */


/**
 * Creates a new instance of ConnectionParm
 */
public class ConnectionParam implements Serializable {
    private static boolean debug = false;
    private static int defaultPoolSize = 10;
    private static int maxConnections = 30;
    private static int defaulttimeou = 10 * 60 * 1000; // 10 * 60 * 1000 = 10 Min
    private Properties properties = new Properties();
    private String name;

    public ConnectionParam(String url, String user) {
        setUrl(url);
        setUser(user);
        name = url + ":" + user;  // 使之不可改变
        setWarmUp(true);
    }

    public static boolean isDebug() {
        return debug;
    }

    /**
     * setDebug() 显示详细的调试信息
     */
    public static void setDebug(boolean d) {
        if (debug != d) System.out.println("Connection pool debug=" + (d ? "ON" : "OFF"));
        debug = d;
    }

    public static int getDefaultPoolSize() {
        return defaultPoolSize;
    }

    public static void setDefaultPoolSize(int defaultPoolSize) {
        ConnectionParam.defaultPoolSize = defaultPoolSize;
    }

    public static int getMaxConnections() {
        return maxConnections;
    }

    public static void setMaxConnections(int maxConnections) {
        ConnectionParam.maxConnections = maxConnections;
    }

    public String getUrl() {
        return properties.getProperty("url");
    }

    /**
     * 数据连接的URL
     */
    private void setUrl(String url) {
        properties.setProperty("url", url);
    }

    public String getUser() {
        return properties.getProperty("user");
    }

    /**
     * 数据库用户名
     */
    private void setUser(String user) {
        properties.setProperty("user", user);
    }

    public String getPassword() {
        return properties.getProperty("password");
    }

    /**
     * 数据库登录密码
     */
    public void setPassword(String password) {
        properties.setProperty("password", password);
    }

    public int getPoolSize() {
        String s = properties.getProperty("minConnection");
        if (s == null) return defaultPoolSize;
        return Integer.parseInt(s);
    }

    /**
     * 惰性连接池大小
     */
    public void setPoolSize(int size) {
        if (size <= 0) size = defaultPoolSize;
        properties.setProperty("minConnection", "" + size);
    }

    public int getMaxConnection() {
        String s = properties.getProperty("maxConnection");
        if (s == null) return maxConnections;
        return Integer.parseInt(s);
    }

    /**
     * 可用最大连接数,但连接池内仅保留最小连接数
     */
    public void setMaxConnection(int c) {
        properties.setProperty("maxConnection", "" + c);
    }

    public long getTimeoutValue() {
        String s = properties.getProperty("timeoutValue");
        if (s == null) return defaulttimeou; // 30 * 60 * 1000 = 30 Min
        return Long.parseLong(s);
    }

    /**
     * 自动断开连接的最大空闲时间
     */
    public void setTimeoutValue(long t) {
        properties.setProperty("timeoutValue", "" + t);
    }

    public long getWaitTime() {
        String s = properties.getProperty("waitTime");
        if (s == null) s = "300";
        return Long.parseLong(s);
    }

    /**
     * 等待连接的最大等待时间
     */
    public void setWaitTime(long t) {
        properties.setProperty("waitTime", "" + t);
    }

    public String getDrive() {
        String s = properties.getProperty("driver");
        if (s == null) s = "sun.jdbc.odbc.JdbcOdbcDriver";
        return s;
    }

    /**
     * 数据库驱动程序
     */
    public void setDrive(String driv) {
        properties.setProperty("driver", driv);
    }

    /**
     * 返回属性体，尽量不要使用
     * 因为它使得今后的修改仍然依赖于Properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * 从外部设置一个属性体，尽量不要使用
     * 因为它使得今后的修改仍然依赖于Properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * 我们将利用内置的方式进行连接池的命名.
     */
    public String getName() {
        return name;
    }

    public boolean getWarmUp() {
        return ("yes".equals(properties.getProperty("warmup")));
    }

    /**
     * setWarmUp() 设置“热机”选项。
     * 连接池的缺省模式是惰性模式。要改变为主动模式，可设置此项为true
     * 使得一个连接池建立之后立刻自动建立 “setPoolSize”个连接待用。
     * 这个过程将在另一个线程内安静地进行，并且不影响立刻申请连接。
     */
    public void setWarmUp(boolean warmup) {
        properties.setProperty("warmup", (warmup ? "yes" : "no"));
    }

}
