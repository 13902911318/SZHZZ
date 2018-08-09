package szhzz.sql.jdbcpool;


import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import szhzz.App.AppManager;
import szhzz.Config.SharedCfgProvider;
import szhzz.Utils.DawLogger;
import szhzz.sql.database.DBException;
import szhzz.sql.database.DBProperties;
import szhzz.sql.database.Database;

import java.util.HashMap;

/**
 * Created by Administrator on 2015/6/28.
 * 用于频繁,短时,同步的连接
 */
public class MsDbStack {
    private static DawLogger logger = DawLogger.getLogger(MsDbStack.class);
//    private static AppManager App = AppManager.getApp();
    private ObjectPool<Database> pool = null;
    private int maxIdle = 0;
    private int maxActive = 0;
    private static HashMap<String, MsDbStack> DbInstanceMap = new HashMap<>();
    private static String defaultFileName = "SQLServer.ini";
    private String fileName = "SQLServer.ini";
    private DBProperties dbProperties = null;

    public static Database getDb(Class requestor){
        return getInstance().getDb_(requestor);
    }
    public static void closeDB(Database db) {
        getInstance().closeDB_(db);
    }

    public static Database getDb(String fileName, Class requestor){
        return getInstance(fileName).getDb_(requestor);
    }
    public static void closeDB(String fileName, Database db) {
        getInstance(fileName).closeDB_(db);
    }

    private static MsDbStack getInstance(String fileName){
        MsDbStack dbStack = DbInstanceMap.get(fileName);
        if(dbStack == null){
            dbStack = new MsDbStack();
            dbStack.fileName = fileName;
            DbInstanceMap.put(fileName, dbStack);
        }
        return dbStack;
    }

    private static MsDbStack getInstance(){
        return getInstance(MsDbStack.defaultFileName);
    }


    private Database getDb_(Class requestor) {
        Database db = null;
        if (pool == null) {
            GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
            cfg.setMinIdle(5);
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
            db = Database.getInstance(getTargetDbProp(), requestor);
        }
        tryOpendb(db);
        return db;
    }

    private void closeDB_(Database db) {
        if (db == null) return;
        try {
            pool.returnObject(db);
        } catch (Exception e) {
            logger.error(e);
            db.close();
        }
    }

    private boolean tryOpendb(Database db) {
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

    private DBProperties getTargetDbProp() {
        if(dbProperties == null) {
            try {
                dbProperties = new DBProperties(getCfg());
            } catch (DBException e) {
                logger.error(e);
            }
        }
        return dbProperties;
    }

    private String getCfg() {
        return SharedCfgProvider.getInstance("MySql").getDir() + "\\" + fileName;
    }

    class DbFactory extends BasePooledObjectFactory<Database> {
        @Override
        public Database create() throws Exception {
            return Database.getInstance(getTargetDbProp(), MsDbStack.class);
        }

        @Override
        public PooledObject<Database> wrap(Database obj) {
            return new DefaultPooledObject<Database>(obj);
        }


        /**
         * 还原对象状态
         */
        public void passivateObject(PooledObject<Database> pooledObject) {
//            App.logEvent("passivateObject(Object obj)");
            Database db = pooledObject.getObject();
            db.setCaller(MsDbStack.class.getName());
            db.close(30);
        }

        public void destroyObject(PooledObject<Database> p) throws Exception {
            Database db = p.getObject();
            db.close();
        }
    }

}
