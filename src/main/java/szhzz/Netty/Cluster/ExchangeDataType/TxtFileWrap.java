package szhzz.Netty.Cluster.ExchangeDataType;

import szhzz.Config.Config;
import szhzz.Config.ConfigF;
import szhzz.Utils.DawLogger;
import szhzz.Utils.Utilities;

import java.io.*;

/**
 * Created by Administrator on 2015/7/6.
 */
public class TxtFileWrap {
    private static DawLogger logger = DawLogger.getLogger(TxtFileWrap.class);
    NettyExchangeData data = null;
    Integer row = null;

    public TxtFileWrap(NettyExchangeData data) {
        this.data = data;
    }


    public static NettyExchangeData getTextFile(String fileName) {
        String encode = fileEncode(fileName);
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
        eData.setNettyType(ClusterProtocal.FUNCTION.TextFile); //ClusterProtocal.FUNCTION.UserData.ordinal()
        eData.setMessage(encode);

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

    protected static String fileEncode(String fileName) {
        String cs = "UTF-8";
//        try {
//            cs = Utilities.detectCharset(fileName);
//            if (cs == null) {
//                cs = System.getProperty("file.encoding");
//            }
//        } catch (IOException e) {
//            logger.error(e);
//        }
        return cs;
    }

    public String getFileName() {
        if (data == null) return null;
        return (String) data.getDataValue(0, 0);
    }

    public String getEncode() {
        if (data == null) return null;
        return data.getMessageString();
    }

    public String readLine() {
        if (data == null) return null;
        if (data.getDataRowCount() <= 0) return null;
        if (row == null) {
            row = 1;
        } else {
            row++;
        }
        if (row >= data.getDataRowCount()) return null;

        return (String) data.getDataValue(row, 0, "");
    }


    public void writeToTextFile(String path) {
        String fileName;
        PrintWriter f = null;

        if (getFileName() == null) return;

        String encode = getEncode();
        if (encode == null) {
            encode = System.getProperty("file.encoding");
        }

        if (path != null) {
            File file = new File(getFileName());
            fileName = path + file.getName();
        } else {
            fileName = getFileName();
        }

        try {
            f = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName, false), encode));
            String line;
            while ((line = readLine()) != null) {
                f.println(line);
            }
        } catch (IOException e) {
            logger.error(e);
        } finally {
            if (f != null) {
                f.close();
            }
        }
    }

    public Config getAsConfig() {
        Config cfg = new ConfigF();
        String line;
        while ((line = readLine()) != null) {
            cfg.readLine(line);
        }
        return cfg;
    }
}

