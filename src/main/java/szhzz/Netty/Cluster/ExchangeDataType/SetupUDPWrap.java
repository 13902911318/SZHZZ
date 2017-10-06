package szhzz.Netty.Cluster.ExchangeDataType;

import szhzz.Utils.DawLogger;
import szhzz.Utils.NU;

/**
 * Created by Administrator on 2015/7/6.
 */
public class SetupUDPWrap {
    private static DawLogger logger = DawLogger.getLogger(SetupUDPWrap.class);
    NettyExchangeData data = null;

    public SetupUDPWrap(NettyExchangeData data) {
        this.data = data;
    }


    public static NettyExchangeData getSetupDdpWrap(int port) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal()); //非查询应答
        eData.setRequestID(0);
        eData.setMessage("SetupUDP");
        eData.setExtData(port, 1);
        eData.setNettyType(ClusterProtocal.FUNCTION.SetupUDP);

        eData.appendRow();
        return eData;
    }

    public static int getLocalPort(NettyExchangeData data) {
        return NU.parseInt(data.getExtData(1), 7530);
    }
}

