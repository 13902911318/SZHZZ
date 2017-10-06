package szhzz.Netty.Cluster.ExchangeDataType;

import szhzz.Config.Config;
import szhzz.Config.ConfigF;
import szhzz.Utils.DawLogger;

import java.io.*;

/**
 * Created by Administrator on 2015/7/6.
 */
public class CfgFileWrap {
    private static DawLogger logger = DawLogger.getLogger(TxtFileWrap.class);
    private static final String charset = "UTF-8";

    public static NettyExchangeData getCfgWrap(Config cfg, String toFile) {
        NettyExchangeData eData = new NettyExchangeData();
        if (toFile == null) {
            toFile = cfg.getConfigUrl();
        }
        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
        eData.setNettyType(ClusterProtocal.FUNCTION.AmAuctionConfigFile); //ClusterProtocal.FUNCTION.UserData.ordinal()
        eData.setMessage("Config File");

        eData.setExtData("write", 1);
        eData.setExtData(toFile, 2);

        eData.appendRow();

        String[] lines = cfg.getTxt().split("\n");
        for (String s : lines) {
            eData.appendRow();
            eData.addData(s);
        }
        return eData;
    }

    public static NettyExchangeData getDeleteCfgWrap(Config cfg) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
        eData.setNettyType(ClusterProtocal.FUNCTION.AmAuctionConfigFile); //ClusterProtocal.FUNCTION.UserData.ordinal()
        eData.setMessage("Config File");

        eData.setExtData("delete", 1);
        eData.setExtData(cfg.getConfigUrl(), 2);

        return eData;
    }

    public static ConfigF getCfg(NettyExchangeData data) {
        Object fileName = data.getExtData(2);
        ConfigF cfg = new ConfigF();

        if ("".equals(fileName)) return cfg;
        cfg.setConfigFileName(fileName.toString());

        for (int i = 0; i < data.getDataRowCount(); i++) {
            cfg.readLine(data.getDataValue(i, 0, "").toString());
        }
        return cfg;
    }

    public static boolean saveCfg(NettyExchangeData data) {
        PrintWriter out = null;
        boolean isSuccess = false;

        String wORd = data.getExtData(1);
        String fileName = data.getExtData(2);
        if ("".equals(fileName)) return isSuccess;

        File file = new File(fileName);

        if ("delete".equals(wORd)) {
            isSuccess = file.delete();
        } else {
            try {
                out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, false), charset));
                String line;
                for (int row = 1; row < data.getDataRowCount(); row++) {
                    line = (String) data.getDataValue(row, 0, null);
                    if (line == null) break;
                    out.println(line);
                }
                isSuccess = true;
            } catch (IOException e) {
                logger.error(e);
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
        return isSuccess;
    }

}

