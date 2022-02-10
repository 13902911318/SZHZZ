package szhzz.Config;

import szhzz.Calendar.MyDate;
import szhzz.Utils.DawLogger;
import szhzz.sql.database.Database;
import szhzz.sql.jdbcpool.DbStack;

import java.io.File;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Project: Quant
 * Package: PACKAGE_NAME
 * <p>
 * User: HuangFang
 * Date: 2022/2/4
 * Time: 19:26
 * <p>
 * Created with IntelliJ IDEA
 */
public class DbConfig extends ConfigF {
    private static DawLogger logger = DawLogger.getLogger(DbConfig.class);
    private String configUrl;
    private static Boolean isNew = null;

    DbConfig() {
    }

    boolean save(Database db) {
        ResultSet rs = null;
        String delete = "delete from `Config` where `ID` = '" + getConfigID() + "'";
        String update = "insert into `Config` (`ID`, `name`, `value`, `comment`, `LineNo`) values ";
        try {
            db.executeUpdate(delete);

            int count = 0;
            int lineNo = 0;
            StringBuffer sb = new StringBuffer(update);
            setProperty("//# LastUpdate", MyDate.getToday().getDateTime(), "Protect");

            for (String name : getKeys()) {
                if (count > 0) {
                    sb.append(",");
                }
                sb.append("('").append(getConfigID()).append("', ");
                sb.append("'").append(name).append("', ");
                sb.append("'").append(getProperty(name)).append("', ");
                sb.append("'").append(getComment(name)).append("', ");
                sb.append(++lineNo).append(")");

                if (sb.length() > 1024) {
                    db.executeUpdate(sb.toString());
                    sb = new StringBuffer(update);
                    count = 0;
                } else {
                    count++;
                }
            }
            if (count > 0) {
                db.executeUpdate(sb.toString());
            }
        } catch (Exception e) {
            logger.error(e);
            return false;
        } finally {
            Database.closeResultSet(rs);
        }
        return true;
    }


    public String getProperty(String name) {
        if (datas.get(name) != null) {
            String val = decodeLine(datas.get(name).value);
            if ("".equals(val) || "null".equals(val.toLowerCase()) || "''".equals(val) || "\"\"".equals(val)) {
                return null;
            }
            return val;
        }
        return null;
    }

    @Override
    public boolean save() {
        Database db = DbStack.getDb(DbConfig.class);
        try {
            save(db);
        } catch (Exception e) {
            logger.error(e);
            return false;
        } finally {
            DbStack.closeDB(db);
        }
        return true;
    }

    @Override
    public void load(String s) {
        this.setCfgID(s);
        reLoad();
    }

    @Override
    public void reLoad() {
//        if(!DbConfig.isNewVersion()){
//            CgangeDbCfg.change();
//        }

        datas.clear();
        index.clear();

        ResultSet rs = null;
        String sql = "select `ID`, `name`, `value`, `comment` from `Config` where ID = '" + getConfigID() + "' order by LineNo ";
        Database db = DbStack.getDb(DbConfig.class);
        try {
            rs = db.dynamicSQL(sql);
            while (rs.next()) {
                this.setProperty(rs.getString("name"), rs.getString("value"), rs.getString("comment"));
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            Database.closeResultSet(rs);
            DbStack.closeDB(db);
        }
    }

    @Override
    public String getConfigUrl() {
        if (configUrl == null) {
            Database db = DbStack.getDb(DbConfig.class);
            try {
                configUrl = db.get_DBUrl() + "/" + getConfigID();
            } finally {
                DbStack.closeDB(db);
            }
        }
        return configUrl;
    }


    public void setConfigFileName(String configFileName) {
    }

    public void setConfigFile(File configFile) {
    }

    public String getConfigFolder() {
        return null;
    }

    public static boolean isNewVersion() {
        if(isNew == null) {
            isNew = false;

            ResultSet rs = null;
            String sql = "select Value from Config where ID = '系统参数' and Name = 'ConfigDB changed'"  ;
            Database db = DbStack.getDb(DbConfig.class);
            try {
                if(db.hasTable("config")) {
                    rs = db.dynamicSQL(sql);
                    if (rs.next()) {
                        Object v = rs.getObject(1);
                        if (v != null) {
                            String debugg = v.toString();
                            isNew = v.toString().toLowerCase().equals("true");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            } finally {
                Database.closeResultSet(rs);
                DbStack.closeDB(db);
            }
        }
        return isNew;
    }

}
