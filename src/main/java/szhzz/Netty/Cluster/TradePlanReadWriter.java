package szhzz.Netty.Cluster;

import szhzz.App.AppManager;
import szhzz.Calendar.MyDate;
import szhzz.Config.CfgProvider;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Netty.Cluster.ExchangeDataType.TradePlanWrap;
import szhzz.Netty.Cluster.ExchangeDataType.TxtFileWrap;
import szhzz.Timer.CircleTimer;

import java.io.File;
import java.util.HashSet;

/**
 * Created by HuangFang on 2015/4/5.
 * 12:43
 */
public class TradePlanReadWriter extends CfgProvider {
    private static AppManager App = AppManager.getApp();
    private static TradePlanReadWriter onlyOne = null;
    private HashSet<NettyExchangeData> files = new HashSet<>();
    private String folder = null;

    private TradePlanReadWriter() {
    }

    public static CfgProvider getInstance(String groupName){
        return getInstance();
    }
    public static TradePlanReadWriter getInstance() {
        if (onlyOne == null) {
            onlyOne = new TradePlanReadWriter();
        }
        return onlyOne;
    }

    public void reloadCfgs() {
        laodCfgs(null);
    }


    public synchronized void addData(NettyExchangeData data) {
//        if (szhzz.App.isOnTrade() || szhzz.App.isDebug()) return;

        files.add(data);
        delayTimer.setCircleTime(10 * 1000);
    }


    void deleteFiles() {
        File dir = new File(getSavePath());
//        szhzz.App.logEvent("delete files in " + dir);

        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.isDirectory()) {
                    f.delete();
                }
            }
        }
        dir = new File(getBackupPath());
//        szhzz.App.logEvent("delete files in " + dir);
        files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.isDirectory()) {
                    f.delete();
                }
            }
        }

    }

    public String getDir() {
        return getSavePath();
    }

    public String getSavePath() {
        if (folder == null) {
            folder = App.getCfg().getProperty("TradePlanFolder",
                    AppManager.getCurrentDisk() + "\\JNIProject\\JavaProj\\configs\\TradePlan\\");
        }
        return folder;
    }

    public String getBackupPath() {
        String backupPath = getSavePath();

        MyDate dateSuffix = new MyDate(MyDate.getLastClosedDay().getDate());
        dateSuffix.futureOpenDay();

        backupPath += "Plan" + dateSuffix.getDate().replace("-", "") + "\\";
        new File(backupPath).mkdirs();

        return backupPath;
    }



    public HashSet<NettyExchangeData> getUpdateFiles() {
        HashSet<NettyExchangeData> updateFiles = new HashSet<>();
        File dir = new File(getSavePath());
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.isDirectory()) {
                    NettyExchangeData eData = TradePlanWrap.getTextFile(f.getAbsolutePath());
                    updateFiles.add(eData);
                }
            }
        }
        return updateFiles;
    }


    CircleTimer delayTimer = new CircleTimer(){

        @Override
        public synchronized void execTask() {
            if (files == null || files.size() == 0) return;

            deleteFiles();
            try {
                this.wait(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String dir = getSavePath();
            for (NettyExchangeData d : files) {
                new TxtFileWrap(d).writeToTextFile(dir);
            }

            dir = getBackupPath();
            for (NettyExchangeData d : files) {
                new TxtFileWrap(d).writeToTextFile(dir);
            }

            files.clear();
        }
    };
}
