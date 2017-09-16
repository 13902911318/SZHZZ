package szhzz.Netty.Cluster;

import szhzz.App.AppManager;
import szhzz.App.MessageAbstract;
import szhzz.App.MessageCode;
import szhzz.Config.CfgProvider;
import szhzz.Config.Config;
import szhzz.DataBuffer.DataConsumer;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Netty.Cluster.ExchangeDataType.StationPropertyWrap;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;
import szhzz.Utils.HardwareIDs;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by HuangFang on 2015/3/28.
 * 9:36
 * <p>
 * 专门用于管理集群连接的多个客户端和本地的集群服务器。
 */
public class Cluster {
    private static DawLogger logger = DawLogger.getLogger(DataConsumer.class);
    private static AppManager App = AppManager.getApp();
    private static Cluster onlyOne = null;
    private final HashMap<String, ClusterProperty> nodes = new HashMap<>();
    private Vector<NettyRequystor> remoteClients = new Vector<>();
    private int checkCount = 1;
    private ClusterServer clusterServer = null;
    private ConnectionListener connectionListener;
//    private ClusterStation clusterStationUI = null;
    private int localLevel = 0;
    private int definedLevel = 0;

    private boolean autoStartTrade = true;
    private boolean forceTakeover = false;

    //    private Config localCfg = null;
    private boolean cgfIsDirty = false;
    private String initMarketDataServer = null;
    private AppMessage msg = new AppMessage();
    private int defaultPort = 7521;
    private int group = 0;
    private boolean offLine = false;
    private Config clusterCfg = null;
    private static String macID = null;
    private Cluster() {
    }

    private String statusMessage = "正在收集信息...";

    public static Cluster getInstance() {
        if (onlyOne == null) {
            onlyOne = new Cluster();
        }
        return onlyOne;
    }

    public NettyRequystor getNextNode() {
        String nodeName = null;
        int levelIndex = -1;
        for (ClusterProperty s : nodes.values()) {
            if (s.level < this.definedLevel) {
                if (levelIndex < s.level && s.connected) {// 找出小于本节点优先权的最大优先权节点
                    levelIndex = s.level;
                    nodeName = s.stationName;
                }
            }
        }
        if (levelIndex >= 0) {
            for (NettyRequystor n : remoteClients) {
                if (n.getStationName().equals(nodeName)) {
                    return n;
                }
            }
        }
        return null;
    }


    public static String getMac() {
        if (macID == null) {
            macID = HardwareIDs.getMACAddress();
        }

        return macID;
    }

    public static String getCpuID() {
        return AppManager.getCpuID();
    }

    public int getGroup() {
        return group;
    }

    public boolean isGroupMenber(int group) {
//        if (group == 0 || getGroup() == 0) return true; //
        return group == getGroup();
    }

    public void changeGroup(int group) {
        this.group = group;
        Config child = clusterCfg.getChild(getHostName());
        child.setProperty("Group", "" + group);
        clusterCfg.save();
    }


    public static String getAppClassName() {
        return AppManager.getAppClass().getName();
    }

    public boolean sentTo(NettyExchangeData msg, String hostName) {
        return ClusterClients.getInstance().tell(hostName, msg);
    }

    public static Config getConfig(){
        return CfgProvider.getInstance("net").getCfg("Group");
    }

    /**
     * 中继的链式委托
     *
     * @param data
     */
    public void handDown(NettyExchangeData data) {
        if (data != null) return; //TODO 合规限制 暂停此类交易 Order and Cancel

        NettyRequystor node = getNextNode();
        if (node != null) {
            if (!ClusterClients.getInstance().tell(node.getStationName(), data)) {
                String msg = "[HANDDOWN](" + data.getMessage() + ") " +
                        "委托下级工作站(" + node.getStationName() + ")失败, " +
                        "链式委托终止于(1): " + getHostName() + "\n" + data.toString();
                BusinessRuse.getInstance().broadcastInformation(msg);
                App.logit(msg);
            }
        } else {
            String msg = "[HANDDOWN](" + data.getMessage() + ") " +
                    "没有下级工作站,链式委托终止于: " + getHostName() + "\n" + data.toString();
            BusinessRuse.getInstance().broadcastInformation(msg);
            App.logit(msg);
        }
    }

    public static void main(String[] args) {
        App.setLog4J();
        Cluster.getInstance().startup(CfgProvider.getInstance("net").getCfg("Group"));

//        ClusterServer clusterServer = new ClusterServer();
//        if (clusterServer.startServer()) {
//            String ip = "localhost";
//            if (clusterServer.registerClient(ip)) {
//                clusterServer.testSend();
//            }
//        }
    }

    //
    public void startup(Config clusterCfg) {
        this.clusterCfg = clusterCfg;

        Config cfg_ = CfgProvider.getInstance("系统策略").getCfg("System");
        autoStartTrade = cfg_.getBooleanVal("自动接管交易", autoStartTrade);
        forceTakeover = cfg_.getBooleanVal("强制接管交易", false);
        setOffLine(cfg_.getBooleanVal("离线", false));

        int port = clusterCfg.getIntVal("Cluster", defaultPort);
        for (String computer : clusterCfg.getChildrenNames()) {
            Config child = clusterCfg.getChild(computer);
            if (computer.equalsIgnoreCase(getHostName())) {
                localLevel = child.getIntVal("Level", 0);
                group = child.getIntVal("Group", 0);
//                changeGroup();
                definedLevel = localLevel;
                if (clusterServer == null) {
                    clusterServer = ClusterServer.getInstance();
                    clusterServer.setServerName(computer);
                    clusterServer.setPort(port);
                    clusterServer.setLocalLevel(localLevel);
                    clusterServer.startServer();
                }
            } else if(child.getIntVal("Level", 0) > 0){
                ClusterClients.getInstance().registerClient(computer,
                        child.getProperty("IP", "").split(";"), port);

                AppManager.logit("启动客户端 " + computer + " " + port);

                NettyRequystor remote = new NettyRequystor(computer);
                remote.setReader(BusinessRuse.getInstance(), 5);
                remote.setQueryData(StationPropertyWrap.getStationLevelQuery());
                remoteClients.add(remote);
            }
        }

        if (connectionListener == null) {
            connectionListener = new ConnectionListener();
            connectionListener.setCircleTime(10 * 1000);
        }
    }

    //显示界面的管理程序
//    public void setClusterStationUI(ClusterStation clusterStationUI) {
//        this.clusterStationUI = clusterStationUI;
//    }

    /**
     *
     */
    public void checkRemote() {
        for (NettyRequystor r : remoteClients) {
            try {
                if (!r.Query()) {
                    dataChanged(r);
                }
            } catch (Exception e) {
                logger.error(e);
                dataChanged(r);
            }
        }
    }

    /**
     * 远端要求关闭
     *
     * @param caller From BusinessRuse
     */
    public void turnOffTrade_OnRequest(Object caller) {
        // 一旦远程请求关闭本地,本地需核实远程都已经关闭方可再次打开
        connectionListener.resetCheckCount();
//        OrderConfigListView.getInstance().startAllTrade(caller, false);
        reportStatus();
    }

    /**
     * 发出到其他站点, 要求远端的其它站点关闭
     *
     * @param caller
     */
    public void turnOffTrade_OtherNodes(Object caller) {
        if (initMarketDataServer == null) {
            initMarketDataServer = App.getCfg().getProperty("行情服务器", "招商证券");
        }

        if (cgfIsDirty) {
            App.logit("cgfIsDirty");
            Config systemCfg = CfgProvider.getInstance("系统策略").getCfg("System");
            systemCfg.setReloadProtect(false);
            systemCfg.reLoad();
            msg.sendMessage(MessageCode.ConfigChanged, systemCfg);
            cgfIsDirty = false;
        }
        reportStatus();
    }

    /**
     * 本站权衡集群状态后，接管主站权
     *
     * @param caller
     */
    public void turnOnTrade_ThisNode(Object caller) {
        if (initMarketDataServer == null) {
            initMarketDataServer = App.getCfg().getProperty("行情服务器", "招商证券");
        }

//        OrderConfigListView.getInstance().startAllTrade(caller, true);
        reportStatus();

    }


    void reportStatus() {
//        StatusData d = new StatusData("集群状态");
//        d.need = true;
//
//
//        if (isOnTrade()) {
//            d.note = ("本站为主交易站");
//            d.status = true;
//        } else {
//            d.note = ("本站设为备用:" + statusMessage);
//            d.status = false;
//        }
//        d.locate = this.getClass().getName();
//
//        if (clusterStationUI != null) {
//            clusterStationUI.dataChanged(getHostName());
//        }
//
//        msg.sendMessage(MessageCode.ReportStatus, d);
//        szhzz.App.logit(d.toString());
    }

    public boolean isOnTrade() {
        return false;
    }


    void dataChanged(NettyExchangeData data) {
//        StationPropertyWrap wrap = new StationPropertyWrap(data);

        String stationName = data.getHostName(); //serverName
        stationName = stationName.toUpperCase();
        ClusterProperty ss = null;
        synchronized (nodes) {
            ss = nodes.get(stationName);
            if (ss == null) {
                ss = new ClusterProperty();
                ss.stationName = stationName;
                ss.type = "Remote";
                nodes.put(stationName, ss);
            }
            ss.connected = true;
            ss.onTrade = data.getErrorCode() == 1;
            ss.lastUpdate = data.getTimeStamp();
            ss.ipAddress = data.getIpAddress();
            ss.timeLap = data.getTimeLap();
            ss.mack = StationPropertyWrap.getMack(data);
            ss.level = StationPropertyWrap.getServerLeve(data);
            ss.canShutdown = StationPropertyWrap.canRemoteShutdown(data);
            ss.closeDate = StationPropertyWrap.getCloseDate(data);
            ss.positionError = StationPropertyWrap.getPositionError(data);

            ss.group = data.getGroup();
            ss.cpuID = data.getCpuID();
            ss.appClass = data.getAppClassName();
        }

//        if (clusterStationUI != null && ss != null) {
//            clusterStationUI.dataChanged(ss);
//        }
    }

    /**
     * 连接失败
     *
     * @param requystor
     */
    public void dataChanged(NettyRequystor requystor) {
        String stationName = requystor.getStationName(); //serverName
        ClusterProperty ss = nodes.get(stationName);
        if (ss == null) {
            ss = new ClusterProperty();
            ss.stationName = stationName;
            ss.type = "Remote";
            nodes.put(stationName, ss);
        }
        ss.level = 0;
        ss.connected = requystor.isConnected();
        ss.ipAddress = requystor.getIpAddress();
        ss.offline = false;
        ss.onTrade = false;
        ss.mack = ""; //requystor.getMack()


//        if (clusterStationUI != null) {
//            clusterStationUI.dataChanged(ss);
//        }
    }

    public boolean isOffLine() {
        return offLine;
    }

    public void setOffLine(boolean offLine) {
        this.offLine = offLine;
    }

    private void checkStatus() {
    }

    public int getLocalLevel() {
        return localLevel;
    }

    public boolean isAutoStartTrade() {
        return autoStartTrade;
    }

    public void setAutoStartTrade(boolean autoStartTrade) {
        this.autoStartTrade = autoStartTrade;
    }

    public void setForceTakeover(boolean setTakeover) {
        this.forceTakeover = setTakeover;
    }


    public static String getHostName() {
        return App.getHostName();
    }

    public boolean isForceTakeover() {
        return forceTakeover;
    }


    class ConnectionListener extends CircleTimer {
        void resetCheckCount() {
            checkCount = 0;
        }

        int count = 3;

        @Override
        public void execTask() {
            if (definedLevel < 10) {
                count = 6;
            }
            try {
                checkRemote();
//                if (clusterStationUI != null) clusterStationUI.dataChanged(getHostName());

                if (checkCount >= count) {
                    checkStatus();
                } else {
                    checkCount++;
                }
            } finally {
                circleTime();
            }
        }
    }

    class AppMessage extends MessageAbstract {
        @Override
        public boolean acceptMessage(MessageCode messageID, Object caller, Object message) {
            if (clusterServer == null || caller == this) return false;

            switch (messageID) {
                case QueryStatus:
                    reportStatus();
                    break;
            }
            return false;
        }
    }
}


