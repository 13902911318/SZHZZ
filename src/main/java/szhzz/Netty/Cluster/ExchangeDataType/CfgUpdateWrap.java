package szhzz.Netty.Cluster.ExchangeDataType;

import sun.applet.AppletEvent;
import szhzz.App.AppEventExchange;
import szhzz.Config.Config;
import szhzz.Config.ConfigF;
import szhzz.Utils.DawLogger;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 2015/7/6.
 */
public class CfgUpdateWrap {
    private static final String charset = "UTF-8";
    private static DawLogger logger = DawLogger.getLogger(CfgUpdateWrap.class);
    NettyExchangeData data = null;


    public CfgUpdateWrap(NettyExchangeData data) {
        this.data = data;
    }

    public static NettyExchangeData getCfgUpdate(String fileName, String col, String val, String comment) {
        return getCfgUpdate(fileName, new String[]{col}, new String[]{val}, new String[]{comment});
    }

    public static NettyExchangeData getCfgUpdate(String fileName, String col[], String val[], String comment[]) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
        eData.setNettyType(ClusterProtocal.FUNCTION.UpdateConfigValue); //ClusterProtocal.FUNCTION.UserData.ordinal()
        eData.setMessage("Config update");

        eData.appendRow();

        eData.setExtData(fileName, 1);

        for (int i = 0; i < col.length; i++) {
            eData.appendRow();
            eData.addData(col[i]);
            eData.addData(val[i]);
            if (i < comment.length) {
                eData.addData(comment[i]);
            }
        }

        return eData;
    }

    public static NettyExchangeData getAppEventUpdate(String fileName, String col[], String val[], String comment[]) {
        NettyExchangeData eData = getCfgUpdate(fileName, col, val, comment);
        eData.setMessage("AppEventUpdate");
        return eData;
    }

    private void updateAppEventCfg() {
        ArrayList<String> cols = new ArrayList<>();
        ArrayList<String> vals = new ArrayList<>();
        ArrayList<String> comments = new ArrayList<>();

        for (int i = 0; i < data.getDataRowCount(); i++) {
            cols.add(data.getDataValue(i, 0, "").toString());
            vals.add(data.getDataValue(i, 0, "").toString());
            comments.add(data.getDataValue(i, 0, "").toString());
        }
        AppEventExchange.getInstance().setEvent((String[]) cols.toArray(),
                (String[]) vals.toArray(),
                (String[]) comments.toArray(), false);
    }

    public void updateCfg() {
        if("AppEventUpdate".equals(data.getMessage())){
            updateAppEventCfg();
            return ;
        }

        Object fileName = data.getExtData(1);
        if ("".equals(fileName)) return;
        //兼容老程序 TBD
        if("event.ini".equalsIgnoreCase(new File(fileName.toString()).getName())){
            updateAppEventCfg();
            return ;
        }

        Config cfg = new ConfigF();
        cfg.load(fileName.toString());

        for (int i = 0; i < data.getDataRowCount(); i++) {
            Object col = data.getDataValue(i, 0, "");
            Object val = data.getDataValue(i, 1, "");
            Object comment = data.getDataValue(i, 2, "");

            if ("".equals(col) || "".equals(val)) continue;
            cfg.setProperty(col.toString(), val.toString(), comment.toString());
        }
        cfg.save();
    }

    public ConfigF getCfg() {
        Object fileName = data.getExtData(1);
        if ("".equals(fileName)) return null;

        ConfigF cfg = new ConfigF();
        cfg.setConfigFileName(fileName.toString());

        for (int i = 0; i < data.getDataRowCount(); i++) {
            Object col = data.getDataValue(i, 0, "");
            Object val = data.getDataValue(i, 1, "");
            Object comment = data.getDataValue(i, 2, "");

            if ("".equals(col) || "".equals(val)) continue;

            cfg.setProperty(col.toString(), val.toString(), comment.toString());
        }

        return cfg;
    }
}

