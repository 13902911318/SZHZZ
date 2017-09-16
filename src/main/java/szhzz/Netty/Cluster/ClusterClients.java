package szhzz.Netty.Cluster;

import szhzz.App.AppManager;
import szhzz.App.BeQuit;
import szhzz.Netty.Cluster.ExchangeDataType.ClusterProtocal;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Utils.DawLogger;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by HuangFang on 2015/3/15.
 * 11:23
 * <p>
 * <p>
 * 连接远程服务器
 */
public class ClusterClients {
    private static DawLogger logger = DawLogger.getLogger(ClusterClients.class);
    private static AppManager App = AppManager.getApp();
    private static ClusterClients onlyOne = new ClusterClients();

    private final static HashMap<Long, NettyRequystor> requests = new HashMap<>();
    private static Vector<Long> abandoned = new Vector<Long>();
    private Hashtable<String, NettyClient> clients = new Hashtable<String, NettyClient>();
    private int defaultPort = 7522;

    private ClusterClients() {

    }


    public static ClusterClients getInstance() {
        return onlyOne;
    }

    protected static void abandon(Long responseID) {
        deQueue(responseID);
        abandoned.add(responseID);
    }

    protected static NettyRequystor deQueue(Long responseID) {
        NettyRequystor r = null;
        if (responseID <= 0) return null;

        synchronized (requests) {
            r = requests.remove(responseID);
        }
        if (r != null) {
//            szhzz.App.logit(" Drop responseID=" + responseID);
            r.clearRequestID();
        } else {
            logger.info(" Drop TradeJni connect with Error, ID=" + responseID);
        }
        return r;
    }


    /**
     * Client 出站
     * 提交查询到远程服务器
     */
    public boolean query(String address, NettyExchangeData msg) {
        NettyClient client = clients.get(address);
        if (client == null) {
            return false;
        }
        client.send(msg);
        return true;
    }

    public boolean query(NettyRequystor requestor) {
        NettyClient client = clients.get(requestor.getStationName());
        if (client == null) {
            return false;
        }
        long w_RequestID = 0;
        NettyExchangeData obj = requestor.getQueryData();
        if (obj != null) {
            synchronized (requests) {
                w_RequestID = client.send(obj);
//            if (!isClientOnly()) {
                if (w_RequestID > 0) {
                    requestor.setRequestID(w_RequestID);
                    requests.put(w_RequestID, requestor);
//                    szhzz.App.logit("Put request ID=" + w_RequestID);
                    return true;
                } else {
                    AppManager.getApp().logEvent(" Query(" + requestor.getFunID() + ") Error ");
                }
//            } else {
//                return w_RequestID > 0;
//            }
            }
            return w_RequestID > 0;
        } else {
            AppManager.getApp().logEvent(" Query(" + requestor.getFunID() + ") Data == null Error ");
        }
        return false;
    }

    /**
     * Client 出站
     * 提交查询到远程服务器
     * 无需等待回答
     */
    public boolean tell(String stationName, NettyExchangeData msg) {
        NettyClient client = clients.get(stationName);
        if (client == null) {
            return false;
        }
        return client.send(msg) > 0;
    }

    public boolean isConnect(String address) {
        NettyClient client = clients.get(address);
        return client != null && client.isConnected();
    }

    public String getAddress(String computerName) {
        NettyClient client = clients.get(computerName);
        if (client != null) {
            return client.getHost();
        }
        return "?";
    }

    /**
     * Client 进站
     *
     * @param data
     */
    public long callBack(NettyExchangeData data) {
        NettyRequystor requestor = null;
        long id = 0;

//        if (isClientOnly()) {
//            if (StationPropertyWrap.isAnswerServer(data)) {
//                //线程隔断
//                BusinessRuse.getInstance().acceptBroadcast(data);
//                if (messageCount++ > 5) {
//                    messageCount = 0;
//                    AppManager.logit("连接到自动交易 On Trade=" + StationPropertyWrap.isOnTrade(data));
//                }
//            }
//            return 0;
//        }

        /**
         * 远程主动推送的信息
         */
        if (ClusterProtocal.isBroadcast(data.getEventType())) {
//            AppManager.logit("Remote Broadcast call from " + data.getIpAddress() +
//                    " " + data.getHostName() +
//                    " NettyType=" + data.getNettyType() + " String=" + data.getMessageString());
            BusinessRuse.getInstance().acceptBroadcast(data);
            return 0;
        }

        /**
         * 查询异步回调
         */
        try {
            id = data.getRequestID();

            requestor = deQueue(id);
            if (requestor != null) {
//                szhzz.App.logit(" 集群查询回调 " + data.getFunID() + " RequestID=" + id +
//                        " 返回" + data.getDataRowCount() + "行记录");
                requestor.push(data);
            } else {
                AppManager.logit(" 集群查询回调 " + data.getNettyType() + " RequestID=" + id + " 回调代码失配");
            }

        } catch (Exception e) {
            logger.error(e);
        }
        if (requestor != null) {
//            BrokerApp.getApp().setStatuText(MiscDate.timeMM() + " T-" + id);
        } else {
            if (abandoned.remove(id)) {
                String errMsg = " 集群查询返回数据超时:" + id;
                AppManager.logit(errMsg + "\n" + data.toString());
            } else {
                String errMsg = " 集群查询回调代码失配:" + id;
                AppManager.logit(errMsg + "\n" + data.toString());
            }
        }
        return id;
    }


//    public void startControlCenter(Config cfg) {
//        AppManager.logit("开启集群监测");
//        int port_ = cfg.getIntVal("ControlCenter", defaultPort);
//
//        String local = AppManager.getHostName();
//        for (String computer : cfg.getChildrenNames()) {
//            try {
//                Config child = cfg.getChild(computer);
//                if (!computer.equalsIgnoreCase(local)) {
//                    registerClient(computer, child.getProperty("IP", "").split(";"), port_);
//                    AppManager.logit("启动客户端 " + computer + " " + port_);
//                }
//            } catch (Exception e) {
//                logger.error(e);
//            }
//        }
//    }

    public NettyClient registerClient(String computerName, String[] address, int port) {
        NettyClient client = clients.get(computerName);
        if (client == null) {
            try {
                client = new NettyClient(address, port);
                clients.put(computerName, client);
                App.logit(computerName + " " + client.getHost() + " to be connect");
                client.start();
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return client;
    }

    public void closeClient(String computerName) {
        NettyClient client = clients.remove(computerName);
        if (client != null) {
            try {
                App.logit(computerName + "  " + client.getHost() + " disconnect!");
                client.disconnectFromServer();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }


    public void closeAll() {
        for (String computerName : clients.keySet()) {
            NettyClient client = clients.get(computerName);
            if (client != null) {
                try {
                    App.logit(computerName + "  " + client.getHost() + " disconnect!");
                    client.disconnectFromServer();
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }
    }

    BeQuit a = new BeQuit() {

        @Override
        public boolean Quit() {
            closeAll();
            return true;
        }
    };

}
