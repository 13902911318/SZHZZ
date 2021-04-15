package szhzz.Netty.Cluster.ExchangeDataType;


import JNI.ExchangeData;
import io.netty.util.internal.StringUtil;
import org.apache.commons.io.FileUtils;
import szhzz.App.AppManager;
import szhzz.Calendar.MyDate;
import szhzz.Netty.Cluster.Cluster;
import szhzz.Utils.DawLogger;
import szhzz.Utils.NU;
import szhzz.Utils.Utilities;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Created by HuangFang on 2015/3/15.
 * 11:08
 */
public class NettyExchangeData extends ExchangeData {
    private static DawLogger logger = DawLogger.getLogger(NettyExchangeData.class);
    private static final String BoD = "[BoD]";
    private static final String EoD = "[EoD]";

//    public static final int colErrCode = 0;   //for all
//    public static final int colLogonID = 1;   //For Trans,Bag.Market.TDF
//    public static final int colEventType = 1; //For Quant
//    public static final int colRequestID = 2; //
//    public static final int colFunID = 3;     //
//    public static final int colMessage = 4;  //


    public static final int colIpAddress = 5;  //
    public static final int colTimeStamp = 6;  //
    public static final int colHostName = 7;  //
    public static final int colNettyType = 8;  //
    public static final int colSubType = 9;  //
    public static final int colLanguage = 10;  //
    public static final int colSerialNo = 11;  //
//    private static final int colExt = 11;  //

    public static final int colGroupID = 12;  //
    public static final int colCpuID = 13;  //
    public static final int colAppClass = 14;  //
    public static final int colMac = 15;  //
    public static final int byPassSignal = 16;  //
    public static final int colForwad = 17;  //
    public static final int colExt = 20;  //

    private String language = "语言认证";  //防止通讯后出现乱码
    String encodeString = null;

    public static boolean isBeggingOfData(String s) {
        return BoD.equals(s);
    }

    public static boolean isEndOfDate(String s) {
        return EoD.equals(s);
    }

    public NettyExchangeData() {
        this.table = new ArrayList<ArrayList>();
    }


    public NettyExchangeData(ExchangeData eData) {
        this.table = (ArrayList<ArrayList>) eData.getTable().clone();
    }

    public NettyExchangeData(ArrayList<String> msg) {
        decode(msg);
    }

    public NettyExchangeData(String msg) {
        String[] a = msg.split("\n");
        for (String s : a) {
            if (isBeggingOfData(s) || isEndOfDate(s)) {
                continue;
            }
            decode(s);
        }
    }

    public void setEvenType(Object coed) {
        setTitleCol(coed, colEventType);
    }
    public void setSubType(Object coed) {
        setTitleCol(coed, colSubType);
    }

    public boolean isSameCharset() {
        Object l = getValue(0, colLanguage);
        if (language.equals(l) || "ASC_II".equals(l)) {
            return true;
        } else {
            logger.error(new Exception("语言编码冲突"));
            logger.info(this.toString());
            return false;
        }
    }

    public boolean isSameGroup(){
        if(Cluster.getInstance()!=null){
            return  Cluster.getInstance().isGroupMenber(getGroup());
        }
        return false;
    }

    private void setLanguage() {
        setTitleCol(language, colLanguage);
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Object getMessage() {
        return getValue(0, colMessage);
    }


    public String getIpAddress() {
        return (String) getValue(0, colIpAddress);
    }

    public String getSubType() {
        return (String) getValue(0, colSubType);
    }

    public void setIpAddress(String address) {
        setTitleCol(address, colIpAddress);
    }

    private void setHostName(String name) {
        setTitleCol(name, colHostName);
    }

    public String getHostName() {
        return (String) getValue(0, colHostName);
    }

    public Long getErrorCode() {
        return NU.parseLong(getValue(0, colErrCode), 0L);
    }

    public void setErrorCode(Object coed) {
        setTitleCol(coed, colErrCode);
    }

    public Long getEventType() {
        return NU.parseLong(getValue(0, colEventType), 0L);
    }

    public ClusterProtocal.EVENT getEvent() {
        return ClusterProtocal.getEvent(getValue(0, colEventType, "-1"));
    }

    public Long getLogonID() {
        return NU.parseLong(getValue(0, colLogonID), 0L);
    }

    public void setForward(boolean isForward) {
        setTitleCol(isForward, colForwad);
    }

    public boolean isForward() {
        return "true".equals(getValue(0, colForwad, "false"));
    }

    public Long getRequestID() {
        return NU.parseLong(getValue(0, colRequestID), 0L);
    }

    public void setRequestID(Object requestID) {
        setTitleCol(requestID, colRequestID);
    }

    public String getTimeStamp() {
        return (String) getValue(0, colTimeStamp);
    }

    private void setTimeStamp() {
        setTitleCol(adjustedTime().getDateTimeFormat("yyyy-MM-dd HH:mm:ss.SSS"), colTimeStamp);
    }

    public MyDate adjustedTime() {
        MyDate n = new MyDate();
//        n.addTimeInMillis(AppEventExchange.getInstance().getTimeOffset());
        return n;
    }

    public long getTimeLap() {
        try {
            MyDate d = new MyDate(getValue(0, colTimeStamp).toString());
            return adjustedTime().getMillisOfDay() - d.getMillisOfDay();
        } catch (Exception ignored) {
            int a = 0;
        }
        return 0;
    }

    public void setExtData(Object o, int col) {
        setTitleCol(o, (colExt + col));
    }

    public void setSerialNo(long serialNo) {
        setTitleCol(serialNo, colSerialNo);
    }

    public long getSerialNo() {
        return NU.parseLong(getValue(0, colSerialNo), -1L);
    }


    public void setNettyType(ClusterProtocal.FUNCTION nettyType) {
        setTitleCol(nettyType.ordinal(), colNettyType);
    }


    /**
     * default 0
     *
     * @return
     */
    public int getGroup() {
        return NU.parseInt(getValue(0, colGroupID), 0);
    }

    private void setGroup(int group) {
        setTitleCol(group, colGroupID);
    }

    public String getCpuID() {
        return (String) getValue(0, colCpuID, "");
    }

    private void setCpuID(String cpuID) {
        setTitleCol(cpuID, colCpuID);
    }

    public String getAppClassName() {
        return (String) getValue(0, colAppClass);
    }

    private void setAppClassName(String appClass) {
        setTitleCol(appClass, colAppClass);
    }

    private void setMac(String cpuID) {
        setTitleCol(cpuID, colMac);
    }

    public void setByPass() {
        setTitleCol(1, byPassSignal);
    }

    public boolean isByPass() {
        return NU.parseInt(getValue(0, byPassSignal), 0) == 1;
    }

    public ClusterProtocal.FUNCTION getNettyType() {
        return ClusterProtocal.FUNCTION.values()[NU.parseInt(getValue(0, colNettyType, "0"), 0)];
    }

    public String getExtData(int col) {
        String retVal = "";

        try {
            if (table != null && table.size() > 0) {
                ArrayList row0 = table.get(0);
                if (row0.size() > (colExt + col)) {
                    Object o = row0.get(colExt + col);
                    if (o != null) {
                        retVal = o.toString();
                    }
                }
            }
        } catch (Exception ignored) {

        }
        return retVal;
    }

    public String encode() {
        if (encodeString != null) return encodeString;

        if (getTable() == null) {
            table = new ArrayList<ArrayList>();
        }
        //Forward 的数据不要改变数据源的信息
        if (StringUtil.isNullOrEmpty(getCpuID())) {
            int groupID = 0;
            setLanguage();
            setCpuID(Cluster.getCpuID());
            setAppClassName(Cluster.getAppClassName());
            setGroup(Cluster.getInstance() == null ? groupID : Cluster.getInstance().getGroup());
            setTimeStamp();
            setHostName(Cluster.getHostName());
            setMac(Cluster.getMac());
        }

        StringBuilder sb = new StringBuilder(BoD).append(nl);
        for (ArrayList row : getTable()) {
            int count = 0;
            for (Object o : row) {
                if(count++ > 0){
                    sb.append("\t");
                }
                sb.append(o);
            }
            sb.append(nl);
        }

        sb.append(EoD).append(nl);
        return sb.toString();
    }

    public String encode(String nl_) {
        if (encodeString != null) return encodeString;

        if (getTable() == null) {
            table = new ArrayList<ArrayList>();
        }
        //Forward 的数据不要改变数据源的信息
        if (StringUtil.isNullOrEmpty(getCpuID())) {
            setLanguage();
            setCpuID(Cluster.getCpuID());
            setAppClassName(Cluster.getAppClassName());
            setGroup(Cluster.getInstance().getGroup());
            setTimeStamp();
            setHostName(Cluster.getHostName());
            setMac(Cluster.getMac());
        }

        StringBuilder sb = new StringBuilder(BoD).append(nl_);
        for (ArrayList row : getTable()) {
            int count = 0;
            for (Object o : row) {
                if(count++ > 0){
                    sb.append("\t");
                }
                sb.append(o);
            }
            sb.append(nl_);
        }

        sb.append(EoD).append(nl_);
        return sb.toString();
    }

    public void setReadOnly(boolean onOff) {
        encodeString = null;
        if (onOff) {
            encodeString = encode();
        }
    }

    public boolean isSameCpuID() {
        return getCpuID().equals(AppManager.getCpuID());
    }

    public void decode(String data) {
        if (getTable() == null) {
            table = new ArrayList<ArrayList>();
        }
        ArrayList currentRecord = new ArrayList();
        getTable().add(currentRecord);
        String[] cols = data.split("\t");
        Collections.addAll(currentRecord, cols);
    }


    public void decode(ArrayList<String> data) {
        if (getTable() == null) {
            table = new ArrayList<ArrayList>();
        }
        for (String line : data) {
            line = line.trim();
            if (isBeggingOfData(line) || isEndOfDate(line)) continue;

            ArrayList currentRecord = new ArrayList();
            getTable().add(currentRecord);
            String[] cols = line.split("\t");
            Collections.addAll(currentRecord, cols);
        }
    }


    public void setASC_II() {
        this.language = "ASC_II";
    }


    public static NettyExchangeData readFile(File file) {
        boolean dataBeging = false;
        boolean dataEnd = false;

        if(file == null)return null;
        if(!file.exists())return null;

        NettyExchangeData eData = new NettyExchangeData();

        FileInputStream in = null;
        String encode = fileEncode(file);//可以确保文件已经生成并关闭.
        encode = "UTF-8";
        String[] element = null;
        try {
            in = new FileInputStream(file);
            BufferedReader buff = new BufferedReader(new InputStreamReader(in, encode));
            String tk;
            while ((tk = buff.readLine()) != null) {

                dataEnd = ( dataEnd|| tk.equals(NettyExchangeData.EoD));

                if(dataEnd) break;

                if(tk.equals(NettyExchangeData.BoD)) {
                    dataBeging = true;
                }else if(dataBeging){
                    element = tk.split("\t");
                    eData.appendRow();
                    eData.addArrayData(element);
                }
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {

                }
            }
        }

        if(dataEnd) return eData;

        return null;
    }

    protected static String fileEncode(File fileName) {
        String cs = null;
        cs = Utilities.detectCharset(fileName);
        if (cs == null) {
            cs = System.getProperty("file.encoding");
        }
        return cs;
    }

    public void writeFile(String file) throws IOException {
        writeFile(new File(file));
    }
    public void writeFile(File file) throws IOException {
        FileUtils.write(file, this.encode(), Charset.forName("UTF-8"));
    }
}
