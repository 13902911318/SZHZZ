package szhzz.Netty.Cluster.ExchangeDataType;

import szhzz.Netty.Cluster.Cluster;

/**
 * Created by Administrator on 2015/7/7.
 */
public class StopSaleWrap {
    NettyExchangeData data = null;
    public StopSaleWrap(NettyExchangeData data) {
        this.data = data;
    }

    public static NettyExchangeData getStopSale(boolean stopSale) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
        eData.setMessage("StopSale");

        eData.setNettyType(ClusterProtocal.FUNCTION.StopSale);

        eData.appendRow();
        eData.appendRow();
        eData.addData(stopSale ? "true" : "false");
        return eData;
    }

    public boolean isStopSale(){
        Object stopSale = data.getDataValue(0,0);
        return stopSale!= null && "true".equalsIgnoreCase(stopSale.toString());
    }
}
