package szhzz.Utils;



import szhzz.App.AppManager;
import szhzz.Calendar.MiscDate;
import szhzz.Config.CfgProvider;
import szhzz.Config.configData;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: vmuser
 * Date: 2007-12-1
 * Time: 22:57:45
 * To change this template use File | Settings | File Templates.
 */

public class ShutdownManager {
    public static boolean STATE_SHUTDOWN = false;
    public static boolean ECCHO = true;
    static int timeInteval = 10000;
    static boolean startWathc = false;
    static Object watcher = null;
    static Hashtable<Long, Thread> registedThread = new Hashtable<Long, Thread>();
    private static DawLogger logger = DawLogger.getLogger(ShutdownManager.class);
    private static Vector hardLock = new Vector();
    private static int resetNo = reset();
    private static waitForShutdown waittimer;

    private static int reset() {
        String f = CfgProvider.getRootFolder() + "/Shutdown.txt";
        try {
            Utilities.String2File("auto", f, false);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return 0;
    }

    public static void registThread(long timeup, Thread t) {
        registedThread.put(timeup, t);
    }

    public static void UnregistThread(long timeup) {
        registedThread.remove(timeup);
    }

    public static void addHardLock(Object o) {
        hardLock.add(o);
        if (waittimer == null) {
            watcher = new Object();
            waittimer = new waitForShutdown();
            Thread thread = new Thread(waittimer);
            thread.start();
            startWathc = true;
        }
    }

    public static void removeHardLock(Object o) {
        if (watcher != null) {
            synchronized (watcher) {
                hardLock.remove(o);
                if (watcher != null) watcher.notify();
            }
        } else {
            hardLock.remove(o);
        }
    }


    public static boolean allowedShutdown() {
        return (hardLock.size() == 0);
    }


    static String getStateString() {
        if (!startWathc) return "false";
        String f = CfgProvider.getRootFolder() + "/Shutdown.txt";
        return Utilities.File2String(f).trim();
    }

    protected static class waitForShutdown implements Runnable {

        String state = "no";

        public void run() {

            synchronized (watcher) {

                for (; ; ) {
                    state = getStateString();
                    if ("auto".equals(state)) {
                        STATE_SHUTDOWN = allowedShutdown();
                    }

                    if (!STATE_SHUTDOWN && "yes".equalsIgnoreCase(state)) {
                        logger.info("Shut down....");
                        STATE_SHUTDOWN = true;
                    }

                    for (Enumeration<Long> e = registedThread.keys(); e.hasMoreElements(); ) {
                        long t = e.nextElement();
                        if (t > MiscDate.timeCPU()) {
                            try {
                                Thread thread = registedThread.get(t);
                                if (thread.isAlive()) thread.interrupt();
                            } finally {
                                UnregistThread(t);
                            }
                        }
                    }

                    if (STATE_SHUTDOWN && allowedShutdown()) {
                        configData.save();
                        if (ECCHO) logger.info("All tasks closed");
                        System.exit(3);
                    } else {
                        if (ECCHO) ; //logger.info(getLookedObjectInfor());
                    }
                    try {
                        watcher.wait(timeInteval);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }
    }
}