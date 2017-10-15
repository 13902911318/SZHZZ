/*
 * ConnectionFactory.java
 *
 * Created on 2003年7月7日, 下午8:45
 */

package szhzz.sql.jdbcpool;

import javax.naming.NameNotFoundException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

/**
 * @author HuangFang
 */

/**
 * @author HaungFang
 */
public class ConnectionManager {

    static Hashtable connectionPools = null;

    static {
        connectionPools = new Hashtable(2, 0.75F);
    }

    public static void setDebug(boolean b) {
        ConnectionParam.setDebug(b);
    }

    /**
     * 从连接池工厂中获取指定名称对应的连接池对象
     *
     * @param dataSource 连接池对象对应的名称
     * @return DataSource    返回名称对应的连接池对象
     * @throws NameNotFoundException 无法找到指定的连接池
     */
    private static ConnectionPool peak(String dataSource)
            throws NameNotFoundException {
        ConnectionPool ds = null;
        ds = (ConnectionPool) connectionPools.get(dataSource);
        return ds;
    }

    public static Hashtable<String, Integer> getConnectionCount() {
        Hashtable<String, Integer> retVal = new Hashtable<String, Integer>();
        for (Enumeration e = connectionPools.keys(); e.hasMoreElements(); ) {
            String name = (String) e.nextElement();
            ConnectionPool ds = (ConnectionPool) connectionPools.get(name);
            retVal.put(name + " Avalable", ds.size());
            retVal.put(name + " Cpacity", ds.capacity());
        }

        return retVal;
    }

    public static String getId(Connection conn) {
        if (conn instanceof Proxy) {
            InvocationHandler h = Proxy.getInvocationHandler(conn);
            if (h instanceof ConnectionPool._Connection) {
                return ((ConnectionPool._Connection) h).getId();
            }
        }
        return null;
    }

    public static LinkedList<Vector> peekAll() {
        LinkedList<Vector> retVal = new LinkedList<Vector>();
        Vector v;
        for (Enumeration e = connectionPools.keys(); e.hasMoreElements(); ) {
            String name = (String) e.nextElement();
            ConnectionPool ds = (ConnectionPool) connectionPools.get(name);

            v = new Vector();
            v.add(name + " Created");
            v.add(ds.conCreated);
            retVal.add(v);

            v = new Vector();
            v.add(name + " Drop");
            v.add(ds.drop);
            retVal.add(v);

            LinkedList l = ds.peekAll();
            int c = 1;
            for (Object o : l) {
                v = new Vector();

                v.add(name + " " + ((ConnectionPool._Connection) o).getId());
                v.add(((ConnectionPool._Connection) o).getInfor());
                retVal.add(v);
            }
        }

        return retVal;
    }

    /**
     * 将指定的名字和数据库连接配置绑定在一起并初始化数据库连接池
     *
     * @param param 对应连接池的名称
     * @param param 连接池的配置参数，具体请见类ConnectionParam
     * @return DataSource    如果绑定成功后返回连接池对象
     * @throws NameAlreadyBoundException 一定名字name已经绑定则抛出该异常
     * @throws ClassNotFoundException    无法找到连接池的配置中的驱动程序类
     * @throws IllegalAccessException    连接池配置中的驱动程序类有误
     * @throws InstantiationException    无法实例化驱动程序类
     * @throws SQLException              无法正常连接指定的数据库
     */
    public static ConnectionPool bind(ConnectionParam param)
            throws NameAlreadyBoundException, ClassNotFoundException,
            IllegalAccessException, InstantiationException, SQLException {
        ConnectionPool source = null;
        if (connectionPools.containsKey(param.getName()))
            throw new NameAlreadyBoundException(param.getName());

        source = new ConnectionPool(param);
        connectionPools.put(param.getName(), source);
        return source;
    }

    /**
     * 重新绑定数据库连接池
     *
     * @param param 对应连接池的名称
     * @param param 连接池的配置参数，具体请见类ConnectionParam
     * @return DataSource    如果绑定成功后返回连接池对象
     * @throws NameAlreadyBoundException 一定名字name已经绑定则抛出该异常
     * @throws ClassNotFoundException    无法找到连接池的配置中的驱动程序类
     * @throws IllegalAccessException    连接池配置中的驱动程序类有误
     * @throws InstantiationException    无法实例化驱动程序类
     * @throws SQLException              无法正常连接指定的数据库
     */
    private static ConnectionPool rebind(ConnectionParam param)
            throws NameAlreadyBoundException, ClassNotFoundException,
            IllegalAccessException, InstantiationException, SQLException {
        try {
            unbind(param);
        } catch (Exception e) {
        }
        return bind(param);
    }

    /**
     * 删除一个数据库连接池对象
     *
     * @param param
     * @throws NameNotFoundException
     */
    private static void unbind(ConnectionParam param) throws NameNotFoundException {
        ConnectionPool dataSource = peak(param.getName());
        if (dataSource instanceof ConnectionPool) {
            ConnectionPool dsi = dataSource;
            try {
                //dsi.stop();
                dsi.shutDown();
            } catch (Exception e) {
            } finally {
                dsi = null;
            }
        }
        connectionPools.remove(param.getName());
    }

    public static void shutDown() {
        for (Enumeration e = connectionPools.keys(); e.hasMoreElements(); ) {
            ConnectionPool dsi = (ConnectionPool) connectionPools.get(e.nextElement());
            dsi.shutDown();
        }
    }

    public static Connection getConnection(ConnectionParam param)
            throws ClassNotFoundException, InterruptedException,
            IllegalAccessException, InstantiationException, SQLException {
        Connection con = null;
        ConnectionPool cp = null;
        try {
            cp = peak(param.getName());
            if (cp == null) cp = bind(param);
        } catch (Exception ex) {
        }

        if (cp != null) {
            con = cp.poll();
            if (ConnectionParam.isDebug()) pr("get Connection success queryName = " + param.getName());
        } else {
            if (ConnectionParam.isDebug()) pr("get Connection false queryName = " + param.getName());
        }
        return con;
    }

    public static Connection getConnection(String url, String user, String password)
            throws ClassNotFoundException, InterruptedException,
            IllegalAccessException, InstantiationException, SQLException {

        ConnectionParam param = new ConnectionParam(url, user);
        param.setPassword(password);
        return getConnection(param);
    }


    private static void pr(String msg) {
        if (ConnectionParam.isDebug()) System.out.println(">>ConnectionManager " + msg);
    }

    private static void pr(String msg, Throwable e) {
        System.out.println(">>ConnectionManager " + msg);
        e.printStackTrace();
    }

}

