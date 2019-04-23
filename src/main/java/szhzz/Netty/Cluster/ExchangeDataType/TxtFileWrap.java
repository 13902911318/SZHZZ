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

    public static NettyExchangeData getDeleteFile(String fileName) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
        eData.setNettyType(ClusterProtocal.FUNCTION.TextFile); //ClusterProtocal.FUNCTION.UserData.ordinal()
        eData.setExtData("delete", 1);

        eData.appendRow();
        eData.appendRow();
        eData.addData(fileName);
        return eData;
    }

    public static NettyExchangeData getTextFile(String fileName) {
        return getTextFile(fileName, null);
    }

    public static NettyExchangeData getTextFile(String fileName, String toFile){
        return getTextFile(fileName, toFile, true);
    }
    public static NettyExchangeData getTextFile(String fileName, String toFile, boolean sameGroup) {
        String encode = fileEncode(fileName);//可以确保文件已经生成并关闭.
        encode = "UTF-8";

        if (toFile == null) toFile = fileName;

        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
        eData.setNettyType(ClusterProtocal.FUNCTION.TextFile); //ClusterProtocal.FUNCTION.UserData.ordinal()
        eData.setMessage(encode);
        eData.setExtData("write", 1);
        eData.setExtData("true", 2); //createDir
        eData.setExtData((sameGroup ? "true": "false"), 3);


        eData.appendRow();
        eData.appendRow();
        eData.addData(toFile);

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

    protected static String fileEncode(String fileName) {
        String cs = null;
        try {
            cs = Utilities.detectCharset(fileName);
            if (cs == null) {
                cs = System.getProperty("file.encoding");
            }
        } catch (IOException e) {
            logger.error(e);
        }
        return cs;
    }


    public String getFileName() {
        if (data == null) return null;
        return (String) data.getDataValue(0, 0);
    }

    public boolean isDeleteFile() {
        return "delete".equals(data.getExtData(1));
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

    public boolean makeDirs(){
        return "true".equalsIgnoreCase(data.getExtData(2)); //createDir
    }


    public void writeToTextFile(String path) {
        String fileName = null;
        PrintWriter f = null;

        if (path != null) {
            File file = new File(getFileName());
            fileName = path + "\\" + file.getName();
        } else {
            fileName = getFileName();
        }

        if (fileName == null) return;


        if (isDeleteFile()) {
            logger.info(fileName + " delete Timelaps=" + data.getTimeLap());
            new File(fileName).delete();
            return;
        }
        logger.info(fileName + " saved Timelaps=" + data.getTimeLap());

        if(makeDirs()){
            try {
                new File(fileName).getParentFile().mkdirs();
            }catch (Exception e){

            }
        }

        String encode = getEncode();
        if (encode == null) {
            try {
                encode = System.getProperty("file.encoding");
            }catch (Exception e){}
        }
        if (encode == null) {
            encode = "UTF-8";
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

