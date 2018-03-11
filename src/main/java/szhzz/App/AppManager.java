package szhzz.App;



import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;
import szhzz.Calendar.MyDate;
import szhzz.Config.*;
import szhzz.Timer.AlarmClock;
import szhzz.Timer.TimerEvent;
import szhzz.sql.database.DBException;
import szhzz.sql.database.DBProperties;
import szhzz.sql.database.Database;
import szhzz.Calendar.MiscDate;
import szhzz.DataBuffer.DataConsumer;
import szhzz.DataBuffer.ObjBufferedIO;
import szhzz.Mail.MailMsg;
import szhzz.Mail.Mailer;
import szhzz.Utils.DawLogger;
import szhzz.Utils.DiskUtils;
import szhzz.Utils.HardwareIDs;
import szhzz.Utils.Utilities;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-9-6
 * Time: 11:17:57
 * To change this template use File | Settings | File Templates.
 */
public class AppManager implements DataConsumer {
    public static final Object eventLock = new Object();
    static final Object lock = new Object();
    public static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
    static protected JTextComponent logAre = null;
    protected static AppManager app = null;
    static PooledExecutor executor = null;
    static PooledExecutor sysExecutor = null;
    static processWatcher thredWatcher = null;
    private static DawLogger logger = DawLogger.getLogger(AppManager.class);
    private static Vector<BeQuit> beQuits = new Vector<BeQuit>();
    private static boolean quitApp = false;
    static private JXBusyLabel Busy = null;
    static private AlarmClock alarmClock = null;
    private static boolean stopProcess = true;
    private static String hostName = null;
    private static boolean autoGc = true;
    private static String configFolder = "configs";
    private static JLabel threadLabel = null;
    protected Config cfg;
    Vector<JProgressBar> progress = new Vector<JProgressBar>();
    String file = "";
    JFrame frame = null;
    MemoBar memoBar = null;
    private JTextComponent eventLogAre = null;
    private StringBuffer eventBuffer = new StringBuffer();
    private boolean locked = false;
    private boolean midleClose = false;
    private JLabel StatuBar = null;
    private JLabel DigitalClock = null;
    private static String cpuID = null;
    private String telNo = null;
    private String macID = null;
    private String HD_ID = null;
    private String localIP = null;
    private String appName = null;  //出版名称
    private String icoUrl = null;
    private static ObjBufferedIO logBuffer = null;
    private static EventLoger eventLoger;
    private static Vector<ClareBuffer> beClareBuffers = new Vector<ClareBuffer>();

    //    private static Shutdown dialog = null;
    private static boolean debug = false;

    private static Mailer mailer = null;
    private MailMsg mailMsg = null;
    private static HashSet<String> localIPs = null;
    private Boolean openEmail = null;
    private static long systemTimeDiff = 0L;
    private static String currentDisk = null;
    private static Class appClass = null;
    private static Boolean autoShutdown = false;
    private static String debugFile = null;
    protected DBProperties targetDbProp = null;

    protected AppManager() {
        eventLoger = new EventLoger();
        logBuffer = new ObjBufferedIO();
        try {
            logBuffer.setReader(this, 50);     //256
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

    public static long getSystemTimeDiff() {
        return systemTimeDiff;
    }

    public static void setSystemTimeDiff(long systemTimeDiff) {
        AppManager.systemTimeDiff = systemTimeDiff;
    }


    private void prepareIDs() {
        getIP(cfg.getProperty("VPN_Signature", null));
        getIP(cfg.getProperty("NET_CARD", null));

        getTelNo();                                  //[23]手机号（电话号）
        getCpuID();                                    //[24]CPUID
        getHD_ID();                              //[25]硬盘序列号
        getMac();                               //[26]网卡Mac地址
        getAppName();                            //[28]客户端名称
        getVersion();                            //[29]客户端版本
    }

    public static AppManager getApp() {
        if (app == null) {
            app = new AppManager();
        }
        return app;
    }

    public static String getCurrentDisk(){
        if(currentDisk == null) {
            String cpath = getCurrentFolder();
            currentDisk = cpath.substring(0, cpath.indexOf(":")) + ":";
        }
        return currentDisk;
    }

    public static String getCurrentFolder() {
        return System.getProperty("user.dir");
    }

    public static void registerClareBuffer(ClareBuffer bg) {
        beClareBuffers.add(bg);
    }

    public static void clareDirtyData() {
        for (ClareBuffer bg : beClareBuffers) {
            bg.clare();
        }
    }

    public static void reshape(JXBusyLabel Busy, float height) {
        float barLength = (height) * 8 / 26;
        Shape trajectory = null;
        trajectory = new Ellipse2D.Float(barLength / 2, barLength / 2, height - barLength, height - barLength);

        Shape pointShape = new Ellipse2D.Float(0, 0, barLength, barLength * 0.318f);

        BusyPainter bp = new BusyPainter(pointShape, trajectory);

        BusyPainter old = Busy.getBusyPainter();
        bp.setTrailLength(old.getTrailLength());
        bp.setPoints(old.getPoints());
        bp.setFrame(old.getFrame());
        Busy.setPreferredSize(new Dimension((int) (height), (int) (height)));
        Busy.setIcon(new EmptyIcon((int) (height), (int) (height)));
        Busy.setBusyPainter(bp);
        bp.setHighlightColor(old.getHighlightColor());
        bp.setBaseColor(old.getBaseColor());
        Busy.repaint();
    }

    public static String getConfigFolder() {
        return configFolder;
    }

//    public void setConfigFolder(String appConfigFolder) {
//        AppManager.appConfigFolder = appConfigFolder;
//        loadConfig();
//    }

    public static void setAutoGc(boolean b) {
        autoGc = b;
    }

    public static boolean isStopProcess() {
        return stopProcess;
    }

    public static void setStopProcess(final boolean b) {
        stopProcess = b;
    }

    static public void clearLog() {
        if (logAre != null) {
            try {
                logAre.getDocument().remove(0, logAre.getDocument().getLength());
            } catch (BadLocationException e) {
                logger.error(e);
            }
        }
    }

    public static synchronized void logit(String msg) {
        logit(msg, true);
    }

    public static synchronized void logit(String msg, boolean logToFile) {
        if (logToFile) logger.info(msg);
        logBuffer.push(MiscDate.todaysDate() + ">   " + msg);
    }

    public static void MessageBox(final String string) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, string);
            }
        });
    }


    private static void setIndeterminate(boolean b) {
        if (Busy == null) return;
        Busy.setVisible(b);
        Busy.setEnabled(b);
        Busy.setBusy(b);
    }

    public static boolean isQuitApp() {
        return quitApp;
    }

    public static void executeInBack(Runnable r) throws InterruptedException {
        executeInBack(r, true);
    }

    public static void executeInBack(Runnable r, boolean isManaged) throws InterruptedException {

        if (isManaged) {
            if (executor == null) {
                executor = new PooledExecutor();
                executor.setMinimumPoolSize(5);
                executor.waitWhenBlocked();
//                executor.runWhenBlocked();
                executor.setKeepAliveTime(1000); //1min
            }
            setStopProcess(false);
            executor.execute(new RunCell(r, isManaged));
            startAProcess();
        } else {
            if (sysExecutor == null) {
                sysExecutor = new PooledExecutor();
                sysExecutor.setMinimumPoolSize(3);
                sysExecutor.waitWhenBlocked();
//                executor.runWhenBlocked();
                sysExecutor.setKeepAliveTime(1000);
            }
            setStopProcess(false);
            sysExecutor.execute(new RunCell(r, isManaged));
        }
    }

    public static void addExecutors(int n) {
        if (executor != null) {
            logit("Increase threads to " + n);
            executor.createThreads(n);
        }
    }

    public static void Exit() {
        System.exit(0);
    }

    public static void Exit(Exception e) {
        e.printStackTrace();
        MessageBox("Fatal Error!\n" + e.getMessage());
        System.exit(7);
    }

    private static synchronized void startAProcess() {
        if (thredWatcher == null) {
            thredWatcher = new processWatcher();
        }
        if (!thredWatcher.waiting) {
            Thread thread = new Thread(thredWatcher);
            thread.start();
        }
    }

//    public static void Shutdown() {
//        if (dialog == null) {
//            dialog = new Shutdown();
//            dialog.setModal(false);
//            dialog.setAlwaysOnTop(true);
//        }else{
//            dialog.startup();
//        }
//        dialog.setShutdown();
//        dialog.pack();
//        dialog.setVisible(true);
//    }

    public static void registerBeQuit(BeQuit bg) {
        beQuits.add(bg);
    }


    public static void debugLogit(String msg) {
        if(!debug) return;
        if(debugFile == null){
            String debugDir = getCurrentFolder() + "\\debug" ;
            new File(debugDir).mkdirs();
            debugFile = debugDir + "\\Debug_"+ MyDate.getToday().getDate() + ".txt";
        }
        try {
            Utilities.String2File(msg + "\r\n", debugFile, true );
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public static void debugLogit(Exception er) {
        if(!debug) return;
        StackTraceElement[] msgs = er.getStackTrace();
        for (StackTraceElement st : msgs) {
            debugLogit(st.toString());
        }
    }

    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = AppManager.class.getResource(path);
        if (imgURL == null) {
            imgURL = AppManager.class.getResource("/resources/record.gif");
        }
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            logit("Couldn't find file: " + path);
            return null;
        }
    }

    public Database getDatabase(Class caller) {
        if (getTargetDbProp() != null)
            return Database.getInstance(getTargetDbProp(), caller);
        return null;
    }

    public String getDbHost() {
        return getTargetDbProp().getProperty("host");
    }

    public String getDbPort() {
        return getTargetDbProp().getProperty("port");
    }

    public static boolean canShutdown() {
        //排除编程失误,误操作等造成的关机. 下述机器不得关闭
        return !("DellE5".equalsIgnoreCase(getHostName()) ||
                "DELL690".equalsIgnoreCase(getHostName()));
    }

    public boolean canRemoteShutdown() {
        if (cfg == null) return false;
        return cfg.getBooleanVal("RemoteShutdown", false);
    }

    public void setRemoteShutdown(boolean b) {
        if (cfg == null) return;
        cfg.setProperty("RemoteShutdown", b ? "true" : "false");
        cfg.save();
    }

    /**
     * 仅适用于大量同步频繁短暂的数据库操作
     * <p>
     * 持续长时间间断的数据库操作不要使用本功能,以免降低效率
     *
     //     * @param requestor
     * @return
     */
//    public Database getDb(Class requestor) {
//        return getDb(requestor, 30);
//    }
//
//    public Database getDb(Class requestor, int timeoutMms) {
//        return DbStack.getDb(requestor);
//    }
//
//    public void closeDB(Database db) {
//        DbStack.closeDB(db);
//    }

    public static void setAutoSutdown(final boolean d) {
        autoShutdown = d;
    }

    public static Boolean isAutoShutdown() {
        return autoShutdown;
    }

    private DBProperties getTargetDbProp() {
        if (targetDbProp == null) {
            try {
                targetDbProp = new DBProperties(getCurrentDBCfg());
            } catch (DBException e) {
                targetDbProp = null;
                logger.error(e);
            }
        }
        return targetDbProp;
    }

    public void resetTargetDbProp() {
        targetDbProp = null;
    }

    public String getCurrentDBCfg() {
        return SharedCfgProvider.getInstance("MySql").getDir() + "\\MySQL.ini";
    }

    public boolean tryOpendb(Database db) {
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

    public void setLog4J() {
        if (cfg == null) {
            loadConfig();
        }
        DawLogger.useLog4J(getConfigFolder() + "/logger.xml");
        logger = DawLogger.getLogger(AppManager.class);
        logger.setLevel(cfg.getIntVal("Logger-level", 5));
        logger.info("================ 启动 ===================");
        logger.info(this.getClass().getName() + " 启动");
        logit("Use log4J");
    }

    public void loadConfig() {
        String cName = this.getClass().getSimpleName();
        this.appName = cName;

        this.file = configFolder + "/" + appClass.getSimpleName() + ".ini";

        System.out.println(new File(".").getAbsolutePath());
        System.out.println(new File(file).getAbsolutePath());

        cfg = new ConfigF();
        cfg.load(this.file);
        prepareIDs();
    }

    public void loadConfig(String appName) {
        this.appName = appName;

//        String cName = this.getClass().getSimpleName();
        this.file = configFolder + "/" + appClass.getSimpleName() + ".ini";
        cfg = new ConfigF();
        cfg.load(this.file);

        configFolder = cfg.getConfigFolder();

        this.file = cfg.getConfigUrl();
        prepareIDs();
        System.out.println(new File(".").getAbsolutePath());
        System.out.println(new File(file).getAbsolutePath());
    }

    public void save() {
        cfg.save();
        logit("Aplication Status saved.");
    }

    public JFrame getMainFram() {
        return this.frame;
    }

    public void setIcoUrl(String icoUrl) {
        this.icoUrl = icoUrl;
    }

    public void setMainFram(JFrame frame) {
        this.frame = frame;
        if (icoUrl != null) {
            URL url =  getClass().getResource(".");
            String path = url.getPath();
            ImageIcon ico = new ImageIcon(getClass().getResource(icoUrl));
            frame.setIconImage(ico.getImage());
        }
    }

    public boolean isAppLocked() {
        return locked;
    }

    public void LockApp(boolean Lock) {
        this.locked = Lock;
    }

    public File dirChoise(String title, File currentDir) {
        String fileName = null;
        JFileChooser fc = new JFileChooser(currentDir);
        fc.setDialogTitle(title);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = fc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            if (selectedFile.exists()) {
                return selectedFile;
            }
        }
        return null;
    }

    public String getRestoreFolder() {
        return cfg.getProperty("RestoreFolder");
    }

    public void setRestoreFolder(String restoreFolder) {
        cfg.setProperty("RestoreFolder", restoreFolder);
        cfg.save();
    }

    public String getBackupFolder() {
        return cfg.getProperty("BackupFolder");
    }

    public void setBackupFolder(String dir) {
        cfg.setProperty("BackupFolder", dir);
    }

    public String getTHS_UserFolder() {
        String currentDir = cfg.getProperty("THSFolder", "");
        return Utilities.slashify(currentDir) + getTHS_User() + "/";
    }

    public String getDropBoxFolder() {
        File DropboxFolder = new File(cfg.getProperty("DropboxFolder", "D:/Dropbox/Bag.STOCK"));
        try {
            if (!DropboxFolder.exists()) {
                DropboxFolder = dirChoise("Dropbox Folder? ", DropboxFolder);
                if (DropboxFolder.exists()) {
                    cfg.setProperty("DropboxFolder", DropboxFolder.getCanonicalPath());
                }
            }
            return Utilities.slashify(DropboxFolder.getCanonicalPath());
        } catch (IOException ignored) {

        }
        return null;
    }

    public String getTHSFolder() {
        String currentDir = cfg.getProperty("THSFolder", "");
        return Utilities.slashify(currentDir);
    }

    public void setTHSFolder(String dir) {
        cfg.setProperty("THSFolder", dir);
    }

    public String getTHS_User() {
        return cfg.getProperty("THS_User", "szhzz");
    }

    public void setTHS_User(String uName) {
        cfg.setProperty("THS_User", uName);
    }

    public String getF10File() {
        return cfg.getProperty("F10File");
    }

    public void setF10File(String file) {
        cfg.setProperty("F10File", file);
    }

    public void setEnv(String name, String val) {
        cfg.setProperty(name, val);
    }

    public String getEnv(String name) {
        return cfg.getProperty(name);
    }

    public Config getCfg() {
        return cfg;
    }

    public Config getPreferCfg(Class aClass) {
        return CfgProvider.getInstance("Windows").getCfg(aClass.getSimpleName());
    }

    public void print(String msg) {
        logger.info(msg);
        if (logAre != null) {
            try {
                logAre.getDocument().insertString(logAre.getDocument().getLength(), msg, null);
                logAre.setCaretPosition(logAre.getDocument().getLength());
            } catch (BadLocationException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    public void setDigitalClock(JLabel digitalClock) {
        this.DigitalClock = digitalClock;
        javax.swing.Timer t = new javax.swing.Timer(1000, new ClockListener());
        t.start();
    }

    public void setStatuBar(JLabel StatuBar) {
        this.StatuBar = StatuBar;
        resetStatuText();
    }

    public void setStatuText(String msg) {
        setStatuText(msg, true);
    }

    public void setStatuText(String msg, boolean echo) {
        if (StatuBar != null) {
            StatuBar.setText(msg);
        } else {
            if (echo) System.out.println(msg);
        }
    }

    public void resetStatuText() {
        if (StatuBar != null) StatuBar.setText("Ready");
    }

    public JTextComponent getMessageBox() {
        return logAre;
    }

    public void setMessageBox(JTextComponent logAre) {
        this.logAre = logAre;
    }

    public void addProgressBar(JProgressBar progressBar) {
        if (progressBar != null)
            progress.add(progressBar);
    }

    public void removeProgressBar(JProgressBar progressBar) {
        if (progressBar != null)
            progress.remove(progressBar);
    }

    public void setProgress(int value, int min, int max) {
        for (JProgressBar progressBar : progress) {
            progressBar.setMinimum(min);
            progressBar.setMaximum(max);
            progressBar.setValue(value);
            progressBar.setVisible(value > 0);
        }
    }

    public void setMemoBar(JProgressBar progressBarMemo) {
        if (memoBar == null) {
            if (progressBarMemo != null)
                memoBar = new MemoBar(progressBarMemo);
        } else {
            memoBar.stop();
        }
    }

    public boolean isMidleClose() {
        return midleClose;
    }

    public void setMidleClose(boolean midleClose) {
        this.midleClose = midleClose;
    }

    public void setThreadLabel(JLabel threadLabel) {
        this.threadLabel = threadLabel;
    }

    public Object addAlarmClock(int hour, int minute, int seconds, Runnable requestor, boolean loop, int priority) {
        if (alarmClock == null) {
            alarmClock = AlarmClock.getInstance();
            alarmClock.setApp(this);
        }

        return alarmClock.setAlarm(hour, minute, seconds, requestor, loop, priority);
    }

    public Object addAlarmClock(int hour, int minute, int seconds, int millis, Runnable requestor, boolean loop, int priorol) {
        if (alarmClock == null) {
            alarmClock = AlarmClock.getInstance();
            alarmClock.setApp(this);
        }

        return alarmClock.setAlarm(hour, minute, seconds, millis, requestor, loop, priorol);
    }

    public void suspendAllAlarmClock(boolean suspend) {
        if (alarmClock != null) {
            alarmClock.suspendAll(suspend);
        }
    }

    public Object addAlarmClock(TimerEvent event) {
        if (alarmClock == null) {
            alarmClock = AlarmClock.getInstance();
            alarmClock.setApp(this);
        }
        return alarmClock.setAlarm(event);
    }

    public Object addAlarmClock(MyDate timer, Runnable requestor, boolean loop, int priority) {
        try {
            if (alarmClock == null) {
                alarmClock = AlarmClock.getInstance();
                alarmClock.setApp(this);
            }
            return alarmClock.setAlarm(timer, requestor, loop, priority);

        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    public Object addAlarmClock(int hour, int minits, Runnable requestor, boolean loop, int priorol) {
        return addAlarmClock(hour, minits, 0, requestor, loop, priorol);
    }


    public void removeAllAlarms() {
        alarmClock.removeAllAlarms();
    }

    public void setFreeTimeJob(Runnable requestor) {
        if (alarmClock == null) {
            alarmClock = AlarmClock.getInstance();
            alarmClock.setApp(this);
        }
        alarmClock.setFreeTimeJob(requestor);
    }

    public void removeAlarmClock(Object o) {
        if (alarmClock != null) {
            alarmClock.removeAlarm(o);
        }
    }

    private AppManager Parent() {
        return this;
    }

    public JXBusyLabel getBusy() {
        return Busy;
    }

    public void setBusy(JXBusyLabel busy) {
        Busy = busy;
    }

//    private class EventLoop implements Runnable {
//        MessageCode messageID;
//        Object caller;
//        Object message;
//
//        void setMessage(MessageCode messageID, Object caller, Object message) {
//            this.messageID = messageID;
//            this.caller = caller;
//            this.message = message;
//        }
//
//        public void run() {
//            for (MessageAbstract o : messageObject) {
//                o.acceptMessage(messageID, caller, message);
//            }
//        }
//    }

    public boolean Quit() {
        quitApp = true;
        if (!isDebug()) {
            try {
                sendMailNow("程序退出", null);
            }catch (Exception e){

            }
            logger.info("程序退出...");
        }

        try {
            Collections.sort(beQuits);
            for (int i = 0; i < beQuits.size(); i++) {
                BeQuit bq = beQuits.get(i);
                logit((beQuits.size()-i) + "\t" + bq.getClass().getName());
            }
            logit(" End of BeQuits list //////////////////////////////////");
            for (int i = 0; i < beQuits.size(); i++) {
                BeQuit bq = beQuits.get(i);
                logit((beQuits.size()-i) + "\t" + bq.getClass().getName());
                bq.Quit();
            }
            logger.info("BeQuit 程序退出 " + beQuits.size() + " beQuits ...");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (executor != null) {

            executor.shutdownAfterProcessingCurrentlyQueuedTasks();
        }
        if (sysExecutor != null) {
            sysExecutor.shutdownAfterProcessingCurrentlyQueuedTasks();
        }
        if (executor != null) {
            logger.info("程序退出 " + executor.getPoolSize() + " executor ...");
            try {
                executor.awaitTerminationAfterShutdown(1000);
            } catch (InterruptedException ignored) {

            }
        }
        if (sysExecutor != null) {
            logger.info("程序退出 " + sysExecutor.getPoolSize() + " sysExecutor ...");
            try {
                //ExecutorService ep = newFixedThreadPool(5);
                sysExecutor.awaitTerminationAfterShutdown(1000);
            } catch (InterruptedException ignored) {

            }
        }
        //最后才可以关闭db
        logger.info("程序退出 " + Database.getConnectCount() + " Database.Quit() ...");
        Database.Quit();
        logger.info("程序正常退出");
        System.out.println("程序正常退出");
        return true;
    }

    public boolean forceQuit() {
        quitApp = true;

        for (BeQuit bq : beQuits) {
            bq.Quit();
        }
        if (executor != null) {
            executor.shutdownNow();
        }
        if (sysExecutor != null) {
            sysExecutor.shutdownNow();
        }
        if (executor != null) {
            try {
                executor.awaitTerminationAfterShutdown(1000);
            } catch (InterruptedException ignored) {

            }
        }
        if (sysExecutor != null) {
            try {
                sysExecutor.awaitTerminationAfterShutdown(1000);
            } catch (InterruptedException ignored) {

            }
        }
        //最后才可以关闭db
        Database.Quit();
        return true;
    }

    public static String getCpuID() {
        if (cpuID == null) {
            cpuID = HardwareIDs.getCPUSerial();
        }
        return cpuID;
    }

    public static String getHostName() {
        if (hostName != null) return hostName;

        String result = null;
        try {
            result = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignored) {

        }
        if ( result == null || "".equals(result)) {
            result = System.getenv("COMPUTERNAME");
        }
        if (result == null || "".equals(result)) {
            result = System.getenv("HOSTNAME");
        }
        if (result == null || "".equals(result)) {
            result = getCpuID();
        }
        if ("".equals(result)) {
            result = null;
        }

        hostName = result.toUpperCase();
        return hostName;
    }

    public String getTelNo() {
        if (telNo == null) {
            telNo = cfg.getProperty("TelNo", "13902911318");
        }
        return telNo;
    }

    public String getAppName() {
        if (appName == null) {
            return cfg.getProperty("AppName", "Tiger-STS");
        }
        return appName;
    }

    public String getVersion() {
        return cfg.getProperty("Version", "1.0");
    }

    public String getMac() {
        if (macID == null) {
            macID = HardwareIDs.getMACAddress("Endpoint VPN Client");
        }
        if (macID == null) {
            macID = cfg.getProperty("LOCALMAC", "54-0A-66-A1-D3-74");
        }
        return macID;
    }

    public String getIP(String signature) {
        localIP = HardwareIDs.getIP(signature);
        if (localIP == null) {
            localIP = cfg.getProperty("LOCALIP", "183.62.98.27");
        }
        return localIP;
    }

    public String getHD_ID() {
        if (HD_ID == null) {
            HD_ID = DiskUtils.getSerialNumber("C");
        }
        return HD_ID;
    }

    public void clearEventLog() {
        if (eventLogAre != null) {
            try {
                eventLogAre.getDocument().remove(0, eventLogAre.getDocument().getLength());
            } catch (BadLocationException e) {
                logger.error(e);
            }
        }
    }

    public static synchronized void logEvent(String msg) {
        logger.info(msg);
        eventLoger.push(MiscDate.todaysDate() + ">   " + msg);
    }

    public void setEventLogAre(JTextComponent tradeInforAre) {
        this.eventLogAre = tradeInforAre;
        if (eventBuffer.length() > 0) {
            eventLogAre.setText(eventBuffer.toString());
            eventBuffer = new StringBuffer();
        }
    }

    @Override
    public long in(Object obj) {
        String msg = obj.toString();
        if (logAre != null) {
            try {
                boolean autoCaretPosition = (logAre.getCaretPosition() == logAre.getDocument().getLength());
                if (logAre.getDocument().getLength() > 47000) {
                    logAre.getDocument().remove(0, logAre.getDocument().getLength() / 2);
                }
                logAre.getDocument().insertString(logAre.getDocument().getLength(), msg + "\n", null);//msg.replace("\n", ", ")
                if (autoCaretPosition) {
                    logAre.setCaretPosition(logAre.getDocument().getLength());
                }
            } catch (BadLocationException e) {
                logger.error(e.getMessage());
            }
        }
        return 0;
    }

    @Override
    public long in(long dataID, Object obj) {
        return 0;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setDebug(String[] args) {
        for (String s : args) {
            if (s.equals("debug")) {
                debug = true;
                break;
            }
        }
    }


    private static class processWatcher implements Runnable {
        boolean waiting = false;

        public void run() {
            if (waiting) return;
            waiting = true;

            synchronized (lock) {
//                MessageAbstract.getInstance().sendMessage(MessageCode.InBackProcessStart, autoShutdown);
                try {
//                    if (stopButton != null) {
//                        stopButton.setEnabled(true);
//                    }
                    setIndeterminate(true);
                    if (executor != null)
                        while ((executor.getPoolSize()) > 0) {
                            if (threadLabel != null) {
                                threadLabel.setText("T=" + (executor == null ? "0" : executor.getPoolSize()) +
                                        (sysExecutor == null ? 0 : " S=" + sysExecutor.getPoolSize()));
                            }
                            lock.wait(500);
                        }
                } catch (InterruptedException e) {
                    logger.error(e);
                } finally {
                    waiting = false;
                    setIndeterminate(false);
//                    if (stopButton != null) {
//                        stopButton.setEnabled(false);
//                    }
                    if (stopProcess) {
                        logit("User suspand progress.");
                    }
//                    MessageAbstract.getInstance().sendMessage(MessageCode.InBackProcessStop, autoShutdown);
                    setStopProcess(false);
//                    if (isAutoShutdown()) {
//                        Shutdown();
//                    }
                }
            }
        }
    }

    private class MemoBar {
        private final long mb = 1024 * 1024;
        JProgressBar progressBarMemo = null;
        Timer countDown = null;
        boolean running = false;
        int interval = 500;
        private transient Runtime runtime = Runtime.getRuntime();

        public MemoBar(JProgressBar progressBarMemo) {
            this.progressBarMemo = progressBarMemo;
            progressBarMemo.setMinimum(0);
            start();
        }

        public void stop() {
            this.running = false;
        }

        public void start() {
            if (countDown != null) {
                countDown.stop();
            }
            if (this.running) return;

            countDown = new Timer(interval, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int thisTotalMemory = (int) (runtime.totalMemory() / mb);
                    int thisUsedMemory = (int) (thisTotalMemory - runtime.freeMemory() / mb);

                    progressBarMemo.setMaximum(thisTotalMemory);
                    progressBarMemo.setValue((thisUsedMemory));
                    progressBarMemo.setString(thisUsedMemory + "M of " + thisTotalMemory + "M");
                    if (autoGc && thisUsedMemory > thisTotalMemory * 0.9) {
                        System.gc();
                    }

                    if (threadLabel != null && (thredWatcher == null || !thredWatcher.waiting)) {
                        threadLabel.setText("T=" + (executor == null ? "0" : executor.getPoolSize()) +
                                (sysExecutor == null ? 0 : " S=" + sysExecutor.getPoolSize()));
                    }

                    if (running) countDown.start();
                }
            });

            countDown.start();
        }
    }

    class ClockListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //... Whenever this is called, get the current time and
            //    display it in the textfield.
            Calendar now = Calendar.getInstance();
            int h = now.get(Calendar.HOUR_OF_DAY);
            int m = now.get(Calendar.MINUTE);
            int s = now.get(Calendar.SECOND);
            DigitalClock.setText("" + h + ":" + m + ":" + s);
        }
    }

    class EventLoger implements DataConsumer {
        private ObjBufferedIO buffer = null;


        EventLoger() {
            buffer = new ObjBufferedIO();
            try {
                buffer.setReader(this, 10);           //256
            } catch (InterruptedException e) {
                logger.equals(e);
            }
        }

        public void push(String msg) {
            buffer.push(msg);
        }

        @Override
        public long in(Object obj) {
            String msg = obj.toString();
            if (eventLogAre != null) {
                try {
                    if (eventLogAre.getDocument().getLength() > 47000) {
                        eventLogAre.getDocument().remove(0, eventLogAre.getDocument().getLength() / 2);
                    }
                    eventLogAre.getDocument().insertString(eventLogAre.getDocument().getLength(), msg + "\n", null);//msg.replace("\n", ", ")
                    eventLogAre.setCaretPosition(eventLogAre.getDocument().getLength());
                } catch (BadLocationException e) {
                    logger.error(e.getMessage());
                }
            } else {
                logit(msg);
            }
            return 0;
        }

        @Override
        public long in(long dataID, Object obj) {
            return 0;
        }
    }

    public void sendMailNow(String msg, String attFile) {
        if (!isOpenMailBox()) return;

        if (mailer == null) {
            mailer = new Mailer();
            mailer.setBuffer(10);
            mailMsg = new MailMsg();
        }
        mailer.sendMailNow(mailMsg.copy(msg, attFile));
    }

    public void sendMail(String msg, String attFile) {
        if (!isOpenMailBox()) return;

        if (mailer == null) {
            mailer = new Mailer();
            mailer.setBuffer(10);
            mailMsg = new MailMsg();
        }
        mailer.sendMail(mailMsg.copy(msg, attFile));
    }

    public void sendBatchMail(String title, String msg) {
        if (!isOpenMailBox()) return;

        if (mailer == null) {
            mailer = new Mailer();
            mailer.setBuffer(10);
            mailMsg = new MailMsg();
        }
        mailer.sendBatchMail(title, msg);
    }

    public boolean isKeepConnectToBroker() {
        return true;
    }

    public boolean isOnTrade() {
        return true;
    }

    public static boolean isLocalIP(String[] ips) {
        if (localIPs == null) {
            localIPs = new HashSet<>();
            Enumeration<NetworkInterface> en = null;
            Enumeration<InetAddress> addresses;

            try {
                en = NetworkInterface.getNetworkInterfaces();
                while (en.hasMoreElements()) {
                    NetworkInterface networkinterface = en.nextElement();
//                    System.out.println(networkinterface.getName());
                    addresses = networkinterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        localIPs.add(addresses.nextElement().getHostAddress());
//                    System.out.println("\t"
//                            + addresses.nextElement().getHostAddress() + "");
                    }
                }
            } catch (SocketException e) {
                logger.error(e);
            }
        }

        String hostName = getHostName();
        for(String ip : ips) {
            if(hostName.equalsIgnoreCase(ip))return true;

            if (localIPs.contains(ip)) return true;
        }
        return false;
    }

    public static boolean isLocalIP(String ip) {
        return isLocalIP(new String[]{ip});
    }

    public boolean isOpenMailBox() {
        if (openEmail == null) {
            openEmail = (cfg != null && cfg.getBooleanVal("SendMail", false));
        }
        return openEmail;
    }

    public void setOpenMail(boolean open) {
        openEmail = open;
        if (cfg != null) {
            cfg.setProperty("SendMail", openEmail);
            cfg.save();
        }

    }

    public static Class getAppClass(){
        return appClass;
    }
    public static void setAppClass(Class c){
        appClass = c;
        CfgProvider.setAppClass(c);
        configFolder = CfgProvider.getRootFolder();
    }

    public static boolean isRunning(String processName, String title) {
        BufferedReader bufferedReader = null;
        Process proc = null;


        try {
            if (processName != null) {
                proc = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq " + processName + "\"");
            } else if (title != null) {
                title = title.replace("*", "");
                proc = Runtime.getRuntime().exec("tasklist /FI \"WINDOWTITLE eq " + title + "*\"");
            } else {
                return false;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            int listCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (processName != null && line.contains(processName)) //判断是否存在
                {
                    return true;
                } else if (title != null && line.contains("=====")) //判断是否存在
                {
                    listCount = 1;
                    return true;
                } else if (line.contains("没有运行的任务匹配指定标准")) {
                    return false;
                }
            }
            return false;
        } catch (Exception ex) {
            logger.equals(ex);
            return false;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    public static boolean taskkill(String processName, String title) {
        BufferedReader bufferedReader = null;
        Process proc = null;

        try {
            if (processName != null) {
                proc = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq " + processName + "\"");
            } else if (title != null) {
                title = title.replace("*", "");
                proc = Runtime.getRuntime().exec("tasklist /FI \"WINDOWTITLE eq " + title + "*\"");
            } else {
                return false;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            int listCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (processName != null && line.contains(processName)) //判断是否存在
                {
                    Runtime.getRuntime().exec( "Taskkill /IM "+processName);//关闭的是对应程序的线程。
                    return true;
                } else if (title != null && line.contains("=====")) //判断是否存在
                {
                    listCount = 1;
                    return true;
                } else if (line.contains("没有运行的任务匹配指定标准")) {
                    return false;
                }
            }
            return false;
        } catch (Exception ex) {
            logger.equals(ex);
            return false;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception ex) {
                }
            }
        }
    }


}
