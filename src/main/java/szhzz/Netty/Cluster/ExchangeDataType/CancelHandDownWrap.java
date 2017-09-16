package szhzz.Netty.Cluster.ExchangeDataType;

import szhzz.Netty.Cluster.Cluster;

/**
 * Created by Administrator on 2015/7/6.
 * 用于发布改变某一交易计划的级别和开关状态
 */
public class CancelHandDownWrap {
    NettyExchangeData data = null;
    Integer row = 0;
    private static int sirialNumber = 0;

    public static NettyExchangeData getCancelHandDown(String accountName, String accountID, String agentID, String stockCode, String orderNumber) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.HandDown.ordinal());
        eData.setRequestID(0);
        eData.setMessage( Cluster.getHostName() + "CANCEL:" + (++sirialNumber));
        eData.setNettyType(ClusterProtocal.FUNCTION.CancelHandDown);

        eData.appendRow();

        eData.appendRow();
        eData.addData(accountName);
        eData.addData(accountID);
        eData.addData(agentID);
        eData.addData(stockCode);
        eData.addData(orderNumber);

        return eData;
    }


    public CancelHandDownWrap(NettyExchangeData data) {
        this.data = data;
    }


    public String getAccountName() {
        if (data == null) return null;
        return (String) data.getDataValue(row, 0);
    }

    public String getAccountID() {
        if (data == null) return null;
        return (String) data.getDataValue(row, 1);
    }

    public String getAgentID() {
        if (data == null) return null;
        return (String) data.getDataValue(row, 2);
    }

    public String getStockCode() {
        if (data == null) return null;
        return (String) data.getDataValue(row, 3);
    }

    public String getOrderNumber() {
        if (data == null) return null;
        return (String) data.getDataValue(row, 4);
    }
}

