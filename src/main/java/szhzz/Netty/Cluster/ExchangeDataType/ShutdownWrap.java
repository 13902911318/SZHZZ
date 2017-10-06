package szhzz.Netty.Cluster.ExchangeDataType;

import szhzz.Utils.DawLogger;

/**
 * Created by Administrator on 2015/7/6.
 */
public class ShutdownWrap {
    private static DawLogger logger = DawLogger.getLogger(ShutdownWrap.class);
    NettyExchangeData data = null;

    public ShutdownWrap(NettyExchangeData data) {
        this.data = data;
    }


    public static NettyExchangeData getShutdownWrap(String status) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
//        eData.setFunID(ClusterProtocal.FUNCTION.Shutdown.ordinal()); //ClusterProtocal.FUNCTION.UserData.ordinal()
        eData.setMessage(status);
        eData.setASC_II();
        eData.setNettyType(ClusterProtocal.FUNCTION.Shutdown);

        eData.appendRow();
        return eData;
    }


//    public void writeToShutdownFile() {
//        try {
//            Utilities.String2File(data.getMessage().toString(), GroupShutdownInspector.getInstance().getFile(), false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}

