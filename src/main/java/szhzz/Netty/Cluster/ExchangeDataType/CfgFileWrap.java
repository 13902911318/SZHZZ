package szhzz.Netty.Cluster.ExchangeDataType;

import szhzz.Config.Config;
import szhzz.Config.ConfigF;

/**
 * Created by Administrator on 2015/7/6.
 */
public class CfgFileWrap {
    private static final String charset = "UTF-8";

    public static NettyExchangeData getCfgWrap(Config cfg) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
        eData.setNettyType(ClusterProtocal.FUNCTION.TradeConfigFile); //ClusterProtocal.FUNCTION.UserData.ordinal()
        eData.setMessage("Config File");

        eData.appendRow();
        eData.setExtData(cfg.getConfigUrl(), 1);

        String[] lines = cfg.getTxt().split("\n");
        for (String s : lines) {
            eData.appendRow();
            eData.addData(s);
        }
        return eData;
    }

    public static ConfigF getCfg(NettyExchangeData data) {
        Object fileName = data.getExtData(1);
        ConfigF cfg = new ConfigF();

        if ("".equals(fileName)) return cfg;
        cfg.setConfigFileName(fileName.toString());

        for (int i = 0; i < data.getDataRowCount(); i++) {
            cfg.readLine(data.getDataValue(i, 0, "").toString());
        }
        cfg.setProperty("ClusterTimeLap", "" + data.getTimeLap());
        return cfg;
    }
}

