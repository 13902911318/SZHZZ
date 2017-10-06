package szhzz.Netty.Cluster.ExchangeDataType;

import szhzz.Netty.Cluster.Cluster;
import szhzz.Utils.NU;

/**
 * Created by Administrator on 2015/7/6.
 * 用于发布改变某一交易计划的级别和开关状态
 */
public class OrderHandDownWrap {
    NettyExchangeData data = null;
    Integer row = 0;
    private static int sirialNumber = 0;

    public static NettyExchangeData getOrderHandDown(String accountName, String accountID, String stockCode, int stdOrderType, long orderVolume, double orderPrice, int stdTradeSide, int stdHedgeType) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.HandDown.ordinal());
        eData.setRequestID(0);
        eData.setMessage(Cluster.getHostName() + "ORDER:" + (++sirialNumber));
        eData.setNettyType(ClusterProtocal.FUNCTION.OrderHandDown);

        eData.appendRow();
        eData.appendRow();
        eData.addData(accountName);
        eData.addData(accountID);

        eData.addData(stockCode);
        eData.addData(stdOrderType);
        eData.addData(orderVolume);
        eData.addData(orderPrice);
        eData.addData(stdTradeSide);
        eData.addData(stdHedgeType);


        return eData;
    }


    public OrderHandDownWrap(NettyExchangeData data) {
        this.data = data;
    }

    public String getCpuID() {
        if (data == null) return "";
        return data.getCpuID();
    }

    public String getAccountName() {
        if (data == null) return null;
        return (String) data.getDataValue(row, 0);
    }

    public String getAccountID() {
        if (data == null) return null;
        return (String) data.getDataValue(row, 1);
    }

    public String getStockCode() {
        if (data == null) return null;
        return (String) data.getDataValue(row, 2);
    }

    public int getStdOrderType() {
        if (data == null) return -1;
        return NU.parseInt(data.getDataValue(row, 3), -1);
    }

    public long getOrderVolume() {
        if (data == null) return 0L;
        return NU.parseLong(data.getDataValue(row, 4), 0L);
    }

    public double getOrderPrice() {
        if (data == null) return 0d;
        return NU.parseDouble(data.getDataValue(row, 5), 0d);
    }


    public int getStdTradeSide() {
        if (data == null) return 0;
        return NU.parseInt(data.getDataValue(row, 6), 0);
    }

    public int getStdHedgeType() {
        if (data == null) return 0;
        return NU.parseInt(data.getDataValue(row, 7), 0);
    }
}

