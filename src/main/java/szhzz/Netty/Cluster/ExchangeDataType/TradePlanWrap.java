package szhzz.Netty.Cluster.ExchangeDataType;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Administrator on 2015/7/7.
 */
public class TradePlanWrap extends TxtFileWrap {
    public TradePlanWrap(NettyExchangeData data) {
        super(data);
    }

    public static NettyExchangeData getTextFile(String fileName) {
        String encode = fileEncode(fileName);
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
//        eData.setFunID(ClusterProtocal.FUNCTION.TradePlan.ordinal()); //ClusterProtocal.FUNCTION.UserData.ordinal()
        eData.setMessage(encode);
        eData.setNettyType(ClusterProtocal.FUNCTION.TradePlan);

        eData.appendRow();
        eData.appendRow();
        eData.addData(fileName);

        FileInputStream in = null;
        try {
            in = new FileInputStream(fileName);
            BufferedReader buff = new BufferedReader(new InputStreamReader(in, encode));

            String tk;
            while ((tk = buff.readLine()) != null) {
                eData.appendRow();
                eData.addData(tk);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {

                }
            }
        }

        return eData;
    }

}
