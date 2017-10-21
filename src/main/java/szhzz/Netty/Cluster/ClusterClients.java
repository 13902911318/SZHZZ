package szhzz.Netty.Cluster;

import szhzz.App.AppManager;
import szhzz.App.BeQuit;
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
//            App.logit(" Drop responseID=" + responseID);
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
                if (w_RequestID > 0) {
//                    requestor.setRequestID(w_RequestID);
//                    requests.put(w_RequestID, requestor);

                    return true;
                } else {
                    AppManager.getApp().logEvent(" Query(" + requestor.getFunID() + ") Error ");
                }
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
    public long tell(String stationName, NettyExchangeData msg) {
        NettyClient client = clients.get(stationName);
        if (client == null) {
            logger.debug("Client " + stationName + " not create error!");
            return -1;
        }
        logger.debug("tell " + stationName);
        return client.send(msg);
    }

    public void broadcast(NettyExchangeData msg) {
        if (clients.isEmpty()) return;
        for (NettyClient client : clients.values()) {
            if (client.isConnected()) {
                msg.setForward(true);
                if (client.send(msg) > 0) {
                    break;
                }
            }
        }
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


        BusinessRuse.getInstance().push(data);
        return 0;

//        if (!ClusterProtocal.isCluster(data.getEventType())) {
//            BusinessRuse.getInstance().push(data);
//            return 0;
//        }
//
//        /**
//         * 查询异步回调
//         */
//        try {
//            id = data.getRequestID();
//
//            requestor = deQueue(id);
//            if (requestor != null) {
////                App.logit(" 集群查询回调 " + data.getFunID() + " RequestID=" + id +
////                        " 返回" + data.getDataRowCount() + "行记录");
//                requestor.push(data);
//            } else {
//                AppManager.logit(" 集群查询回调 NettyType=" + data.getNettyType() + " RequestID=" + id + " 回调代码失配");
//            }
//
//        } catch (Exception e) {
//            logger.error(e);
//        }
//        if (requestor != null) {
////            BrokerApp.getApp().setStatuText(MiscDate.timeMM() + " T-" + id);
//        } else {
//            if (abandoned.remove(id)) {
//                String errMsg = " 集群查询返回数据超时:" + id;
//                AppManager.logit(errMsg + "\n" + data.toString());
//            } else {
//                String errMsg = " 集群查询回调代码失配:" + id;
//                AppManager.logit(errMsg + "\n" + data.toString());
//            }
//        }
//        return id;
    }


    public NettyClient registerClient(String computerName, String[] address, int port) {
        NettyClient client = clients.get(computerName);
        if (client == null) {
            try {
                client = new NettyClient(address, port);
                clients.put(computerName, client);
                App.logit(computerName + " " + client.getHost() + " to be connect");
            } catch (Exception e) {
                logger.error(e);
            }
        }
        client.start();
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
