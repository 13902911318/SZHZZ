package szhzz.App;

//import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
//import java.util.concurrent.*;


import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import szhzz.Calendar.MiscDate;
import szhzz.Config.CfgProvider;
import szhzz.Config.Config;
import szhzz.Config.ConfigF;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
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
//    static private JXBusyLabel Busy = null;
//    static private AlarmClock alarmClock = null;
    private static boolean stopProcess = true;
    private static String hostName = null;
    private static JLabel threadLabel = null;
    protected Config cfg = new ConfigF();
     Vector<JProgressBar> progress = new Vector<JProgressBar>();
    String cfgFile = "";
    JFrame frame = null;
    MemoBar memoBar = null;
    private JTextComponent eventLogAre = null;
    private StringBuffer eventBuffer = new StringBuffer();
    private boolean locked = false;
    private JLabel StatuBar = null;
    private JLabel DigitalClock = null;
    private static String cpuID = null;
    private String telNo = null;
    private String macID = null;
    private String HD_ID = null;
    private String localIP = null;
    private String appName = null;
//    private DBProperties targetDbProp = null;
    private String icoUrl = null;
    private static ObjBufferedIO logBuffer = null;
    private static EventLoger eventLoger;

    //    private static Shutdown dialog = null;
    private boolean debug = false;
    private boolean autoGc = false;

    private static Mailer mailer = null;
    private MailMsg mailMsg = null;
    private static HashSet<String> localIPs = null;
    private static long systemTimeDiff = 0L;
    private static String currentDisk = null;
    private static Class appClass = null;

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

//    public static void reshape(JXBusyLabel Busy, float height) {
//        float barLength = (height) * 8 / 26;
//        Shape trajectory = null;
//        trajectory = new Ellipse2D.Float(barLength / 2, barLength / 2, height - barLength, height - barLength);
//
//        Shape pointShape = new Ellipse2D.Float(0, 0, barLength, barLength * 0.318f);
//
//        BusyPainter bp = new BusyPainter(pointShape, trajectory);
//
//        BusyPainter old = Busy.getBusyPainter();
//        bp.setTrailLength(old.getTrailLength());
//        bp.setPoints(old.getPoints());
//        bp.setFrame(old.getFrame());
//        Busy.setPreferredSize(new Dimension((int) (height), (int) (height)));
//        Busy.setIcon(new EmptyIcon((int) (height), (int) (height)));
//        Busy.setBusyPainter(bp);
//        bp.setHighlightColor(old.getHighlightColor());
//        bp.setBaseColor(old.getBaseColor());
//        Busy.repaint();
//    }


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
//        if (Busy == null) return;
//        Busy.setVisible(b);
//        Busy.setEnabled(b);
//        Busy.setBusy(b);
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
//            executor.execute(new RunCell(r, isManaged));
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
//            sysExecutor.execute(new RunCell(r, isManaged));
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
        try {
            Utilities.String2File(msg, "g:/debug.txt", true);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void debugLogit(Exception er) {
        try {
            StackTraceElement[] msgs = er.getStackTrace();
            for (StackTraceElement st : msgs) {
                Utilities.String2File(st.toString(), "g:/debug.txt", true);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
            logit("Couldn't find cfgFile: " + path);
            return null;
        }
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




//    public boolean tryOpendb(Database db) {
//        boolean isOpened = false;
//        if (db == null)
//            return false;
//        try {
//            isOpened = db.openDB();
//        } catch (Exception e) {
//            logger.error(e);
//        }
//        return isOpened;
//    }

    public void setLog4J() {
        if (cfg == null) {
            loadConfig();
        }
        DawLogger.useLog4J(CfgProvider.getRootFolder() + "/logger.xml");
        logger = DawLogger.getLogger(AppManager.class);
        logger.setLevel(cfg.getIntVal("Logger-level", 5));
        logger.info("================ 启动 ===================");
        logger.info(this.getClass().getName() + " 启动");
        logit("Use log4J");
    }

    public void loadConfig() {
        String cName = this.getClass().getSimpleName();
        this.appName = cName;
        loadConfig(this.appName);
        prepareIDs();
    }

    public void loadConfig(String appName) {
        this.appName = appName;

        String cName = this.getClass().getSimpleName();
        cfg = CfgProvider.getInstance(appName).getCfg(appName);
        this.cfgFile = cfg.getConfigUrl();
        prepareIDs();
        System.out.println(new File(".").getAbsolutePath());
        System.out.println(new File(cfgFile).getAbsolutePath());
    }

    public void saveCfg() {
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

    public Config getCfg() {
        return cfg;
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


    public void setLogAre(JTextComponent logAre) {
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

    public void setThreadLabel(JLabel threadLabel) {
        this.threadLabel = threadLabel;
    }


    private AppManager Parent() {
        return this;
    }

    public boolean Quit() {
        quitApp = true;
        if (!isDebug()) {
            sendMailNow("程序退出", null);
            logger.info("程序退出...");
        }

        try {
            Collections.sort(beQuits);
            for (int i = 0; i < beQuits.size(); i++) {
                BeQuit bq = beQuits.get(i);
                System.out.println(bq.getClass().getName());
                bq.Quit();
            }
            logger.info("程序退出 " + beQuits.size() + " beQuits ...");
        } catch (Exception e) {

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
//        logger.info("程序退出 " + Database.getConnectCount() + " Database.Quit() ...");
//        Database.Quit();
        logger.info("程序正常退出");
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
//        Database.Quit();
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
            telNo = cfg.getProperty("TelNo", "");
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
            localIP = cfg.getProperty("LOCALIP", "127.0.0.1");
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

    public synchronized void logEvent(String msg) {
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

    public boolean isAutoGc() {
        return autoGc;
    }

    public void setAutoGc(boolean autoGc) {
        this.autoGc = autoGc;
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
        if (mailer == null) {
            mailer = new Mailer();
            mailer.setBuffer(10);
            mailMsg = new MailMsg();
        }
        mailer.sendMailNow(mailMsg.copy(msg, attFile));
    }


    public void sendBatchMail(String title, String msg) {
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

    public static Class getAppClass(){
        return appClass;
    }
    public static void setAppClass(Class c){
        appClass = c;
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
}
