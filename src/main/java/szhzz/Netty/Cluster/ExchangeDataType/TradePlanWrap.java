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

    public static NettyExchangeData getTextFile(String fileName, String toFile, boolean sameGroupOnly) {
        String encode = fileEncode(fileName);//可以确保文件已经生成并关闭.
        encode = "UTF-8";

        if (toFile == null) toFile = fileName;

        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
        eData.setNettyType(ClusterProtocal.FUNCTION.TradePlan); //ClusterProtocal.FUNCTION.UserData.ordinal()
        eData.setMessage(encode);
        eData.setExtData("write", 1);
        eData.setExtData("true", 2); //createDir
        eData.setExtData((sameGroupOnly ? "true": "false"), 3);


        eData.appendRow();
        eData.appendRow();
        eData.addData(toFile);

        FileInputStream in = null;

        try {
            in = new FileInputStream(fileName);
            BufferedReader buff = new BufferedReader(new InputStreamReader(in, encode));
            String tk;
            while ((tk = buff.readLine()) != null) {
                if(tk.equals(BoD)){
                    tk = alias_BoD;
                }else if(tk.equals(EoD)){
                    tk = alias_EoD;
                }

                eData.appendRow();
                eData.addData(tk);
            }
        } catch (IOException e) {
//            e.printStackTrace();
            eData = null;
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
