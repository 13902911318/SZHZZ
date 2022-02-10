package szhzz.Config;

import szhzz.sql.database.Database;
import szhzz.sql.jdbcpool.DbStack;

import java.sql.ResultSet;

/**
 * Created by HuangFang on 2015/4/5.
 * 12:43
 */
public class DbCfgProvider extends CfgProvider {
    private static DbCfgProvider onlyOne = null;
    public static final String gourpID = "[DataBase]";

    private DbCfgProvider() {
        laodCfgs("");
    }

    static DbCfgProvider getInstance() {
        if (onlyOne == null) {
            onlyOne = new DbCfgProvider();
        }
        return onlyOne;
    }

    public Config getCfg(String cfgID, boolean createNew) {
        if (cfgID == null) return null;

        Config cfg_ = allCfgs.get(cfgID);
        if (cfg_ == null) {
                    cfg_ = new DbConfig();
                    cfg_.load(cfgID);
                    addCfg(cfg_);
//            if (createNew) {
//                try {
//                    cfg_ = new DbConfig();
//                    cfg_.load(cfgID);
//                    addCfg(cfg_);
//                } catch (Exception e) {
//
//                }
//            } else {
//                return null;
//            }
        }
        return cfg_;
    }

    public String getDir() {
        return gourpID;
    }

    public static String getRootFolder() {
        return gourpID;
    }

    protected void laodCfgs(String name) {
        ResultSet rs = null;
        allCfgs.clear();
        String sql = "select distinct `ID` from `Config` order by `ID`";
        Database db = DbStack.getDb(DbConfig.class);
        try {
            rs = db.dynamicSQL(sql);
            while (rs.next()) {
                allCfgs.put(rs.getString(1), null);
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            Database.closeResultSet(rs);
            DbStack.closeDB(db);
        }
    }

    public static void main(String[] args) {
        CfgProvider.getInstance(gourpID);
    }
}
