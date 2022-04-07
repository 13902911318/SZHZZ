package szhzz.STOCK;

import szhzz.App.AppManager;
import szhzz.App.MessageAbstract;
import szhzz.App.MessageCode;
import szhzz.Calendar.MyDate;
import szhzz.PinYin.getPingYin;
import szhzz.Utils.DawLogger;
import szhzz.sql.database.DBException;
import szhzz.sql.database.Database;
import szhzz.sql.jdbcpool.DbStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Project: SZHZZ
 * Package: szhzz.STOCK
 * <p>
 * User: HuangFang
 * Date: 2022/2/22
 * Time: 20:53
 * <p>
 * Created with IntelliJ IDEA
 */
public class StockNameExp {
    private static DawLogger logger = DawLogger.getLogger(StockNameExp.class);

    static StockNameExp stockName = null;
    private static Hashtable<String, List<String>> stockNames = new Hashtable<String, List<String>>();
    private static Vector<List<String>> pyTable = null;  //ComboBox 需要 Vector 参数
    private static String lastStockCode = "";
    private static String lastStocName = "";
    private static PYComparator pyComparator = new PYComparator();
    private static ArrayList<String> tradeAble = null;
    private static ArrayList<String> auditCode = null;

    StockNameExp() {
        retrieve(null);
    }

    public static StockNameExp getInstance() {
        if (stockName == null) {
            stockName = new StockNameExp();
        }
        return stockName;
    }

    public static void refresh(Database db) {
        if (stockName == null) {
            stockName = new StockNameExp();
        }
        stockName.retrieve(db);
        if (pyTable == null) {
            pyTable = new Vector<List<String>>();
        } else {
            pyTable.clear();
        }
        pyTable.addAll(stockNames.values());
        Collections.sort(pyTable, pyComparator);
    }

    public static Hashtable<String, List<String>> getHashtable() {
        return getInstance().stockNames;
    }

    public static List<String> getStockCodesMarketA() {
        ArrayList<String> stockCodes_ = new ArrayList<String>();
        stockCodes_.add("SH1A0001");
        stockCodes_.add("SZ399001");

        getInstance();
        for (Enumeration e = stockNames.keys(); e.hasMoreElements(); ) {
            String code = (String) e.nextElement();
            if (code == null) continue;
//            if (isMarket_A_Stock(code)) {
            if (MarketInformation.isManagedStock(code)) {
                stockCodes_.add(code);
            }
        }
        return stockCodes_;
    }

    /**
     * 股票,主要指数,ETF, 板块
     *
     * @param db
     * @return TODO changeName to getManagedStocks()
     */
    public static Vector<String> getDayLineStocks(Database db) {
        ResultSet rs = null;

        Vector<String> stockCodes_ = new Vector<String>();
        String sql = "SELECT DISTINCT stockCode " +
                " FROM DayLine " +
                " WHERE  " + MarketInformation.getManagedCodeReg();  // 已含 ETF交易，主要指数

        // TODO 恢复以便加入 ETF 交易
//                "stockCode regexp '^SZ00|^SZ30|^SH60|^SH51|^SZ200|^SZ399|^SZ159|^SH1A|^SH1B|^88|^SH688' "; //|^SZ900


        try {
            rs = db.dynamicSQL(sql);
            while (rs.next()) {
                stockCodes_.add(rs.getString(1));
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            Database.closeResultSet(rs);
        }
        return stockCodes_;
    }


    public static List<String> getAuditCode(){
        return getAuditCode(null);
    }
    public static List<String> getAuditCode(Database db_) {
        ResultSet rs = null;
        Database db ;
        if(db_ == null){
            db = DbStack.getDb(StockNameExp.class);
        }else{
            db = db_;
        }
        if (auditCode == null) {
            auditCode = new ArrayList<String>();
            String sql = "SELECT DISTINCT stockCode " +
                    " FROM DayLine " +
                    " WHERE  " + MarketInformation.getAuditCode();

            try {
                rs = db.dynamicSQL(sql);
                while (rs.next()) {
                    auditCode.add(rs.getString(1));
                }
            } catch (Exception e) {
                logger.error(e);
            } finally {
                Database.closeResultSet(rs);
                if(db_ == null){
                    DbStack.closeDB(db);
                }
            }
        }
        return (ArrayList<String>) auditCode.clone();
    }

    public static List<String> getTradebleStocks(Database db) {
        ResultSet rs = null;

        if (tradeAble == null) {
            tradeAble = new ArrayList<String>();
            String sql = "SELECT DISTINCT stockCode " +
                    " FROM DayLine " +
                    " WHERE  " + MarketInformation.getTradebleCodeReg();

            try {
                rs = db.dynamicSQL(sql);
                while (rs.next()) {
                    tradeAble.add(rs.getString(1));
                }
            } catch (Exception e) {
                logger.error(e);
            } finally {
                Database.closeResultSet(rs);
            }
        }
        return (ArrayList<String>) tradeAble.clone();
    }

    /**
     * 股票主要指数,ETF
     *
     * @param date
     * @return
     */
    public static Vector<String> getDayLineStocks(String date) {
        ResultSet rs = null;

        Vector<String> stockCodes_ = new Vector<String>();
        String sql = "SELECT DISTINCT stockCode " +
                " FROM DayLine " +
                " WHERE " + MarketInformation.getManagedCodeReg();

        if (date != null) {
            sql += " AND  tradedate = '" + date + "' ";
        }

        Database db = DbStack.getDb(StockNameExp.class);
        try {
            rs = db.dynamicSQL(sql);
            while (rs.next()) {
                stockCodes_.add(rs.getString(1));
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            Database.closeResultSet(rs);
            DbStack.closeDB(db);
        }
        return stockCodes_;
    }


    public static Vector<String> getShenZhen_B() {
        Vector<String> stockCodes_ = new Vector<String>();

        StockNameExp stocks = getInstance();
        for (Enumeration e = stockNames.keys(); e.hasMoreElements(); ) {
            String code = (String) e.nextElement();
            if (code.startsWith("SZ200")) {
                stockCodes_.add(code);
            }
        }
        return stockCodes_;
    }


    public static Vector<String> getShangHai_B() {
        Vector<String> stockCodes_ = new Vector<String>();

        StockNameExp stocks = getInstance();
        for (Enumeration e = stockNames.keys(); e.hasMoreElements(); ) {
            String code = (String) e.nextElement();
            if (code.startsWith("SH9009")) {
                stockCodes_.add(code);
            }
        }
        return stockCodes_;
    }

    public static Vector<String> getTHS88X(String codePrefix) {
        Vector<String> stockCodes_ = new Vector<String>();

        StockNameExp stocks = getInstance();
        for (String code : stockNames.keySet()) {
            if (code.startsWith(codePrefix)) {
                stockCodes_.add(code);
            }
        }
        return stockCodes_;
    }

    public static Vector<String> getETF() {
        Vector<String> stockCodes_ = new Vector<String>();

        getInstance();
        for (String code : stockNames.keySet()) {
            if (MarketInformation.isManagedETF(code)) {
                stockCodes_.add(code);
            }
        }
        return stockCodes_;
    }

    public static Vector<String> getStockCodesGEM() {
        Vector<String> stockCodes_ = new Vector<String>();

        StockNameExp stocks = getInstance();
        for (Enumeration e = stockNames.keys(); e.hasMoreElements(); ) {
            String code = (String) e.nextElement();
            if (code.startsWith("SZ30")) {
                stockCodes_.add(code);
            }
        }
        return stockCodes_;
    }

    public static Vector<String> getSmallBandStockCodes() {
        Vector<String> stockCodes_ = new Vector<String>();

        StockNameExp stocks = getInstance();
        for (Enumeration e = stockNames.keys(); e.hasMoreElements(); ) {
            String code = (String) e.nextElement();
            if (code.startsWith("SZ002")) {
                stockCodes_.add(code);
            }
        }
        return stockCodes_;
    }

    public static Vector<String> getStockDebug(String stockcode) {
        Vector stockCodes_ = new Vector<String>();

        stockCodes_.add(stockcode);
        return stockCodes_;
    }

    public static String getStockName(String stockCode) {
        if (stockCode == null) return "";
        if (lastStockCode.equals(stockCode)) return lastStocName;
        getInstance();
        if (stockNames == null || stockNames.get(stockCode) == null) return "";
        lastStockCode = stockCode;
        lastStocName = (String) stockNames.get(stockCode).get(1);
        return lastStocName == null ? "" : lastStocName;
    }

    /**
     * this for THS band only
     * project "Market" maintain SZ,SH
     * @return
     */
    public static int maintainStockNameInBack() {
        String pyCode;
        String brand;
        ResultSet rs = null;

        int updatedCount = 0;
        HashMap<String, String> aliasNames = new HashMap<>();
        Database db = DbStack.getDb(StockNameExp.class);


        try {

            String sql = "SELECT " +
                    " stockCode, " +
                    " stockName" +
                    " FROM StockName_A " +
                    " WHERE stockCode like '88%'";

            rs = db.dynamicSQL(sql);

            while (rs.next()) {
                String stockCode_A = rs.getString(1);
                if (stockCode_A == null) continue;
                String stockName_A = rs.getString(2);
                if (stockName_A == null) continue;

                aliasNames.put(stockCode_A, stockName_A);
            }
            Database.closeResultSet(rs);

//            String del = "DELETE FROM StockName WHERE stockCode like '88%'";
//            try {
//                db.executeUpdate(del);
//            } catch (Exception e) {
//                logger.error(del, e);
//            }

            brand = "BAND";
            for (String stockCode : aliasNames.keySet()) {
                String stockName = aliasNames.get(stockCode);
                pyCode = getPingYin.getPinYinHeadChar(stockName);

                if (pyCode.length() > 10) {
                    pyCode = pyCode.substring(0, 9);
                }

                sql = "INSERT INTO StockName (" +
                        " stockCode, " +
                        " stockName, " +
                        " PyCode," +
                        " Brand " +
                        ") VALUES (" +
                        "'" + stockCode + "'," +
                        "'" + stockName + "'," +
                        "'" + pyCode + "'," +
                        "'" + brand + "'" +
                        ") " +
                        " ON DUPLICATE KEY UPDATE " +
                        " stockName = '" + stockName + "'," +
                        " PyCode = '" + pyCode + "'," +
                        " Brand = '" + brand + "';";
                try {
                    db.executeUpdate(sql);
                    updatedCount++;
                } catch (Exception e) {
                    logger.error(sql, e);
                }
            }
            if (updatedCount > 0) {
                refresh(db);
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            DbStack.closeDB(db);
        }
        // TODO check log!
        AppManager.logit("THS Band changed! " + updatedCount + " ROWS.");
        MessageAbstract.getInstance().sendMessage(MessageCode.StockCodesTableChanged, "StockCodesTableChanged");
        return updatedCount;
    }

    static public Vector<String> getMaintainStockCode() {
        Vector<String> stocks = new Vector<String>();
        ResultSet rs = null;
        Database db = DbStack.getDb(StockNameExp.class);
        String sql = "select stockCode FROM marketanalyze\n" +
                "GROUP BY stockCode\n" +
                "having max(Tradedate) < '2010-02-12'\n";
        try {
            rs = db.dynamicSQL(sql);
            while (rs.next()) {
                stocks.add(rs.getString(1));
            }
        } catch (Exception e) {

        } finally {
            Database.closeResultSet(rs);
            DbStack.closeDB(db);
        }
        return stocks;
    }

    /**
     * get All stock codes
     *
     * @param db
     */
    void retrieve(Database db) {
        boolean closeDb = false;
        ResultSet rs = null;
        try {
            if (db == null) {
                db = DbStack.getDb(this.getClass());
                closeDb = true;
            }

            String sql = "SELECT DISTINCT " +
                    "stockCode, stockName, PyCode " +
                    "FROM StockName ORDER BY stockCode ";
            rs = db.dynamicSQL(sql);

            stockNames.clear();

            while (rs.next()) {
                String stockCode = rs.getString(1);
                if (stockCode != null) {
                    String stockName = rs.getString(2);
                    String pyCode = rs.getString(3);
                    Vector r = new Vector();
                    r.add(stockCode);
                    r.add(stockName);
                    r.add(pyCode);
                    stockNames.put(stockCode, r);
                }
            }

            //未上市的新股
            sql = "SELECT DISTINCT " +
                    " ApplyCode, stockName " +
                    " FROM newIpo " +
                    " where TradeDate > '" + MyDate.getToday().getDate() + "'" +
                    " ORDER BY stockCode ";
            rs = db.dynamicSQL(sql);

            while (rs.next()) {
                String stockCode = rs.getString(1);

                if (stockCode != null) {
                    if (stockNames.containsKey(stockCode)) continue;

                    String stockName = rs.getString(2);
                    String pyCode = getPingYin.getPinYinHeadChar(stockName);
                    Vector r = new Vector();
                    r.add(stockCode);
                    r.add(stockName);
                    r.add(pyCode);
                    stockNames.put(stockCode, r);
                }
            }
        } catch (DBException e) {
            logger.error(e);
        } catch (SQLException e) {
            logger.error(e);  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            Database.closeResultSet(rs);
            if (closeDb && db != null) db.close();
        }
    }

    public Vector<List<String>> getPyTable() {
        if (pyTable == null) {
            pyTable = new Vector<List<String>>();
            pyTable.addAll(stockNames.values());
            Collections.sort(pyTable, pyComparator);
        }
        return pyTable;
    }

    static class PYComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 == null) return -1;
            if (o2 == null) return 1;

            Vector<String> v1 = (Vector) o1;
            Vector<String> v2 = (Vector) o2;
            if (v1.size() < 3 || v1.get(2) == null) return 1;
            if (v2.size() < 3 || v2.get(2) == null) return -1;
            return v1.get(2).compareTo(v2.get(2));
        }
    }

//    static class exeInBack implements Runnable {
//        int retval;
//
//
//        public void run() {
//            retval = maintanStockNameInBack();
//        }
//        //executeInBack(Runnable r)
//    }

    void test() {
        ResultSet rs = null;
        String sql = "SELECT " +
                " stockCode, " +
                " stockName" +
                " FROM StockName_A " +
                " order by 1 ";
        int count = 0;
        Database db = DbStack.getDb(this.getClass());
        try {
            rs = db.dynamicSQL(sql);
            while (rs.next()) {
                String stockCode = rs.getString(1);
//                if (stockCode.startsWith("SH688")) {
//                    int a = 0;
//                }

//                String brand = StockRegulate.stockCatigory(stockCode);
//                if (StockRegulate.isStockCat(brand) || StockRegulate.isIndexCat(brand) || StockRegulate.isFutureCat(brand)) {
                if (MarketInformation.isManaged(stockCode)) {
                    count++;
                    String name = MarketInformation.getProductName(MarketInformation.getBlockOrMarket(stockCode, false));
                    System.out.println(stockCode + "\t" + getStockName(stockCode) + "\t<-" + name);
                }
//                if(MarketInformation.isMarketAStockOrIndex(stockCode) && !MarketInformation.isManaged(stockCode)){
//                    count++;
//                    String name = MarketInformation.getProductName(MarketInformation.getBlockOrMarket(stockCode, false));
//                    System.out.println(stockCode + "\t" + getStockName(stockCode)+ "\t<-" + name);
//                }else if(!MarketInformation.isMarketAStockOrIndex(stockCode) && MarketInformation.isManaged(stockCode)){
//                    count++;
//                    String name = MarketInformation.getProductName(MarketInformation.getBlockOrMarket(stockCode, false));
//                    System.out.println("\t\t\t\t" + stockCode + "\t" + getStockName(stockCode)+ "\t<-" + name);
//                }
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            Database.closeResultSet(rs);
            DbStack.closeDB(db);
        }
        System.out.println("count=" + count);
    }
}
