package szhzz.sql.jdbcpool;

//import org.apache.commons.pool.BasePoolableObjectFactory;
//import org.apache.commons.pool.ObjectPool;
//import org.apache.commons.pool.impl.StackObjectPool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import szhzz.sql.database.Database;
import szhzz.App.AppManager;
import szhzz.Utils.DawLogger;

/**
 * Created by Administrator on 2015/6/28.
 * 用于频繁,短时,同步的连接
 */
public class DbStack {
    private static DawLogger logger = DawLogger.getLogger(DbStack.class);
    private static ObjectPool<Database> pool = null;
    private static int maxIdle = 0;
    private static int maxActive = 0;
    private static AppManager App = AppManager.getApp();


    public static Database getDb(Class requestor) {
        Database db = null;
        if (pool == null) {
            GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
//            cfg.setMaxIdle(5);
            cfg.setMinIdle(5);
//            cfg.setLifo(true);           //default
            pool = new GenericObjectPool<Database>(new DbFactory(), cfg);
        }

        try {
            maxIdle = Math.max(maxIdle, pool.getNumIdle());

            if (maxIdle < pool.getNumIdle()) {
                maxIdle = pool.getNumIdle();
                logger.info("getDb " + requestor.getName() + " max NumIdle=" + maxIdle);
            }

            db = pool.borrowObject();
            db.setCaller(requestor.getName());

            if (maxActive < pool.getNumActive()) {
                maxActive = pool.getNumActive();
                logger.info("getDb " + requestor.getName() + " pool max NumActive=" + maxActive);
            }

        } catch (Exception e) {
            logger.error(e);
        }
        if (db == null) {
            db = App.getDatabase(requestor);
//            App.logEvent("new DB " + dbPool.size());
        }
        tryOpendb(db);
        return db;
    }

    public static void closeDB(Database db) {
        if (db == null) return;
        try {
            pool.returnObject(db);
        } catch (Exception e) {
            logger.error(e);
            db.close();
        }
    }

    public static boolean tryOpendb(Database db) {
        boolean isOpened = false;
        if (db == null)
            return false;

        try {
            isOpened = db.openDB();
        } catch (Exception e) {
            logger.error(e);
        }
        return isOpened;
    }


    static class DbFactory extends BasePooledObjectFactory<Database> {
        @Override
        public Database create() throws Exception {
            return App.getDatabase(DbStack.class);
        }

        @Override
        public PooledObject<Database> wrap(Database obj) {
            return new DefaultPooledObject<Database>(obj);
        }

        /**
         * 产生一个新对象
         */
//        public PooledObject<Database> makeObject() {
//            return App.getDatabase(DbStack.class);
//        }

//        public boolean validateObject(Object obj) {
//            return (obj instanceof Database);
//        }

        /**
         * 还原对象状态
         */
        public void passivateObject(PooledObject<Database> pooledObject) {
//            App.logEvent("passivateObject(Object obj)");
            Database db = pooledObject.getObject();
            db.setCaller(DbStack.class.getName());
            db.close(30);
        }

        public void destroyObject(PooledObject<Database> p) throws Exception {
            Database db = p.getObject();
            db.close();
        }
    }

}
