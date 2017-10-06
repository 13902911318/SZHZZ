package szhzz.Netty.Cluster.ExchangeDataType;


import szhzz.App.AppManager;

/**
 * Created by Administrator on 2015/7/7.
 */
public class StopAppWrap {

    public static NettyExchangeData getStopApp() {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
        eData.setMessage(AppManager.getApp().getAppName());
        eData.setNettyType(ClusterProtocal.FUNCTION.StopApp);


        return eData;
    }

    public boolean isStopThisApp(NettyExchangeData data) {
        return (data.getNettyType() == ClusterProtocal.FUNCTION.StopApp &&
                AppManager.getApp().getAppName().equals(data.getDataValue(0, 0)));
    }
}
