package szhzz.Config;



import szhzz.App.AppManager;
import szhzz.sql.database.Database;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;

/**
 * Created with IntelliJ IDEA.
 * User: SZHZZ
 * Date: 13-3-23
 * Time: 上午10:32
 * To change this template use File | Settings | File Templates.
 */
public class ConfigDB extends Config {
    private Database db = null;

    private ConfigDB() {
    }

    public ConfigDB(Database db, String newCfgID) {
        this.db = db;
        this.configID = newCfgID;
    }

    public ConfigDB(Database db) {
        this.db = db;
    }

    @Override
    public boolean save() {
        boolean saved = true;
        AppManager App = AppManager.getApp();
        if (this.db == null) {
            this.db = App.getDatabase(this.getClass());
        }
        App.tryOpendb(this.db);

        StringBuffer sb = new StringBuffer("");
        for (item e : index) {
            sb.append(e.toString());
            sb.append("\n");
        }
        try {
            String sql = "REPLACE INTO cfgTable (ID, Data) VALUES ('" + configID + "',";
            sql += "'" + sb.toString() + "')";
            if (db != null) {
                if (!db.isOpened()) db.openDB();
                db.executeUpdate(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            saved = false;
        }
        return saved;
    }


    @Override
    public void load(String ID) {
        String val = null;
        AppManager App = AppManager.getApp();
        if (this.db == null) {
            this.db = App.getDatabase(this.getClass());
        }
        App.tryOpendb(this.db);

        this.configID = ID;
        clear();

        String sql = "SELECT Data FROM cfgTable WHERE ID='" + ID + "'";
        ResultSet rs = null;
        try {
            rs = this.db.dynamicSQL(sql);
            if (rs.next()) {
                val = rs.getString(1);
                rs.close();
                BufferedReader buff = new BufferedReader(new InputStreamReader((new ByteArrayInputStream(val.getBytes()))));
                loadDataVal(buff);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Database.closeResultSet(rs);
        }
    }

    @Override
    public void reLoad() {
        load(configID);
    }

    @Override
    public String getConfigUrl() {
        return db.get_DBUrl() + "/" + configID;
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getConfigFolder() {
        return AppManager.getApp().getCfg().getConfigFolder();
    }
}
