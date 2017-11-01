package szhzz.sql.database;

import szhzz.App.AppManager;
import szhzz.Utils.DawLogger;
import szhzz.sql.gui.DB_Connection;

import java.awt.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/10/27.
 */
public class OpenMySql {
    private static DawLogger logger = DawLogger.getLogger(OpenMySql.class);

    public static boolean openMySql() {
        AppManager app = AppManager.getApp();
        Database db = app.getDatabase(OpenMySql.class);
        boolean connected = false;
        try {
            if (app.tryOpendb(db)) {
                connected = true;
            }else{
                String startMySql = System.getProperty("user.dir") + "\\bats\\StartMySql.bat" ;
                if(new File(startMySql).exists()){
                    String command = "cmd /c start \"\" \"" + startMySql + "\"";
                    try {
                        Runtime.getRuntime().exec(command);//避开win10的权限限制 //
                        TimeUnit.SECONDS.sleep(5);
                        db = app.getDatabase(OpenMySql.class);
                        if (app.tryOpendb(db)) {
                            connected = true;
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
                if(!connected ){
                    openCfg(null);
                    app.resetTargetDbProp();
                    db = app.getDatabase(OpenMySql.class);
                    if (app.tryOpendb(db)) {
                        connected = true;
                    }
                }
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return connected;
    }

    public static String openCfg(String file) {
        if (file == null || "".equals(file)) {
            file = AppManager.getApp().getCurrentDBCfg();
        }
        DBProper data = null;
        try {
            data = new DBProper(file);
            DB_Connection dialog = new DB_Connection();
            dialog.setData(data);
            dialog.pack();

            Dimension dim = dialog.getToolkit().getScreenSize();
            Rectangle abounds = dialog.getBounds();
            dialog.setLocation((dim.width - abounds.width) / 2 + 100,
                    (dim.height - abounds.height) / 2 + 120);

            dialog.setVisible(true);
            file = (data.getFileName());
        } catch (DBException e) {
            logger.error(e);  //To change body of catch statement use File | Settings | File Templates.
        }
        return file;
    }

}
