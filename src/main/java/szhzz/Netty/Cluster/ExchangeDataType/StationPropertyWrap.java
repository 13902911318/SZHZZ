package szhzz.Netty.Cluster.ExchangeDataType;


import szhzz.App.AppManager;
import szhzz.Utils.NU;
import szhzz.Netty.Cluster.Cluster;
import szhzz.Utils.HardwareIDs;

/**
 * Created by Administrator on 2015/7/6.
 */
public class StationPropertyWrap {
//    NettyExchangeData data = null;
    private static NettyExchangeData closeOthersMsg = null;
    private static NettyExchangeData queryLevel = null;

    public static NettyExchangeData getStationLevelQuery() {
        if (queryLevel == null) {
            queryLevel = new NettyExchangeData();

            queryLevel.setErrorCode(0);
            queryLevel.setEvenType(0);
            queryLevel.setRequestID(0);
            queryLevel.setMessage("QueryServerLevel");
            queryLevel.setNettyType(ClusterProtocal.FUNCTION.QueryServerLevel);
            queryLevel.setASC_II();
        }
        return queryLevel;
    }

    public static NettyExchangeData getCloseOthersMsg() {
        if (closeOthersMsg == null) {

            closeOthersMsg = new NettyExchangeData();

            closeOthersMsg.setErrorCode(0);
            closeOthersMsg.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
            closeOthersMsg.setRequestID(0);
//            closeOthersMsg.setFunID(ClusterProtocal.FUNCTION.CloseOthers.ordinal());  // colFunID
            closeOthersMsg.setMessage("CloseOthers");
            closeOthersMsg.setNettyType(ClusterProtocal.FUNCTION.CloseOthers);
            closeOthersMsg.setASC_II();

            closeOthersMsg.setReadOnly(true); // 只需encode 1次

        }

        return closeOthersMsg;
    }

    /**
     * 返回本节点的级别
     * @param data
     * @return
     */
    public static NettyExchangeData getStationProperty(NettyExchangeData data) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(Cluster.getInstance().isOnTrade() ? 1 : 0);
        eData.setEvenType(0);
        eData.setIpAddress(data.getIpAddress()); //数据进入本站的时候设置了 data.getIpAddress()
        eData.setRequestID(data.getRequestID().intValue());
        eData.setMessage("AnswerServerLevel");
        eData.setNettyType(ClusterProtocal.FUNCTION.AnswerServerLevel);
        eData.setASC_II();

        eData.appendRow();
        eData.appendRow();
        eData.addData(Cluster.getInstance().getLocalLevel());

//        String closeDate = AppEventExchange.getInstance().getEvent("DayClose");
//        if(closeDate == null) closeDate = "";
        String closeDate = "";

        eData.addData(closeDate);  // Col = 1
//        int e = NU.parseInt(AppEventExchange.getInstance().getEvent("持仓修正"),0);
        int e = 0;
        eData.addData(e);       // Col = 2
        eData.addData("false");  // Col = 3
        eData.addData((Cluster.getInstance().isOnTrade()  ? 1 : 0));    // Col = 4
        eData.addData((AppManager.getApp().isDebug() ? 1 : 0));         // Col = 5
        eData.addData(Cluster.getInstance().isOffLine());      // Col = 6
        eData.addData(HardwareIDs.getMACAddress());            // Col = 7

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
            if(o != null){
                d = o.toString();
            }
        }
        return d;
    }

    public static int getPositionError(NettyExchangeData data) {
        if (data == null) return 0;
        if (isAnswerServer(data)) {
            return NU.parseInt(data.getDataValue(0, 2), 0);
        }
        return 0;
    }

    public static boolean isClusterData(NettyExchangeData data) {
        ClusterProtocal.FUNCTION funID = data.getNettyType();
        return funID == ClusterProtocal.FUNCTION.AnswerServerLevel ||
                funID == ClusterProtocal.FUNCTION.CloseOthers ||
                funID == ClusterProtocal.FUNCTION.QueryServerLevel;
    }


    public static String getMack(NettyExchangeData data) {
        if (data == null) return "";
        return data.getExtData(1).toString();
    }


    public static boolean canRemoteShutdown(NettyExchangeData data) {
        if (data == null) return false;
        return "true".equalsIgnoreCase((String) data.getDataValue(0,3));
    }
}

