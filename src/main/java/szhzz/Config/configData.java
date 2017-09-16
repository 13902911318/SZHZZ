package szhzz.Config;


import szhzz.Utils.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: vmuser
 * Date: 2007-11-28
 * Time: 15:20:28
 * To change this template use File | Settings | File Templates.
 */
public class configData {
    public static final int OperationClose = 1;
    public static final int OperationIntime = 2;
    public static final int OperationDayDetal = 3;
    public static final String Encod_GB2312 = setFileEncode();

//    public static final String configFolder = "./configs/";

    static String configFileName = "./Config.txt";
    static Hashtable datas = null;

    static int Op_Modle = -1;

    private static String SystemStartDate = "2007-01-01";

    public static String getSystemStartDate() {
        return SystemStartDate;
    }

    public static void setSystemStartDate(String systemStartDate) {
        SystemStartDate = systemStartDate;
    }

    public static void setConfigFile(String profil) {
        if (profil != null) configData.configFileName = profil;
    }

    private static String setFileEncode() {
        System.setProperty("file.encoding", "GB2312");
        return System.getProperty("file.encoding");
    }

    public static String getVal(String name) {
        if (datas == null) loadData();
        return (String) datas.get(name);
    }

    public static String getVal(String name, String defalt) {
        if (defalt == null) defalt = "null";
        if (datas == null) loadData();
        if (datas.containsKey(name)) {
            String val = (String) datas.get(name);
            if ("null".equals(val)) return null;
            return val;
        }
        datas.put(name, defalt);

        if ("null".equals(defalt)) return null;
        return defalt;
    }

    public static int getIntVal(String name, int defalt) {
        if (datas == null) loadData();
        if (datas.containsKey(name)) {
            return Integer.parseInt((String) datas.get(name));
        }
        datas.put(name, "" + defalt);
        return defalt;
    }

    public static boolean getBooleanVal(String name, boolean defalt) {
        if (datas == null) loadData();
        if (datas.containsKey(name)) {
            return ((String) datas.get(name)).equals("true");
        }
        String val = "false";
        if (defalt) val = "true";
        datas.put(name, val);
        return defalt;
    }

    public static void setBooleanVal(String name, boolean yn) {
        if (datas == null) loadData();
        String val = "false";
        if (yn) val = "true";
        datas.put(name, val);
    }

    public static long getLongtVal(String name, Long defalt) {
        if (datas == null) loadData();
        if (datas.containsKey(name)) {
            return Long.parseLong((String) datas.get(name));
        }
        datas.put(name, "" + defalt);
        return defalt;
    }

    public static void setVal(String name, String val) {
        if (datas == null) loadData();
        if (val == null) val = "null";
        datas.put(name, val);
    }

    static void loadData() {
        BufferedReader buff = null;
        datas = new Hashtable();
        try {
            buff = Utilities.getBufferedReader(configFileName);
            loadDataVal(buff);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if (buff != null) try {
                buff.close();
            } catch (IOException ign) {

            }
        }
    }

    static void loadDataVal(BufferedReader in) {
        String tk;
        String name = null, val = null;
//        boolean BOS = false;
//        String bos = "Start of Values ------------";
//        String eos = "End of Values ------------";
        try {
            while ((tk = in.readLine()) != null) {
//                if (! BOS) {
//                    BOS = tk.startsWith(bos);
//                } else {
//                    if (! tk.startsWith(eos)) return;

                name = Utilities.getEquationLeft(tk);
                if (name != null) {
                    val = Utilities.getEquationRight(tk);
                    if (val != null) datas.put(name, val);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void save() {
        String name;
        if (datas == null) return;

        StringBuffer sb = new StringBuffer("");
        Vector index = new Vector();
        index.addAll(datas.keySet());
        Collections.sort(index);

        for (int i = 0; i < index.size(); i++) {
            name = (String) index.get(i);
            sb.append(name);
            sb.append("=");
            sb.append(datas.get(name));
            sb.append(Utilities.NEW_LINE);
        }
        try {
            Utilities.String2File(sb.toString(), configFileName, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static int OperationModle() {
        if (Op_Modle < 0) {
            String op = configData.getVal("Op_Modle");
            if (op.equalsIgnoreCase("close")) {
                Op_Modle = OperationClose;
            } else if (op.equalsIgnoreCase("detail")) {
                Op_Modle = OperationDayDetal;
            } else {
                Op_Modle = OperationIntime;   // default
            }
        }

        return Op_Modle;
    }

    public static void setOperationModle(int op) {
        Op_Modle = op;
    }


    public static String getDataRootFoulder() {
        String f = getVal("DataBasePath", "./SinaStockData/");
        Utilities.makeDir(f);
        return f;
    }

    public static String getDDHistoryFoulder() {
        String f = getVal("DataBasePath", "./SinaStockData/") + "DD_WebPage/";
        Utilities.makeDir(f);
        return f;
    }

    public static String getClosePriceFoulder() {
        String f = getVal("DataBasePath", "./SinaStockData/") + "ClosePrice/";
        Utilities.makeDir(f);
        return f;
    }

    public static String getDDFoulder() {
        String f = getVal("DataBasePath", "./SinaStockData/") + "DD/";
        Utilities.makeDir(f);
        return f;
    }

    public static String getStockReportFoulder() {
        String f = getVal("DataBasePath", "./SinaStockData/") + "Export/";
        Utilities.makeDir(f);
        return f;
    }

    public static String getVHIReportFoulder() {
        String f = getVal("DataBasePath", "./SinaStockData/") + "VHI/";
        Utilities.makeDir(f);
        return f;
    }

    public static String getOptimizeFoulder() {
        String f = getVal("DataBasePath", "./SinaStockData/") + "Optimizer/";
        Utilities.makeDir(f);
        return f;
    }

    public static String getStocksDaysFoulder() {
        String f = getVal("DataBasePath", "./SinaStockData/") + "Minute/";
        Utilities.makeDir(f);
        return f;
    }

    public static String getStocksListFoulder() {
        String f = getVal("DataBasePath", "./SinaStockData/") + "Stocks/";
        Utilities.makeDir(f);
        return f;
    }

}
