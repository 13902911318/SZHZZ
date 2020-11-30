package szhzz.Netty.Cluster;

import szhzz.App.AppManager;
import szhzz.Calendar.MyDate;
import szhzz.Config.SharedCfgProvider;
import szhzz.Netty.Cluster.ExchangeDataType.ClusterProtocal;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Netty.Cluster.ExchangeDataType.TradePlanWrap;
import szhzz.Netty.Cluster.ExchangeDataType.TxtFileWrap;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;

import java.io.File;
import java.util.HashSet;

/**
 * Created by HuangFang on 2015/4/5.
 * 12:43
 */
public class TradePlanReadWriter extends SharedCfgProvider {
    private static DawLogger logger = DawLogger.getLogger(TradePlanReadWriter.class);

    private static AppManager App = AppManager.getApp();
    private static TradePlanReadWriter onlyOne = null;
    private HashSet<NettyExchangeData> files = new HashSet<>();
    private HashSet<NettyExchangeData> amAuctionFiles = new HashSet<>();
    private boolean onWriting = false;

    public static TradePlanReadWriter getInstance() {
        if (onlyOne == null) {
            onlyOne = new TradePlanReadWriter();
            onlyOne.groupName = "TradePlan";
        }
        return onlyOne;
    }

    public void addData(NettyExchangeData data) {
        if (App.isOnTrade() || App.isDebug()) return;

        files.add(data);
        delayTimer.setCircleTime(1000);
    }

    public void addAuctionData(NettyExchangeData data) {
        if (App.isOnTrade() || App.isDebug()) return;

        amAuctionFiles.add(data);
        delayTimer.setCircleTime(1000);
    }

    private void deleteFiles() {
        File dir = new File(getSavePath());
        App.logEvent("delete files in " + dir);

        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.isDirectory()) {
                    f.delete();
                }
            }
        }
    }

    private void deleteBackupFiles() {
        File dir = new File(getBackupPath());
        App.logEvent("delete files in " + dir);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.isDirectory()) {
                    f.delete();
                }
            }
        }
    }

    public String getSavePath() {
        return getDir();
    }

    public String getBackupPath() {
        MyDate dateSuffix = new MyDate(MyDate.getLastClosedDay().getDate());
        dateSuffix.futureOpenDay();
        return getBackupPath(dateSuffix.getDate());
    }

    public String getBackupPath(String date) {
        MyDate dateSuffix = new MyDate(date);
        String backupPath = getDir() + "\\Plan" + dateSuffix.getDate().replace("-", "");
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
                    eData.setNettyType(ClusterProtocal.FUNCTION.TradePlan);
                    updateFiles.add(eData);
                }
            }
        }
        return updateFiles;
    }


    private void writeFiles() {
        String dir = null;
        if (onWriting) return;
        onWriting = true;

        try {
            if (amAuctionFiles.size() > 0) {
                dir = getBackupPath();
                for (NettyExchangeData d : amAuctionFiles) {
                    new TxtFileWrap(d).writeToTextFile(dir);
                }
            }

            if (files.size() == 0) return;
            if (amAuctionFiles.size() == 0) {
                deleteFiles();
//            deleteBackupFiles();  //TODO No!
            }
            amAuctionFiles.clear();

//            try {
//                TimeUnit.SECONDS.sleep(2);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            dir = getSavePath();
            for (NettyExchangeData d : files) {
                new TxtFileWrap(d).writeToTextFile(dir);
            }

            files.clear();
        } catch (Error ignored) {

        } finally {
            onWriting = false;
        }
    }

    CircleTimer delayTimer = new CircleTimer() {
        @Override
        public void execTask() {
            writeFiles();
        }

        public void setCircleTime(long millis) {
            if (onWriting) return;
            super.setCircleTime(millis);
        }
    };
}
