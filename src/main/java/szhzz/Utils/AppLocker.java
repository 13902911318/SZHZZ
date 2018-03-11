package szhzz.Utils;


import sun.security.action.GetPropertyAction;
import szhzz.App.AppManager;
import szhzz.App.BeQuit;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.AccessController;


/**
 * Created by Administrator on 2017/9/15.
 */
public class AppLocker {
    private static RandomAccessFile out = null;

    public static boolean lock(String title) {
        boolean locked = !Executor.isRunning(null, title);
        if (!locked) {
            Shutdown();
        }
        return locked;
    }

    public static boolean lock() {
        boolean locked = false;

//        String appConfigFolder = CfgProvider.getRootFolder() + "\\loker\\";
//        new File(appConfigFolder).mkdirs();

        File configFolder = TempDirectory.location();

        String appClass = AppManager.getAppClass().getSimpleName();
        File locker = new File(configFolder + "\\"+ appClass + ".lock");

        try {
            while (true) {
                if (locker.exists() && !locker.delete()) {
                    break;
                }
                out = new RandomAccessFile(locker, "rw");
                out.write("lock".getBytes());
                locker.deleteOnExit();
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
//        System.exit(1);
        Shutdown dialog = new Shutdown();
        dialog.setModal(false);
        dialog.setAlwaysOnTop(true);
        dialog.startup();

        dialog.setQuit(5);
        dialog.pack();
        dialog.setVisible(true);

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

    private static class TempDirectory {
        private TempDirectory() {
        }

        // temporary directory location
        private static final File tmpdir = new File(AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir")));

        static File location() {
            return tmpdir;
        }

    }
}


