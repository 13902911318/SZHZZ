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
import szhzz.Utils.Internet;

import java.util.HashMap;
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

    private int defaultLevel = 0;


    //    Config localCfg = null;

    String initMarketDataServer = null;

    private int serverPort = 7521;
    private int group = 0;
    boolean offLine = false;

    Config clusterCfg = null;
    static String macID = null;
    boolean definedGate = false;
    //    static String proxy = "";
    static Boolean routerDebug = null;

    //    HashMap<String, String> location = new HashMap<>();
    String localName = "";
    static final String noDefined = "No defined";
    private String clusterName = "Cluster";
    private String serverIP;

    public static boolean isRouterDebug() {
        if (routerDebug == null) {
            if (AppManager.getApp().getCfg() != null) {
                routerDebug = AppManager.getApp().getCfg().getBooleanVal("RouterDebug", false);
            } else {
                routerDebug = false;
            }
        }
        return routerDebug;
    }

    static AppManager App = AppManager.getApp(); //Error!
    boolean forceTakeover = false;
    private int localLevel = 0;
    boolean autoStartTrade = true;
    final HashMap<String, ClusterProperty> nodes = new HashMap<>();
    boolean cgfIsDirty = false;
    AppMessage msg = new AppMessage();
//    Hashtable<String,String> ipToName= new Hashtable<>();

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
            if (s.level < this.defaultLevel) {
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
        if (onlyOne != null && onlyOne.clusterCfg != null) return onlyOne.clusterCfg;
        return SharedCfgProvider.getInstance("net").getCfg("Group");
    }

//    private void addIpToName(String computer, Config cfg){
//        String[] ips = cfg.getProperty("IP", "").split(";");
//        for(String ip : ips){
//            ipToName.put(ip, computer);
//        }
//    }
//
//    public String getNameByIP(String ip){
//        return ipToName.get(ip);
//    }

    /**
     * @param clusterCfg
     */
    public void startup(Config clusterCfg) {
        if (clusterCfg != null && clusterServer == null) {
            this.clusterCfg = clusterCfg;

            serverPort = clusterCfg.getIntVal(clusterName, serverPort);
            ClusterClients.getInstance().setTimer(clusterCfg.getIntVal("Timer", 10 * 1000));
            ClusterClients.getInstance().setConnectionTimeout(clusterCfg.getIntVal("ConnectionTimeout", 10 * 1000));

            AppManager.logit("开启集群监测, Timer=" + clusterCfg.getIntVal("Timer", 10 * 1000));
//            String localNet = "";
            if (clusterCfg.getChildrenNames() == null) return;

            for (String computer : clusterCfg.getChildrenNames()) {
                Config child = clusterCfg.getChild(computer);
                //Abandon
                if (child.getIntVal("Level", 0) <= 0) {
                    logger.info(computer + " Level=0, not managed");
                    continue;
                }

                if (computer.equalsIgnoreCase(getHostName())) {
                    localName = child.getProperty("Public-IP", noDefined);
                    serverIP = child.getProperty("IP", "").split(";")[0];
                    if (!Internet.setMainIp(serverIP)) {
                        AppManager.MessageBox("请在 Group.ini 中设置正确的本机 IP", 15);
                    }
                    setLocalLevel(child.getIntVal("Level", 0));
                    setDefaultLevel(child.getIntVal("Level", 0));
                    setGroup(child.getIntVal("Group", 0));

                    clusterServer = ClusterServer.getInstance();
                    clusterServer.setServerName(computer);
                    clusterServer.setPort(serverIP, serverPort); //ips[0],
//                    clusterServer.setLocalLevel(getLocalLevel());
                    clusterServer.startServer();
                    break;
                }
//                location.put(child.getProperty("IP", ""), child.getProperty("P", noDefined));
            }


            for (String computer : clusterCfg.getChildrenNames()) {
                Config child = clusterCfg.getChild(computer);
                //Abandon
                if (child.getIntVal("Level", 0) <= 0) {
                    continue;
                }
                if (!computer.equalsIgnoreCase(getHostName())) {
                    String address = getLocalAddress(computer);
                    ClusterClients.getInstance().registerClient(computer, address.split(";"));

                    NettyRequystor remote = new NettyRequystor(computer);
                    remote.setReader(BusinessRuse.getInstance(), 5);
                    remoteClients.add(remote);
                }
            }

            if (connectionListener == null) {
                connectionListener = new ConnectionListener();
                connectionListener.setCircleTime(10 * 1000);
            }
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
        if (data.isByPass() && StationPropertyWrap.isRouterDebug(data)) {
//                logger.info("标志 8 (dataChanged) ID=" + data.getRequestID() + " " +
//                        data.getIpAddress() + "<-" + stationName);
            StationPropertyWrap.addRouter(data, "(8) " + AppManager.getHostName() + "." + this.getClass().getSimpleName() + ".dataChanged");
            logger.info("Router:" + StationPropertyWrap.getRouter(data));
        }
        synchronized (nodes) {
            ss = nodes.get(stationName);
            if (ss == null) {
                ss = new ClusterProperty();
                ss.stationName = stationName;
//                ss.type = "Remote";
                nodes.put(stationName, ss);
            }
            ss.bypass = data.isByPass();
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
            ss.internetIP = StationPropertyWrap.getInternetIP(data);
            ss.vpnIP = StationPropertyWrap.getVpnIP(data);
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
//            ss.type = "Remote";
            nodes.put(stationName, ss);
        }
        ss.level = 0;
        ss.connected = requystor.isConnected();
        ss.ipAddress = requystor.getIpAddress();
        ss.offline = !requystor.isConnected();
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

//    public boolean isAutoStartTrade() {
//        return autoStartTrade;
//    }

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

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setLocalLevel(int localLevel) {
        this.localLevel = localLevel;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public String getServerIP() {
        return serverIP;
    }


    public int getServerPort() {
        return serverPort;
    }

    public int getDefaultLevel() {
        return defaultLevel;
    }

    private void setDefaultLevel(int defaultLevel) {
        this.defaultLevel = defaultLevel;
    }


    class ConnectionListener extends CircleTimer {
        void resetCheckCount() {
            checkCount = 0;
        }

        int count = 3;

        @Override
        public void execTask() {
            if (defaultLevel < 10) {
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
    }

    public boolean isOnTrade() {
        return false;
    }

    /**
     * 中继的链式委托
     *
     * @param data
     */
    public void handDown(NettyExchangeData data) {
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
    public void joinToTradeServer() {

    }

    /**
     * 用于StockWind
     */
    public void leavTradeServer() {

    }

    boolean isSameLocation(String location) {
        return !location.equals(noDefined) && location.equals(localName);
    }

    public void reStart() {
        this.clusterCfg = getConfig();
        this.clusterCfg.reLoad();

        if (this.clusterCfg.getChildrenNames() != null) {
            for (String computer : this.clusterCfg.getChildrenNames()) {
                Config child = this.clusterCfg.getChild(computer);
                if (child.getIntVal("Level", 0) > 0 && !computer.equalsIgnoreCase(getHostName())) {
                    String address = getLocalAddress(computer);
                    ClusterClients.getInstance().registerClient(computer, address.split(";")); // 地址发生变化后会重启。否则保持原有链接
                }
            }
        }
        if (connectionListener == null) {
            connectionListener = new ConnectionListener();
            connectionListener.setCircleTime(10 * 1000);
        }

    }

    public String getLocalAddress(String computer) {
        String ipString = "";
        String address = "";
        Config child = clusterCfg.getChild(computer);
        if (this.isSameLocation(child.getProperty("Public-IP", "No defined"))) {
            //同一局域网内
            String[] ips = child.getProperty("IP", "").split(";");
            for (String ip : ips) {
                if (address.length() > 0) address += ";";
                address += ip + ":" + this.serverPort;
            }
        } else {
            //外网链接
            ipString = this.NatIp(computer, child.getProperty("IP", ""));
            address = ipString + ":" + this.NatPort(computer, this.serverPort);
            ipString = child.getProperty("VPN-IP");
            if (ipString != null) {
                address = address + ";" + ipString + ":" + this.serverPort;
            }
        }
        return address;
    }

    public Config getChildCfg(String computerName) {
        return getConfig().getChild(computerName);
    }

    public String NatIp(String computerName, String ip) {
        Config cfg = getChildCfg(computerName);
        String publicIP = cfg.getProperty("Public-IP", ip);
        if (isSameLocation(publicIP)) return ip;
        return publicIP;
    }

    public int NatPort(String computerName, int port) {
        Config cfg = getChildCfg(computerName);
        String publicIP = cfg.getProperty("Public-IP", noDefined);
        if (isSameLocation(publicIP)) return port;
        return cfg.getIntVal(String.valueOf(port), port);
    }
}


