package szhzz.Netty.Cluster.ExchangeDataType;


import szhzz.App.AppManager;
import szhzz.Netty.Cluster.BusinessRuse;
import szhzz.Netty.Cluster.Cluster;
import szhzz.Utils.DawLogger;
import szhzz.Utils.HardwareIDs;
import szhzz.Utils.Internet;
import szhzz.Utils.NU;

/**
 * Created by Administrator on 2015/7/6.
 */
public class StationPropertyWrap {
    private static DawLogger logger = DawLogger.getLogger(StationPropertyWrap.class);
    private static NettyExchangeData closeOthersMsg = null;
    //    private static NettyExchangeData queryLevel = null;
    private static StationPropertyWrap extender = null;
    private static String vpnInterfaceName = "OrayBoxVPN Virtual Ethernet Adapter";

    public static NettyExchangeData getStationLevelQuery() {
//        if (queryLevel == null) {
        NettyExchangeData queryLevel = new NettyExchangeData();

        queryLevel.setErrorCode(0);
        queryLevel.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        queryLevel.setRequestID(0);
        queryLevel.setMessage("QueryServerLevel");
        queryLevel.setNettyType(ClusterProtocal.FUNCTION.QueryServerLevel);
        queryLevel.setASC_II();

        if(Cluster.isRouterDebug()){
            queryLevel.setExtData(AppManager.getHostName(), 1); //Router message
        }
//        }
        return queryLevel;
    }

    public static void addRouter(NettyExchangeData e, String r) {
        e.setExtData(e.getExtData(1) + "+" + r, 1);
    }

    public static boolean isRouterDebug(NettyExchangeData e) {
        return !getRouter(e).isEmpty();
    }

    public static String getRouter(NettyExchangeData e) {
        return e.getExtData(1);
    }

    public static NettyExchangeData getCloseOthersMsg() {
        if (closeOthersMsg == null) {

            closeOthersMsg = new NettyExchangeData();

            closeOthersMsg.setErrorCode(0);
            closeOthersMsg.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
            closeOthersMsg.setRequestID(0);

            closeOthersMsg.setMessage("CloseOthers");
            closeOthersMsg.setNettyType(ClusterProtocal.FUNCTION.CloseOthers);
            closeOthersMsg.setASC_II();

            closeOthersMsg.setReadOnly(true); // 只需encode 1次

        }

        return closeOthersMsg;
    }

    /**
     * 返回本节点的级别
     *
     * @param data
     * @return
     */
    public static NettyExchangeData getStationProperty(NettyExchangeData data) {
        if (extender != null) {
            return extender.getStationProperty(data);
        }
        if (data.isByPass()) {
            int a = 0;
        }
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(Cluster.getInstance().isOnTrade() ? 1 : 0);
        eData.setEvenType(ClusterProtocal.EVENT.Cluster.ordinal());
        eData.setIpAddress(data.getIpAddress()); //数据进入本站的时候设置了 data.getIpAddress()
        eData.setRequestID(data.getRequestID());
        eData.setMessage("AnswerServerLevel");
        eData.setNettyType(ClusterProtocal.FUNCTION.AnswerServerLevel);
        eData.setASC_II();
//        eData.setRequestID(data.getRequestID());

        eData.appendRow();
        eData.appendRow();
        eData.addData(Cluster.getInstance().getLocalLevel());
        eData.addData(BusinessRuse.getInstance().getCloseDate());  // Col = 1
        eData.addData(BusinessRuse.getInstance().getErrorCode());       // Col = 2
        eData.addData(AppManager.getApp().canRemoteShutdown());      // Col = 3
        eData.addData((Cluster.getInstance().isOnTrade() ? 1 : 0));    // Col = 4
        eData.addData((AppManager.getApp().isDebug() ? 1 : 0));         // Col = 5
        eData.addData(Cluster.getInstance().isOffLine());      // Col = 6
        eData.addData(HardwareIDs.getMACAddress());            // Col = 7
        eData.addData(Internet.getPublicIp());            // Col = 8
        eData.addData(Internet.getVpnIp(vpnInterfaceName));            // Col = 9
//      eData.addData(Cluster.getTradeProxyHost());    // Col = 8  isProxy()

        return eData;
    }


    public static boolean isOnTrade(NettyExchangeData data) {
        return NU.parseInt(data.getDataValue(0, 4), -1) == 1;
    }

    public static boolean isOnDebug(NettyExchangeData data) {
        return NU.parseInt(data.getDataValue(0, 5), -1) == 1;
    }

    public static boolean isOffLine(NettyExchangeData data) {
        return NU.parseInt(data.getDataValue(0, 6), -1) == 1;
    }

    public static boolean isAnswerServer(NettyExchangeData data) {
        return data.getNettyType() == ClusterProtocal.FUNCTION.AnswerServerLevel;
    }

    public static int getServerLeve(NettyExchangeData data) {
        if (data == null) return 0;
        if (isAnswerServer(data)) {
            return NU.parseInt(data.getDataValue(0, 0), 0);
        }

        return 0;
    }


    public static String getCloseDate(NettyExchangeData data) {
        String d = "";
        if (data == null) return "";
        if (isAnswerServer(data)) {
            Object o = data.getDataValue(0, 1);
            if (o != null) {
                d = o.toString();
            }
        }
        return d;
    }

    public static int getAppErrorCode(NettyExchangeData data) {
        if (data == null) return 0;
        if (isAnswerServer(data)) {
            return NU.parseInt(data.getDataValue(0, 2), 0);
        }
        return 0;
    }

    public static String getMack(NettyExchangeData data) {
        if (data == null) return "";
        return data.getExtData(1).toString();
    }

    public static String getInternetIP(NettyExchangeData data) {
        if (data == null) return "";
        return data.getDataValue(0, 8, "").toString();
    }

    public static String getVpnIP(NettyExchangeData data) {
        if (data == null) return "";
        return data.getDataValue(0, 9, "").toString();
    }

    public static boolean canRemoteShutdown(NettyExchangeData data) {
        if (data == null) return false;
        return "true".equalsIgnoreCase((String) data.getDataValue(0, 3));
    }


    public static String tradeProxy(NettyExchangeData data) {
        if (data == null) return "";
        return (String) data.getDataValue(0, 8, "");
    }

    public static void setExtender(StationPropertyWrap extender) {
        StationPropertyWrap.extender = extender;
    }

    public static void setVpnInterfaceName(String vpnInterfaceName) {
        StationPropertyWrap.vpnInterfaceName = vpnInterfaceName;
    }
}

