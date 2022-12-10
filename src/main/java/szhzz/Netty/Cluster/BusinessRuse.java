package szhzz.Netty.Cluster;

import com.sun.istack.internal.NotNull;
import szhzz.App.AppManager;
import szhzz.App.MessageAbstract;
import szhzz.App.MessageCode;
import szhzz.DataBuffer.DataConsumer;
import szhzz.DataBuffer.ObjBufferedIO;
import szhzz.Netty.Cluster.ExchangeDataType.*;
import szhzz.Netty.Cluster.Net.ServerHandler;
import szhzz.StatusInspect.StatusInspector;
import szhzz.Utils.DawLogger;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by HuangFang on 2015/3/30.
 * 10:46
 * <p>
 * 管理远程主动推送的信息
 */
public class BusinessRuse implements DataConsumer {
    private static DawLogger logger = DawLogger.getLogger(BusinessRuse.class);
    static BusinessRuse onlyOne = null;

    protected ObjBufferedIO dataBuffer = null;

    Message message = new Message();

    BusinessRuse() {
    }


    public static void setInstance(BusinessRuse onlyOne) {
        if(BusinessRuse.onlyOne!=null){
            logger.error(new Exception( "重复定义 BusinessRuse"));
        }
        BusinessRuse.onlyOne = onlyOne;
    }

    public static BusinessRuse getInstance() {
        if (onlyOne == null) {
            AppManager.MessageBox("未定义 BusinessRuse setInstance()");
        }
        return onlyOne;
    }

    public void setBufferSize(int bufferSize) {
        if (bufferSize <= 0) {
            dataBuffer = null;
            return;
        }
        dataBuffer = new ObjBufferedIO();
        try {
            dataBuffer.setReader(this, bufferSize);
        } catch (InterruptedException e) {
            logger.error(e);

            dataBuffer.close();
            dataBuffer = null;
        }
    }

    public int getErrorCode() {
//        int e = NU.parseInt(AppEventExchange.getInstance().getEvent("持仓修正"),0);
        return StatusInspector.getInstance().getErrorCount();
    }

    public String getCloseDate() {
        return "";
    }

    public void push(Object obj) {
        try {

            if (Cluster.getInstance().isOffLine()) return;
            if (dataBuffer != null) {
                dataBuffer.push(obj);
            } else {
                in(obj);
            }

        } finally {
        }
    }

    /**
     * 直接回答客户端的请求
     * 用于覆盖
     *
     * @param data
     * @return
     */
    public ArrayList<NettyExchangeData> answer(NettyExchangeData data) {
        ArrayList<NettyExchangeData> eDatas = null;
        switch (data.getNettyType()) {
            case QueryServerLevel:
                NettyExchangeData exDate = StationPropertyWrap.getStationProperty(data);
                if (exDate != null) {
                    eDatas = new ArrayList<>();
                    eDatas.add(exDate);
                }
                break;
            default:
                push(data);
        }
        return eDatas;
    }

    public void broadcast(NettyExchangeData data) {
//        if (!Cluster.getInstance().isOffLine()) {
//            ClusterServer.getInstance().broadcast(data);
//        }
        if (ServerHandler.hasConnection()) {
            ServerHandler.broadcast(data);
        } else {
            Cluster.getInstance().broadcast(data); //经由客户端委托服务器发布广播
        }
    }

    public void broadcastSqlUpdate(ArrayList<String> data, String key) {
        broadcast(SqlUpdateWrap.getSqlUpdate(data, key));
    }

    public void broadcastSqlUpdate(ArrayList<String> data) {
        broadcast(SqlUpdateWrap.getSqlUpdate(data));
    }

    public void broadcastSqlUpdate(String data) {
        broadcast(SqlUpdateWrap.getSqlUpdate(data));
    }

    public void broadcastSqlUpdate(String data, String key) {
        broadcast(SqlUpdateWrap.getSqlUpdate(data, key));
    }

    public void broadcastMessage(MessageCode messageID, String data) {
        broadcast(MessageWrap.getMessageObject(messageID, data));
    }

    public void broadcastOrderState(String orderID, String userSusband, String activeStates) {
        broadcast(OrderStateWrap.getOrderState(orderID, userSusband, activeStates));
    }

    public void broadcastInformation(String information) {
        broadcast(InformationWrap.getInformationObject(information));
        AppManager.logit(information);
    }

    public void broadcastStopBuy(boolean stopBuy) {
        broadcast(StopBuyWrap.getStopBuy(stopBuy));
    }

    public void broadcastStopSale(boolean stopSale) {
        broadcast(StopSaleWrap.getStopSale(stopSale));
    }


    public void broadcastOrderState(ArrayList<String> orderID, ArrayList<String> userSusband, ArrayList<String> activeStates) {
        broadcast(OrderStateWrap.getOrderState(orderID, userSusband, activeStates));
    }

    public void broadCastUpdateFiles() {
        HashSet<NettyExchangeData> data = TradePlanReadWriter.getInstance().getUpdateFiles();
        for (NettyExchangeData d : data) {
            broadcast(d);
        }
    }

//    public void broadcastShutdown(String statu) {
//        broadcast(ShutdownWrap.getShutdownWrap(statu));
//    }

//    public void broadcastAccountQuery(NettyExchangeData data) {
//        if (Cluster.connectToProxy()) return;
//        broadcast(data);
//    }

    /**
     * 标志7
     * @param obj
     * @return
     */
    @Override
    public long in(Object obj) {
        if (onlyOne != null) {
            logger.info("标志7 " + ((NettyExchangeData)obj).getHostName() + "@" + ((NettyExchangeData)obj).getIpAddress());
            return onlyOne.in(obj);
        }

        return -1;

    }

    public String getPassword(String key) {
        return "";
    }


    int acceptCluster(NettyExchangeData eData) {
        if(eData == null) return -1;
        switch (eData.getNettyType()) {
            case AnswerServerLevel:
                Cluster.getInstance().dataChanged(eData);
                break;
        }
        return 0;
    }


    int acceptBroadcast(NettyExchangeData eData) {
        if (eData != null && eData.isForward()) {
            eData.setForward(false);
            broadcast(eData);
            return 0;
        }
        return -1;
    }

    public boolean isSameCpuID(NettyExchangeData eData) {
        if(eData == null) return false;
        return Cluster.getCpuID().equals(eData.getCpuID());
    }


    public boolean isSameAppClass(NettyExchangeData eData) {
        if(eData == null) return false;
        return Cluster.getAppClassName().equals(eData.getAppClassName());
    }


    void acceptMessage(NettyExchangeData eData) {
        if(eData == null) return ;

        if (!isSameCpuID(eData) || !isSameAppClass(eData)) {
            MessageWrap messageWrap = new MessageWrap(eData);
            message.sendMessage(messageWrap.getMessageCode(), messageWrap.getMessageData());
        }
    }

    @Override
    public long in(long dataID, Object obj) {
        return 0;
    }


    class Message extends MessageAbstract {
        @Override
        public boolean acceptMessage(MessageCode messageID, Object caller, Object message) {
            if (caller == this) return false;
            switch (messageID) {
                case Net_QueryOrderStatus:
                    BusinessRuse.getInstance().broadcastMessage(messageID, message.toString());
                    break;

            }

            return false;
        }
    }


    public static boolean isSameIp(SocketAddress add1, SocketAddress add2) {
        return (getIp(add1).equals(getIp(add2)));
    }

    public static boolean isSameIp(SocketAddress add) {
        return (AppManager.isLocalIP(getIp(add)));
    }

    public static String getIp(SocketAddress add) {
        String IP = add.toString();
        IP = IP.replaceAll("/", "");
        int p2 = IP.indexOf(":");
        if (p2 > 0) {
            IP = IP.substring(0, p2);
        }
        return IP;
    }

    public static boolean isSameIp(String ip) {
        return (AppManager.isLocalIP(ip));
    }

    MessageAbstract msg = new MessageAbstract() {
        @Override
        public boolean acceptMessage(MessageCode messageID, Object caller, Object message) {
            if (caller == this) return false;

            switch (messageID) {
                case QueryStatus:
                    reportStatus();
            }
            return false;
        }
    };

    void reportStatus() {
//        if (onlyOne != null) {
//            onlyOne.reportStatus();
//        }
    }
}

