package szhzz.Netty.Cluster;

import szhzz.App.AppManager;
import szhzz.App.MessageAbstract;
import szhzz.App.MessageCode;
import szhzz.Config.CfgProvider;
import szhzz.Config.Config;
import szhzz.Config.SharedCfgProvider;
import szhzz.DataBuffer.DataConsumer;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Netty.Cluster.ExchangeDataType.StationPropertyWrap;
import szhzz.StatusInspect.StatusData;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;
import szhzz.Utils.HardwareIDs;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;


/**
 * Created by HuangFang on 2015/3/28.
 * 9:36
 * <p>
 * 专门用于管理集群连接的多个客户端和本地的集群服务器。
 */
public class Cluster {
    static DawLogger logger = DawLogger.getLogger(DataConsumer.class);

    static Cluster onlyOne = null;

    Vector<NettyRequystor> remoteClients = new Vector<>();
    int checkCount = 1;
    ClusterServer clusterServer = null;
    ConnectionListener connectionListener;
    ClusterStation clusterStationUI = null;

    int definedLevel = 0;


    //    Config localCfg = null;

    String initMarketDataServer = null;

    int defaultPort = 7521;
    int group = 0;
    boolean offLine = false;
    Config clusterCfg = null;
    static String macID = null;
    boolean definedGate = false;
//    static String proxy = "";

    static AppManager App = AppManager.getApp(); //Error!
    boolean forceTakeover = false;
    int localLevel = 0;
    boolean autoStartTrade = true;
    final HashMap<String, ClusterProperty> nodes = new HashMap<>();
    boolean cgfIsDirty = false;
    AppMessage msg = new AppMessage();
    Hashtable<String,String> ipToName= new Hashtable<>();

    Cluster() {
        App = AppManager.getApp(); //Error!
//        Config cfg = CfgProvider.getInstance("系统策略").getCfg("System");
//        proxy = cfg.getBooleanVal("设为交易代理", false);
    }

    String statusMessage = "正在收集信息...";

    public static Cluster getInstance() {
        if (onlyOne == null) {
            AppManager.MessageBox("Developer: Cluster 需要定义衍生类.");
        }
        return onlyOne;
    }

    public static void setInstance(Cluster onlyOne) {
        Cluster.onlyOne = onlyOne;
    }

//    public static String getProxy() {
//        return proxy;
//    }

//    public static void setProxy(String proxy) {
//        if (proxy == null) proxy = "";
//        Cluster.proxy = proxy;
//    }

    public void broadcast(NettyExchangeData msg) {

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

    public long sentTo(NettyExchangeData msg, String hostName) {
        return ClusterClients.getInstance().tell(hostName, msg);
    }

    public boolean isConnect(String hostName) {
        return ClusterClients.getInstance().isConnect(hostName);
    }

//    public boolean isConnectProxy() {
//        if ("".equals(getTradeProxyHost())) return false;
//        return ClusterClients.getInstance().isConnect(getTradeProxyHost());
//    }

    public static Config getConfig() {
        return SharedCfgProvider.getInstance("net").getCfg("Group");
    }

    private void addIpToName(String computer, Config cfg){
        String[] ips = cfg.getProperty("IP", "").split(";");
        for(String ip : ips){
            ipToName.put(ip, computer);
        }
    }

    public String getNameByIP(String ip){
        return ipToName.get(ip);
    }

    //移到ClusterExt
    public void startup(Config clusterCfg) {
        if (clusterCfg != null && clusterServer == null) {
            this.clusterCfg = clusterCfg;

            Config cfg_ = CfgProvider.getInstance("Schedule").getCfg("System");
            //移除
            autoStartTrade = cfg_.getBooleanVal("自动接管交易", autoStartTrade);
            forceTakeover = cfg_.getBooleanVal("强制接管交易", false);
            setOffLine(cfg_.getBooleanVal("离线", false));

            ClusterClients.getInstance().setConnectionTimeout(clusterCfg.getIntVal("ConnectionTimeout", 1000));

            if (clusterCfg.getChildrenNames() == null || clusterCfg.getChildrenNames().size() == 0) {
                AppManager.MessageBox("请定义 " + clusterCfg.getConfigUrl() + " 集群节点设置文件");
            }
            int port = clusterCfg.getIntVal("Cluster", defaultPort);

            for (String computer : clusterCfg.getChildrenNames()) {
                Config child = clusterCfg.getChild(computer);
                addIpToName(computer, child);

                if (computer.equalsIgnoreCase(getHostName())) {
                    localLevel = child.getIntVal("Level", 0);
                    group = child.getIntVal("Group", 0);

                    definedLevel = localLevel;
                    if (clusterServer == null) {
                        clusterServer = ClusterServer.getInstance();
                        clusterServer.setServerName(computer);
                        clusterServer.setPort(port);
                        clusterServer.setLocalLevel(localLevel);
                        clusterServer.startServer();
//                        setProxy(child.getProperty("Proxy", ""));
                    }
                } else if (child.getIntVal("Level", 0) > 0) {
                    ClusterClients.getInstance().registerClient(computer,
                            child.getProperty("IP", "").split(";"), port);

                    AppManager.logit("启动客户端 " + computer + " " + port);

                    NettyRequystor remote = new NettyRequystor(computer);
                    remote.setReader(BusinessRuse.getInstance(), 5);
                    remote.setQueryData(StationPropertyWrap.getStationLevelQuery());
                    remoteClients.add(remote);
                }
            }
        }

        if (connectionListener == null) {
            connectionListener = new ConnectionListener();
            connectionListener.setCircleTime(10 * 1000);
        }
    }

    //显示界面的管理程序
    public void setClusterStationUI(ClusterStation clusterStationUI) {
        this.clusterStationUI = clusterStationUI;
    }

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
            ss.errorCode = StationPropertyWrap.getAppErrorCode(data);
            ss.tradeProxy = StationPropertyWrap.tradeProxy(data);

            ss.group = data.getGroup();
            ss.cpuID = data.getCpuID();
            ss.appClass = data.getAppClassName();
        }
//        if (Cluster.getInstance().isGroupMenber(ss.group) && ss.isProxy && !isProxy()) {
//            setTradeProxyHost(stationName);
//        }

        if (clusterStationUI != null && ss != null) {
            clusterStationUI.dataChanged(ss);
        }

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
        ss.tradeProxy = "";

        if (clusterStationUI != null) {
            clusterStationUI.dataChanged(ss);
        }
    }

    public boolean isOffLine() {
        return offLine;
    }

    public void setOffLine(boolean offLine) {
        this.offLine = offLine;
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


//    public static String getTradeProxyHost() {
//        return getProxy();
//    }

//    public static boolean connectToProxy() {
//        Config cfg = CfgProvider.getInstance("系统策略").getCfg("System");
//        return !isProxy() && cfg.getBooleanVal("使用交易代理", false);
//        return getProxy().length() > 0;
//    }


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
                if (clusterStationUI != null) clusterStationUI.dataChanged(getHostName());

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

    //////////////////////////////////////////
    void checkStatus() {
//        if (isOffLine()) return;
//        if (forceTakeover) {
//            statusMessage = "用户指定本站为集群主机";
//            App.logit(statusMessage);
//            localLevel = 11;
//
//            ClusterServer.getInstance().setLocalLevel(localLevel);
//        } else if (!autoStartTrade) {
//            statusMessage = "用户关闭集群节点";
//            App.logit(statusMessage);
//
//            localLevel = -1 * definedLevel; //降级并知会本节点的原级别和健康不良
//
//            ClusterServer.getInstance().setLocalLevel(localLevel);
//            turnOffTrade_OnRequest(null);
////            if (isKeepConnectToBroker()) {
////                turnOffTrade();
////            }
//            return;
//        } else if (!StatusInspector.getInstance().checkRelate("集群节点", true)) {
//            statusMessage = StatusInspector.getInstance().getErrorMsg() + ",关闭集群节点";
//            App.logit(statusMessage);
//
//            localLevel = -1 * definedLevel;  //降级并知会本节点的原级别和健康不良
//            ClusterServer.getInstance().setLocalLevel(localLevel);
//
//            if (isOnTrade()) {
//                turnOffTrade_OnRequest(this);
//            }
//            return;
//        } else {
//            localLevel = definedLevel;
//        }
//
//        ClusterServer.getInstance().setLocalLevel(localLevel);
//
//        int maxLevel = 0;
//        boolean remoteIsOn = false;
//        boolean takeOver = false;
//        int onlineSiblings = 0;
//        String gateHosts = null;
//        int proxyLevel = 0;
//
//        synchronized (nodes) {
//            for (ClusterProperty ss : nodes.values()) {
//                if (ss.offline) {
//                    continue;
//                }
//
//                if (!isGroupMenber(ss.group)) { //ss.group > 0 &&
//                    continue;
//                }
//
//                //远程在交易中
//                if (ss.onTrade) {
//                    remoteIsOn = true;
////                    if (ss.level > localLevel) { //远程级别高于本机且已打开交易,关闭本地交易
////                        if (isKeepConnectToBroker()) {
////                            turnOffTrade();
////                            return;
////                        }
////                    } else {
////                        //远程级别低于本机,本地已经打开交易, 关闭所有远程
////                        if (isKeepConnectToBroker()) {
////                            clusterServer.broadcast();
////                            return;
////                        }
////                    }
//                }
//                //在线？
//                if (ss.connected) {
//                    onlineSiblings++;
//                }
//                //较高权限的节点健康不良
//                if (Math.abs(ss.level) > localLevel && ss.level < 0) {
//                    takeOver = true;
//                }
//                //求远程最大 level
//                maxLevel = Math.max(maxLevel, ss.level);
////                if (!"".equals(ss.tradeProxy)) {
////                    if(proxyLevel < ss.level){
////                        proxyLevel = ss.level;
////                        gateHosts = ss.stationName.toUpperCase();
////                    }
////                }
//            }
//        }
////        setTradeProxyHost(gateHosts);
//
//
//        // 本站成集群中具有最高权限的节点
//        // 接手进行交易
//        if (maxLevel < localLevel)
//
//        {
//            App.logit("本站为集群中具有最高权限" + localLevel + "的节点");
//            //关闭其他站点
//            if (remoteIsOn) {
//                statusMessage = "正在请求关闭其他站点";
//                App.logit(statusMessage);
//                clusterServer.closeOtheNodes();
//                checkRemote();
//            } else {
//                // 只有本机在线
//                // 不能肯定高级别节点不在交易中(例如VPN网络故障)
//                // 执行买入委托时减小每次交易量,以减少可能的重复买入
//                // 低级别节点不参与早盘竞价
//                if (localLevel < 5 && (!takeOver || onlineSiblings == 0)) { //
//                    App.logit("localLevel < 9 && (!takeOver || onlineSiblings == 0)");
//                    if (MyDate.IS_AFTER_TIME(9, 30, 0)) {
//                        if (!isOnTrade()) {
//                            App.logit("MyDate.IS_AFTER_TIME(9,29,30)");
//
//                            if (!cgfIsDirty) {
//                                DialogManager.getInstance().openWindow("ClusterStation");
//
//                                App.logit("set cgfIsDirty");
//                                App.sendMail("本机具有最高权限(疑似网络故障),启动底级别交易", null);
//                                cgfIsDirty = true;
//                                Config systemCfg = CfgProvider.getInstance("系统策略").getCfg("System");
//                                systemCfg.setProperty("每笔委托金额上限", "500000");// 50 万
//                                systemCfg.setProperty("挂单比例", "0.3");//
//                                systemCfg.setProperty("集合竞价买入单笔上限", "1000000");// 100 万
//                                systemCfg.setReloadProtect(true);
//                                msg.sendMessage(MessageCode.ConfigChanged, systemCfg);
//                            }
//                            App.logit("cgfIsDirty=" + cgfIsDirty);
//
//                            this.turnOnTrade_ThisNode(this);
//                        }
//                    } else {
//                        statusMessage = "低级别节点不参与早盘竞价,9:30 后开盘";
//                        App.logit(statusMessage);
//
//                    }
//                } else if (!isOnTrade()) {
//                    //如果确认集群通信良好，远程站点都没在交易中,而本机具有最高权限,启动交易
//                    App.logit("本机具有最高权限,启动交易");
//                    App.sendMail("本机具有最高权限,启动交易", null);
//                    DialogManager.getInstance().openWindow("ClusterStation");
//
//
//                    if (cgfIsDirty) {
//                        App.logit("cgfIsDirty");
//                        Config systemCfg = CfgProvider.getInstance("系统策略").getCfg("System");
//                        systemCfg.setReloadProtect(false);
//                        systemCfg.reLoad();
//                        msg.sendMessage(MessageCode.ConfigChanged, systemCfg);
//                        cgfIsDirty = false;
//                    }
//                    App.logit("cgfIsDirty=" + cgfIsDirty);
//
//                    App.logit("turnOnTrade()");
//                    turnOnTrade_ThisNode(this);
//                } else {
//                    statusMessage = "本站设为备用";
//                }
//            }
//        } else{
//            statusMessage = "本站设为备用";
//        }
    }

    /**
     * 本站权衡集群状态后，接管主站权
     *
     * @param
     */
//    public void turnOnTrade_ThisNode(Object caller) {
//        if (initMarketDataServer == null) {
//            initMarketDataServer = App.getCfg().getProperty("行情服务器", "招商证券");
//        }
//        OrderConfigListView.getInstance().startAllTrade(caller, true);
//        reportStatus();
//
//    }
//
//    /**
//     * 远端要求关闭
//     *
//     * @param caller From BusinessRuse
//     */
//    public void turnOffTrade_OnRequest(Object caller) {
//        // 一旦远程请求关闭本地,本地需核实远程都已经关闭方可再次打开
//        connectionListener.resetCheckCount();
//        OrderConfigListView.getInstance().startAllTrade(caller, false);
//        reportStatus();
//    }
//
    public boolean isOnTrade() {
        //return OrderConfigListView.isOnTrade();
        return false;
    }

    /**
     * 中继的链式委托
     *
     * @param data
     */
    public void handDown(NettyExchangeData data) {
//        if (data != null) return; //TODO 合规限制 暂停此类交易 Order and Cancel
//
//        NettyRequystor node = getNextNode();
//        if (node != null) {
//            if (ClusterClients.getInstance().tell(node.getStationName(), data) < 0) {
//                String msg = "[HANDDOWN](" + data.getMessage() + ") " +
//                        "委托下级工作站(" + node.getStationName() + ")失败, " +
//                        "链式委托终止于(1): " + getHostName() + "\n" + data.toString();
//                BusinessRuse.getInstance().broadcastInformation(msg);
//                App.logit(msg);
//            }
//        } else {
//            String msg = "[HANDDOWN](" + data.getMessage() + ") " +
//                    "没有下级工作站,链式委托终止于: " + getHostName() + "\n" + data.toString();
//            BusinessRuse.getInstance().broadcastInformation(msg);
//            App.logit(msg);
//        }
    }

    void reportStatus() {
        StatusData d = new StatusData("集群状态");
        d.need = true;


        if (isOnTrade()) {
            d.note = ("本站为主交易站");
            d.status = true;
        } else {
            d.note = ("本站设为备用:" + statusMessage);
            d.status = false;
        }
        d.locate = this.getClass().getName();

        if (clusterStationUI != null) {
            clusterStationUI.dataChanged(getHostName());
        }

        msg.sendMessage(MessageCode.ReportStatus, d);
        App.logit(d.toString());
    }

    public String getStatusMessage() {
//        if (isOnTrade()) {
//            return ">:本站为主交易站";
//        }
        return ">:" + statusMessage;
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
            Config systemCfg = CfgProvider.getInstance("Schedule").getCfg("System");
            systemCfg.setReloadProtect(false);
            systemCfg.reLoad();
            msg.sendMessage(MessageCode.ConfigChanged, systemCfg);
            cgfIsDirty = false;
        }
        reportStatus();
    }

    /**
     * 用于StockWind
     */
    public void joinToTradeServer(){

    }

    /**
     * 用于StockWind
     */
    public void leavTradeServer() {

    }
}


