package szhzz.Netty.Cluster.ExchangeDataType;

import szhzz.Netty.Cluster.Cluster;

/**
 * Created by Administrator on 2015/7/6.
 */
public class InformationWrap {
    NettyExchangeData data = null;

    public static NettyExchangeData getInformationObject(String data) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);

        eData.setMessage("LogInformation");
        eData.setNettyType(ClusterProtocal.FUNCTION.LogInformation);

        eData.appendRow();
        eData.appendRow();
        eData.addData(data);        //信息内容
        return eData;
    }

    public InformationWrap(NettyExchangeData data) {
        this.data = data;
    }


    public String getInformation() {
        if (data == null) return null;
        return (String) data.getDataValue(0, 0);
    }
}

