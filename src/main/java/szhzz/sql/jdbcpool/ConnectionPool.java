/*
 * ConnectionPool.java
 *
 * Created on 2003年7月7日, 下午9:29
 */

package szhzz.sql.jdbcpool;

import szhzz.Utils.DawLogger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author HuangFang
 */
public class ConnectionPool {
    protected static Timer timer = null;  // shared by all pool
    private static DawLogger logger = DawLogger.getLogger(ConnectionPool.class);
    private final ConnectionParam param;
    //    BoundedLinkedQueue store = null;
    BoundedStack store = null;
    Integer conCreated = 0;
    int drop = 0;
    boolean isShutdown = false;
    Object syn_ = new Object();
    private boolean debug = false;

    protected ConnectionPool(ConnectionParam param) {
        // 保持对 param 的引用，使得参数的变化可以随时产生作用
        this.param = param;
        Properties pr = param.getProperties();
        debug = param.isDebug();
        setCapacity(param.getPoolSize());
        if (debug) logger.debug("Pool " + param.getName() + " created minConnection=" + param.getPoolSize());

        // 所有连接池共享一个时间控制线程
        if (timer == null) {
            timer = new Timer(true);  // deamon thread

            // 保持timer.schedule不会自动失效的空任务
            timer.schedule(new dumLitener(), 5 * 60 * 1000, 5 * 60 * 1000);

            //if(debug) logger.debug("Timer created");
        }
        String driver = param.getDrive();
        if (driver != null) {
            try {
                Class.forName(driver);
            } catch (Exception e) {
            }
        }
        if (param.getWarmUp()) {
//            WarmUp();
        }
    }

    /**
     * 改变连接池的大小
     */
    protected void setCapacity(int newCapacity) {
        if (store == null)
            store = new BoundedStack(newCapacity);
        else
            store.setCapacity(newCapacity);
    }

    protected int capacity() {
        return store.capacity();
    }

    public int size() {
        return store.size();
    }

    /**
     * isEmpty(), is the pool empty?
     */
    protected boolean isEmpty() {
        return store.isEmpty();
    }

    /**
     * the pool has connection avilable? but don't take out
     */
    protected Object peek() {
        return store.peek();
    }

    protected LinkedList peekAll() {
        return store.peekAll();
    }

    protected void put(Object item) throws InterruptedException {
        store.put(item);
    }


    /**
     * 无条件立刻建立连接，响应快速连接请求, 不受最大连接数的限制
     * 此连接停止使用后仍可能被放入连接池
     */
    protected Connection take() throws InterruptedException, SQLException {
        _Connection xcon = null;
        if (isShutdown) {
            throw new SQLException("Server shuted down!");
        }
        if (!isEmpty()) {
            xcon = (_Connection) poll(0);  // 不见得一定可以获得一个连接
        }
        if (xcon == null) xcon = new _Connection();
        return xcon.getConnection();
    }

    /**
     * 在连接池大小、最大连接数和等待时间的限制下
     * 提取连接
     */
    private Connection poll(long mins) throws InterruptedException, SQLException {
        Connection con = null;
        _Connection xcon = null;
        if (isShutdown) {
            throw new SQLException("Server shuted down!");
        }

        if (debug) logger.debug("poll out  connection ...");

        // 如果池为空，已建连接数尚未达到指定连接池大小，新建一个连接
        if (isEmpty() && conCreated < store.capacity()) { //param.getPoolSize()
            if (debug) logger.debug(conCreated + " < " + param.getPoolSize() + " Create a new connection");
            xcon = new _Connection();
        } else {
            if (debug)
                logger.debug(conCreated + " > " + param.getPoolSize() + " poll from store, wait time = " + param.getWaitTime());
            // 连接池不为空或已建连接数已经达到指定数量时
            // 在指定时间内等待提取其它线程释放的有效连接
            xcon = (_Connection) store.poll(mins);
            if (debug) logger.debug("xcon = " + xcon);
        }

        // 在指定时间内未能取得有效的连接，已建连接数尚未达到最大可用数量时
        if (xcon == null) {
            if (conCreated < param.getMaxConnection()) {
                if (debug) logger.debug(" xcon = null Create new");
                xcon = new _Connection();
            } else {
                logger.error(" Connection count = " + conCreated + " >= " + param.getMaxConnection());
            }
        }

        if (xcon != null) {
            con = xcon.getConnection();
        }

        if (con == null)
            if (debug) logger.warn(" can not get connection!");


        return con;
    }

    /**
     * 用param.getWaitTime()指定的时间，这个时间可能随时改变
     * 但仅影响后续的方法
     */
    protected Connection poll() throws InterruptedException, SQLException {
        return poll(param.getWaitTime());
    }

    private boolean offer(_Connection item, long msecs) throws InterruptedException {
        return store.offer(item, msecs);
    }


    /**
     * WarmUp()“热机方法”。
     * 连接池的缺省模式是惰性模式。要改变为主动模式，可设置param.setWarmUp
     * 为true,使得连接池建立之后立刻自动建立 “param.setPoolSize”个连接待用。
     * <p/>
     * 这个过程将在另一个线程内安静地进行，并且不影响消费线程立刻申请连接。
     */
    private void WarmUp() {
        Thread t = new Thread() {
            public void run() {
                for (; conCreated <= param.getPoolSize() / 2; ) {
                    try {
                        _Connection conn = new _Connection();
                        Connection xconn = conn.getConnection();
                        xconn.close();   // 不是 _Connection conn 的 close()!, 数据库的链接仍保持，只是放到缓存里了
                    } catch (Exception igno) {
                        logger.error("", igno);
                    }
                }
            }
        };
        t.start();
    }

    ////////////////////////////////////////////

    /**
     * shutDown() 仅关闭在连接池内尚未关闭的连接。
     * 对于已经取用的连接，取得控制的程序应予管理，
     * 尽量在shutDown()之前关闭连接，把连接返回连接
     * 池以便shutDown()能关闭所有连接。
     */
    protected void shutDown() {
        isShutdown = true;
        while (!isEmpty()) {
            try {
                _Connection xcon = (_Connection) store.poll(0);
                if (xcon != null) {
                    if (debug) logger.debug("Shut down...");
                    xcon.close();
                }
            } catch (Exception e) {
                logger.error("isStop() error", e);
            }
        }
    }

    protected void finalize() throws Throwable {
        shutDown();
        super.finalize();
    }

//    protected void pr(String msg) {
//        System.out.println(System.currentTimeMillis() + " ConnectionPool " + Thread.currentThread() + " " + msg);
//    }
//
//    protected void pr(String msg, Throwable e) {
//        System.out.println(System.currentTimeMillis() + " ConnectionPool " + Thread.currentThread() + " " + msg);
//        System.out.println(e.toString());
//        e.printStackTrace(System.out);
//    }

    /**
     * 数据连接的封装，接管了close方法
     *
     * @author HangFang
     */
    protected class _Connection implements InvocationHandler {
        private final static String CLOSE_METHOD_NAME = "close";
        private final static String ISCLOSE_METHOD_NAME = "isclosed";
        protected timeLitener currentTimer = null;
        String id = "";
        private Connection conn = null;
        private Connection xconn = null;
        //状态
        private boolean inUse = true;

        //用户最后一次访问该连接方法的时间
        private long lastAccessTime = System.currentTimeMillis();

        _Connection() {

            synchronized (syn_) {  //Warmup 有可能使序号重复
                id = "conn_" + conCreated++;
                if (debug) logger.debug("_Connection " + id + " created");
            }
        }

        /**
         * Returns the conn.
         *
         * @return Connection
         */
        protected Connection getConnection() throws SQLException {
            cancelTimer();

            if (debug) logger.debug("getConnection()");
            // 新建或恢复连接
            if (conn == null || conn.isClosed()) {
                xconn = null;   // GC
                if (debug) logger.debug("conn = " + conn);
                if (debug) logger.debug("conn reconnect ");

                conn = null;
                this.conn = DriverManager.getConnection(param.getUrl(), param.getUser(), param.getPassword());
            }

            if (xconn == null) {
                if (debug) logger.debug("Wrap with Strategy");
                //返回数据库连接conn的接管类，以便接管close方法
//                xconn = (Connection) Strategy.newProxyInstance(conn.getClass().getClassLoader(),
//                        conn.getClass().getInterfaces(), this);

                //改正来自 http://blog.csdn.net/njchenyi/article/details/3091092
                xconn = (Connection) Proxy.newProxyInstance(conn.getClass().getClassLoader(), new Class[]{Connection.class}, this);
            }

//            lastAccessTime = System.currentTimeMillis();
//            timer.schedule(getTimeLitener(), param.getTimeoutValue());
//            if (ConnectionParam.isDebug()) pr("Schedued " + id + " wait for " + param.getTimeoutValue());
//            if (ConnectionParam.isDebug()) pr("Exit from getConnection()");

            setInUse(true);
            return xconn;
        }

        /**
         * 该方法真正的关闭了数据库的连接
         * 仅在本实例被放回缓存并经过一定时间后，才会被关闭
         *
         * @throws SQLException
         */
        void close() throws SQLException {
            if (debug) logger.debug(id + " close() !");
            //由于类属性conn是没有被接管的连接，因此一旦调用close方法后就直接关闭连接
            if (conn != null && !conn.isClosed()) conn.close();
            try {
                if (!isShutdown && isInUse()) {
                    // 即使是已经断开连接的包装，仍然放回连接池
                    // 以提高效率
//                    if(debug) logger.debug(id + " offer back after close()");
//                    offer(this, 0);
                }
            } catch (Exception ie) {
                logger.error("close() error", ie);
            }
        }

        /**
         * @see java.lang.reflect.InvocationHandler
         * #invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object)
         */
        public Object invoke(Object proxy, Method m, Object[] args)
                throws Throwable {

            Object obj = null;
            //判断是否调用了close的方法，如果调用close方法则把连接返回给连接池
            // 对数据库的连接由 Timer 控制
            if (CLOSE_METHOD_NAME.equalsIgnoreCase(m.getName())) {
                if (debug) logger.debug(id + " invoked " + m + "(" + args + ")");
                if (isShutdown)
                    conn.close();
                else {
                    setInUse(false);
                    if (!offer(this, 0)) {  // offer back to pool
                        //无法放回缓存的实例将被抛弃。必须断开连接
                        close();
                        drop++;
                    } else {
                        // schedule to real close
                        lastAccessTime = System.currentTimeMillis();
                        timer.schedule(getTimeLitener(), param.getTimeoutValue());
                        if (debug)
                            logger.debug("Connection " + id + " offerback,  will be closed in " + param.getTimeoutValue() / 1000 + "s");
                    }
                }
            } else if (ISCLOSE_METHOD_NAME.equalsIgnoreCase(m.getName())) {
                return (!inUse || conn.isClosed());
            } else {
                if (debug) logger.debug(id + " invoked " + m + "(" + args + ")");
                if (conn.isClosed()) {
//                //TODO solve this prob
                    throw new SQLException("Error throwed from connection pool, Connection " + id + " is closed");
                    //    getConnection();
//                    if (!inUse) {
//                        offer(this, 0);
//                    } else {

//                    }
                } else {
                    obj = m.invoke(conn, args);
                }
            }
            return obj;
        }

        /**
         * Returns the lastAccessTime.
         *
         * @return long
         */
        protected long getLastAccessTime() {
            return lastAccessTime;
        }

        /**
         * Returns the inUse.
         *
         * @return boolean
         */
        protected boolean isInUse() {
            return inUse;
        }

        /**
         * Sets the inUse.
         *
         * @param inUse The inUse to set
         */
        protected void setInUse(boolean inUse) {
            this.inUse = inUse;
        }

        /**
         * 每一个 timeLitener 只能使用一次，因为
         * TimerTask 的cancel状态是不可撤销的
         * 在timeLitener被触发之前，如果该连接
         * 执行了操作，我们将向 Timer 登记一个新
         * timeLitener ，顺延等待时间，并废弃以
         * 前登记的timeLitener
         */
        protected timeLitener getTimeLitener() {
            cancelTimer();
            currentTimer = new timeLitener();
            return currentTimer;
        }

        void cancelTimer() {
            if (currentTimer != null) {
                currentTimer.cancel();
                currentTimer = null; // GC
            }
        }

        public boolean isThis(Connection conn) {
            return (this.conn != null && this.conn == conn);
        }

        public String getId() {
            return id;
        }

        public String getInfor() {
            try {
                if (conn.isClosed()) {
                    return "Con" + id + " disconnected.";
                } else {
                    return "Con" + id + " Closed in " + (param.getTimeoutValue() - (System.currentTimeMillis() - lastAccessTime)) / 1000 + "s";
                }
            } catch (SQLException e) {

            }
            return "Con" + id;
        }

        /**
         * 在连接包装内部的时间控制任务
         */
        protected class timeLitener extends TimerTask {
            /**
             * 该方法将由Timer触发
             */
            public void run() {
                try {

                    long timeLeft = (System.currentTimeMillis() - lastAccessTime);
                    // 在指定时间内，曾经操作过数据库
                    if (!isShutdown && timeLeft < param.getTimeoutValue()) {
                        // reschedule
                        timer.schedule(getTimeLitener(), param.getTimeoutValue() - timeLeft);
                        if (debug) logger.debug(id + " will be close after " + (param.getTimeoutValue() - timeLeft));
                    } else {
                        if (debug)
                            logger.debug(id + " Time up Closed, timeLeft=" + (param.getTimeoutValue() - timeLeft));
                        close();  // parentClass.close()
                    }
                } catch (Exception e) {
                    logger.error("TimerTask error ", e);
                }
            }
        }
    }

    /**
     * 每一个连接，均使用 Time 的延迟功能实现超时自动
     * 断开连接。当一个 Time 实例内没有任务时，该 Time
     * 自动停止，并不可以恢复使用。我们使用一个空任务在
     * Time 实例内执行定时循环任务，以防止Time 实例的
     * 停止
     * 该 dumLitener 今后可能加入某些维护功能
     */
    private class dumLitener extends TimerTask {
        public void run() {
        }
    }

}
