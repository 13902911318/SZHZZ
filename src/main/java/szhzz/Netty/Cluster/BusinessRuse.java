package szhzz.Netty.Cluster;

import szhzz.App.AppManager;
import szhzz.App.MessageAbstract;
import szhzz.App.MessageCode;
import szhzz.DataBuffer.DataConsumer;
import szhzz.DataBuffer.ObjBufferedIO;
import szhzz.Netty.BufferedSqlUpdater;
import szhzz.Netty.Cluster.ExchangeDataType.*;
import szhzz.Netty.Proxy.MarketProxy;
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
    private static AppManager App = AppManager.getApp();
    private static BusinessRuse onlyOne = null;
    protected ObjBufferedIO dataBuffer = null;
    private Message message = new Message();
    private TradePlanReadWriter tradePlanWriter = null;
    NettyExchangeData data = null;

    private BusinessRuse() {
    }


    public static BusinessRuse getInstance() {
        if (onlyOne == null) {
            onlyOne = new BusinessRuse();
//            onlyOne.setBufferSize(10);
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

    public void acceptBroadcast(NettyExchangeData data) {
        try {
            if (Cluster.getInstance().isOffLine()) return;
            if (dataBuffer != null) {
                dataBuffer.push(data);
            } else {
                in(data);
            }
        } finally {
        }
    }

    private void broadcast(NettyExchangeData data) {
        if (!Cluster.getInstance().isOffLine()) {
            ClusterServer.getInstance().broadcast(data);
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
        App.logit(information);
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

    public void broadcastShutdown(String statu) {
        broadcast(ShutdownWrap.getShutdownWrap(statu));
    }

    public void broadcastAccountQuery(NettyExchangeData data) {
        broadcast(data);
    }

    @Override
    public long in(Object obj) {
        boolean count = false;
//        try {
//            TimeUnit.SECONDS.sleep(20);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Buffer out thread = " + Thread.currentThread().getId());

        if (!(obj instanceof NettyExchangeData)) {
            logger.info("Not a NettyExchangeData:\n" + obj.toString());
            return -1;
        }

        if (Cluster.getInstance().isOffLine()) return -1;

        NettyExchangeData eData = (NettyExchangeData) obj;
//        if (AccountQueryWrap.isTypeOf(eData)) {
//            AccountsManager.getInstance().acceptNettyData(eData);
//            return 0;
//        }

        switch (eData.getNettyType()) {
            case AnswerServerLevel:
                Cluster.getInstance().dataChanged(eData);
                break;
            case AccountQuery:
//                AccountsManager.getInstance().acceptForenNettyData(eData);
                break;
            case CloseOthers:
                App.logit("Remote call close");
                if(!Cluster.getInstance().isOffLine()){
                    if(Cluster.getInstance().isGroupMenber(eData.getGroup())){
                        Cluster.getInstance().turnOffTrade_OnRequest(this);
                    }

                }
                break;
            case SendMessage:
                acceptMessage(eData);
                break;
            case UpdateConfigValue:
                CfgUpdateWrap cfgWriter = new CfgUpdateWrap(eData);
                cfgWriter.updateCfg();
                break;
            case TextFile:
                TxtFileWrap tw = new TxtFileWrap(eData);
                tw.writeToTextFile(null);
                break;
            case TradePlan:
                if(Cluster.getInstance().isGroupMenber(eData.getGroup())) {
                    if (tradePlanWriter == null) {
                        tradePlanWriter = TradePlanReadWriter.getInstance();
                    }
                    tradePlanWriter.addData(eData);
                }
                break;
            case OrderState:
                if(Cluster.getInstance().isGroupMenber(eData.getGroup())){
                    orderUpdate(eData);
                }
                break;
            case StopBuy:
                if(Cluster.getInstance().isGroupMenber(eData.getGroup())){
                    stocpBuy(eData);
                }
                break;
            case StopSale:
                if(Cluster.getInstance().isGroupMenber(eData.getGroup())){
                    stocpSale(eData);
                }
                break;
            case SqlUpdate:
                BufferedSqlUpdater.getInstance().push(eData);
//                count = true;
                break;
            case MarketData:
                MarketDataWrap.checkSerialNo(eData);
//                TDF_Proxy.getInstance().NettyCallBack(eData);

//                count = true;
                break;
            case UserData:
                //TODO TBD
                String id = eData.getMessageString();
                if ("SQL_UPDATE".equals(id)) {
                    BufferedSqlUpdater.getInstance().push(eData);
                }
//                count = true;
                break;
            case LogInformation:
                String msg = new InformationWrap(eData).getInformation();
                App.logit(msg);
                break;
            case SetupUDP:
                try {
                    int port = SetupUDPWrap.getLocalPort(eData);
                    MarketProxy.getInstance().setupDdp(port);
                    App.logEvent("Udp setuped. Remote address " + eData.getHostName() + ":" + SetupUDPWrap.getLocalPort(eData));
                }catch (Exception e){
                    logger.error(e);
                }
                break;
            default:
                App.logit("Remote Broadcast call from " + eData.getIpAddress() +
                        " " + eData.getHostName() +
                        " NettyType=" + eData.getNettyType() + " String=" + eData.getMessageString());
        }

        return 0;
    }

    public boolean isSameCpuID(NettyExchangeData eData){
        return eData != null && Cluster.getCpuID().equals(eData.getCpuID()) ;
    }

    public boolean isSameAppClass(NettyExchangeData eData){
        return eData != null && Cluster.getAppClassName().equals(eData.getAppClassName());
    }

    private void acceptMessage(NettyExchangeData eData) {
        MessageWrap messageWrap = new MessageWrap(eData);
        if (!isSameCpuID(eData) || !isSameAppClass(eData)) {
            message.sendMessage(messageWrap.getMessageCode(), messageWrap.getMessageData());
        }
    }

    private void orderUpdate(NettyExchangeData eData) {
        OrderStateWrap orderStateWrap = new OrderStateWrap(eData);

        while (orderStateWrap.next()) {
            String orderID = orderStateWrap.getOrderID();
            String state = orderStateWrap.getActiveStates();
            String userSusband = orderStateWrap.getUserSusband();
//            OrderConfigListView.getInstance().setOrderStates(orderID, userSusband, state);
        }
    }

    private void stocpBuy(NettyExchangeData eData) {
        StopBuyWrap stopBuyWrap = new StopBuyWrap(eData);
//        OrderConfigListView.getInstance().setStopBuyFromRemote(stopBuyWrap.isStopBuy());
    }

    private void stocpSale(NettyExchangeData eData) {
        StopSaleWrap stopSaleWrap = new StopSaleWrap(eData);
//        OrderConfigListView.getInstance().setStopSaleFromRemote(stopSaleWrap.isStopSale());
    }


//    private void ShutdownIncom(NettyExchangeData eData) {
//        Database db = DbStack.getDb(this.getClass());
//        try {
//            ShutdownInspector.getInstance().setNoBoBraodcast(true);
//            ShutdownWrap shutdownWrap = new ShutdownWrap(eData);
//            shutdownWrap.writeToShutdownFile();
//            szhzz.App.Shutdown();
//        } finally {
//            DbStack.closeDB(db);
//        }
//    }

    @Override
    public long in(long dataID, Object obj) {
        return 0;
    }

//    public boolean isOffLine() {
//        return offLine;
//    }
//
//    public void setOffLine(boolean offLine) {
//        this.offLine = offLine;
//    }


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

//    class FileObserver implements Observer {
//        public boolean closed = false;
//        @Override
//        public void update(Observable observable, Object eventArgs) {
//            if(closed ) return;
//
//            closed = true;
//            try {
//                FileSystemEventArgs args = (FileSystemEventArgs) eventArgs;
//                if (!args.getFileName().toLowerCase().contains("shutdown.txt")) return; //子目录名，或意外的其他文件
//                if (args.getKind() == StandardWatchEventKinds.ENTRY_CREATE) {
//                    File f = new File(args.getFileName());
//                    MyDate fTime = new MyDate(f.lastModified());
//                    if (MyDate.getToday().compareMinuts(fTime) <= 1) {
//
//                        broadcastShutdown();
//                        szhzz.App.Shutdown();
//                    }
//                }
//            } catch (Exception e) {
//                logger.error(e);
//            }
//        }
//    }

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

    private MessageAbstract msg = new MessageAbstract() {
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

    private void reportStatus() {

//            StatusData d = new StatusData("行情数据传输");
//            d.need = false;
//            d.status = MarketDataWrap.getLostCount() > 0;
//            d.relate = "TCP/UDP";
//            d.note = "数据包丢失 " + MarketDataWrap.getLostCount() + " @" + MarketDataWrap.getLostPercent();
//            d.locate = "BusinessRuse";
//
//            msg.sendMessage(MessageCode.ReportStatus, d);

    }
}

