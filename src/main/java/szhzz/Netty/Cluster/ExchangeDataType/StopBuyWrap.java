package szhzz.Netty.Cluster.ExchangeDataType;


/**
 * Created by Administrator on 2015/7/7.
 */
public class StopBuyWrap {
    NettyExchangeData data = null;
    public StopBuyWrap(NettyExchangeData data) {
        this.data = data;
    }

    public static NettyExchangeData getStopBuy(boolean stopBuy) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
        eData.setMessage("StopBuy");
        eData.setNettyType(ClusterProtocal.FUNCTION.StopBuy);

        eData.appendRow();
        eData.appendRow();
        eData.addData(stopBuy ? "true" : "false");
        return eData;
    }

    public boolean isStopBuy() {
        Object stopBuy = data.getDataValue(0, 0);
        return stopBuy != null && "true".equalsIgnoreCase(stopBuy.toString());
    }
}
