package szhzz.Utils;



import szhzz.App.AppManager;
import szhzz.App.BeQuit;
import szhzz.Config.CfgProvider;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Created by Administrator on 2017/9/15.
 */
public class AppLocker {
    private static RandomAccessFile out = null;

    public static boolean lock(AppManager app) {
        boolean locked = false;

        String configFolder = CfgProvider.getRootFolder() + "\\loker\\";
        new File(configFolder).mkdirs();

        String appClass = app.getAppClass().getSimpleName();
        File locker = new File(configFolder + appClass + ".lock");
        try {
            while (true) {
                if (locker.exists() && !locker.delete()) {
                    break;
                }
                out = new RandomAccessFile(locker, "rw");
                out.write("lock".getBytes());
                locked = true;
                break;
            }

        } catch (Exception e) {
            locked = false;
        }
        if (!locked) {
            Shutdown();
        }
        return locked;
    }

    public static void Shutdown() {
        System.exit(1);
    }

    private static BeQuit autoQuit = new BeQuit() {
        @Override
        public boolean Quit() {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {

                }
            }
            return true;
        }
    };
}
