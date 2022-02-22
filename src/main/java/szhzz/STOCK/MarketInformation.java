package szhzz.STOCK;

import szhzz.Utils.DawLogger;
import szhzz.Utils.NU;
import szhzz.sql.database.Database;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Project: SZHZZ
 * Package: szhzz.STOCK
 * <p>
 * User: HuangFang
 * Date: 2022/2/22
 * Time: 19:36
 * <p>
 * Created with IntelliJ IDEA
 */
public class MarketInformation {
    private static DawLogger logger = DawLogger.getLogger(MarketInformation.class);

    public static final int BK_SHZS = 0;    //上证指数
    public static final int BK_SHAG = 1;    //上证A股
    public static final int BK_SHBG = 2;    //上证B股
    public static final int BK_SHJJ = 3;    //上证基金
    public static final int BK_SHZQ = 4;    //上证债券
    public static final int BK_SHZZ = 5;    //上证转债
    public static final int BK_SHHG = 6;    //上证回购
    public static final int BK_SHETF = 7;    //上证ETF基金
    public static final int BK_SHKF = 8;    //上证开放基金
    public static final int BK_SHQZ = 9;    //上证权证
    public static final int BK_SHOT = 10;    //上证其它
    public static final int BK_SZZS = 15;    //深圳指数
    public static final int BK_SZAG = 16;    //深证A股
    public static final int BK_SZBG = 17;    //深证B股
    public static final int BK_SZJJ = 18;    //深证基金
    public static final int BK_SZZQ = 19;    //深圳债券
    public static final int BK_SZZZ = 20;    //深圳转债
    public static final int BK_SZHG = 21;    //深圳回购
    public static final int BK_SZETF = 22;    //深证ETF基金
    public static final int BK_SZKF = 23;    //深证开放基金
    public static final int BK_SZQZ = 24;    //深证权证
    public static final int BK_ZXQY = 25;    //中小版
    public static final int BK_CYB = 26;    //创业板
    public static final int BK_SZOT = 27;    //深证其它
    public static final int BK_KCB = 28;    //科创版
    public static final int BK_ZJ = 30;    //中金期货
    public static final int BK_SQ = 32;    //上海期货
    public static final int BK_DL = 33;    //大连期货
    public static final int BK_ZZ = 34;    //郑州期货
    public static final int BK_BOCE = 40;    //渤海商品
    public static final int BK_GJ = 41;    //贵金属
    public static final int BK_SGE = 42;    //上海黄金
    public static final int BK_GZME = 43;    //广东贵金属
    public static final int BK_ID = 50;    //国际指数
    public static final int BK_HK = 60;    //香港股票
    public static final int BK_HKFE = 61;    //恒指期货
    public static final int BK_FOREX = 64;    //外汇汇率
    public static final int BK_CBOT = 65;    //CBOT期货
    public static final int BK_BURSA = 66;    //马来西亚
    public static final int BK_TOCOM = 67;    //日本期货
    public static final int BK_LME = 68;    //伦敦LME
    public static final int BK_COMEX = 69;    //纽约金属
    public static final int BK_NYMEX = 70;    //纽约期货
    public static final int BK_ICE = 71;    //美棉、美糖
    public static final int BK_IP = 72;    //布伦油
    public static final int BK_CME = 73;    //美国CME期货
    public static final int BK_OEM = 80;    //网际风
    public static final int BK_NEIBU = 81;    //网际风内部
    public static final int BK_CHOOSE = 85;    //自选股
    public static final int BK_ZHISHU = 86;    //板块指数
    public static final int BK_THS = 88;    //同花顺
    public static final int BK_FUTRUES = 90;    //所有期货，以代码排序
    public static final int BK_STOCKS = 91;    //所有股票，以代码排序
    public static final int BK_BYNAME = 97;    //所有品种，以名称排序
    public static final int BK_BYLABEL = 98;    //所有品种，以代码排序
    public static final int BK_BYSHIFT = 99;    //所有品种，以大智慧索引排序
    public static final int BK_MAXCOUNT = 100;    //全部板块数量

    public static final int MK_SH = 0;        //上海证券
    public static final int MK_SZ = 1;        //深圳证券
    public static final int MK_ZJ = 10;        //中金期货
    public static final int MK_SQ = 11;        //上海期货
    public static final int MK_DL = 12;        //大连期货
    public static final int MK_BH = 18;        //渤海商品
    public static final int MK_SJ = 19;        //上海黄金
    public static final int MK_ZZ = 20;        //郑州期货
    public static final int MK_HZ = 21;        //恒指期货
    public static final int MK_MY = 22;        //马来西亚
    public static final int MK_JP = 23;        //日本期货
    public static final int MK_HK = 30;        //香港市场
    public static final int MK_GJ = 31;        //贵金属
    public static final int MK_GZ = 32;        //广州贵金属
    public static final int MK_NY = 33;        //纽约原油
    public static final int MK_TJ = 34;        //天津贵金属
    public static final int MK_ID = 40;        //国际指数
    public static final int MK_WH = 41;        //外汇汇率
    public static final int MK_LM = 42;        //伦敦LME
    public static final int MK_CB = 43;        //CBOT期货
    public static final int MK_CM = 44;        //纽约金属
    public static final int MK_ICE = 45;        //美国ICE糖和棉花
    public static final int MK_IP = 46;        //布伦特原油
    public static final int MK_CE = 50;        //美国CME期货
    public static final int MK_NP = 60;        //网际风内部
    public static final int MAX_MARKET_COUNT = 61;    //所有市场数量

    public static final HashMap<Integer, String> maketName = new HashMap<>();    //所有市场数量
    public static final HashMap<Integer, String> productName = new HashMap<>();    //
    private static Pattern shAPattern = Pattern.compile("^SH.*|^60.*|^68.*|^204.*|^51.*|^73.*|^78.*|^1A.*");
    private static Pattern szAPattern = Pattern.compile("^SZ.*|^003.*|^002.*|^001.*|^000.*|^30.*|^399.*");

    public static String getProductName(int code) {
        if (productName.size() == 0) {
            productName.put(BK_SHZS, "上证指数");
            productName.put(BK_SHAG, "上证A股");
            productName.put(BK_SHBG, "上证B股");
            productName.put(BK_SHJJ, "上证基金");
            productName.put(BK_SHZQ, "上证债券");
            productName.put(BK_SHZZ, "上证转债");
            productName.put(BK_SHHG, "上证回购");
            productName.put(BK_SHETF, "上证ETF基金");
            productName.put(BK_SHKF, "上证开放基金");
            productName.put(BK_SHQZ, "上证权证");
            productName.put(BK_SHOT, "上证其它");
            productName.put(BK_SZZS, "深圳指数");
            productName.put(BK_SZAG, "深证A股");
            productName.put(BK_SZBG, "深证B股");
            productName.put(BK_SZJJ, "深证基金");
            productName.put(BK_SZZQ, "深圳债券");
            productName.put(BK_SZZZ, "深圳转债");
            productName.put(BK_SZHG, "深圳回购");
            productName.put(BK_SZETF, "深证ETF基金");
            productName.put(BK_SZKF, "深证开放基金");
            productName.put(BK_SZQZ, "深证权证");
            productName.put(BK_ZXQY, "中小版");
            productName.put(BK_CYB, "创业板");
            productName.put(BK_SZOT, "深证其它");
            productName.put(BK_KCB, "科创版");
            productName.put(BK_ZJ, "中金期货");
            productName.put(BK_SQ, "上海期货");
            productName.put(BK_DL, "大连期货");
            productName.put(BK_ZZ, "郑州期货");
            productName.put(BK_BOCE, "渤海商品");
            productName.put(BK_GJ, "贵金属");
            productName.put(BK_SGE, "上海黄金");
            productName.put(BK_GZME, "广东贵金属");
            productName.put(BK_ID, "国际指数");
            productName.put(BK_HK, "香港股票");
            productName.put(BK_HKFE, "恒指期货");
            productName.put(BK_FOREX, "外汇汇率");
            productName.put(BK_CBOT, "CBOT期货");
            productName.put(BK_BURSA, "马来西亚");
            productName.put(BK_TOCOM, "日本期货");
            productName.put(BK_LME, "伦敦LME");
            productName.put(BK_COMEX, "纽约金属");
            productName.put(BK_NYMEX, "纽约期货");
            productName.put(BK_ICE, "美棉、美糖");
            productName.put(BK_IP, "布伦油");
            productName.put(BK_CME, "美国CME期货");
            productName.put(BK_OEM, "网际风");
            productName.put(BK_NEIBU, "网际风内部");
            productName.put(BK_CHOOSE, "自选股");
            productName.put(BK_ZHISHU, "板块指数");
            productName.put(BK_THS, "同花顺");
            productName.put(BK_FUTRUES, "所有期货，以代码排序");
            productName.put(BK_STOCKS, "所有股票，以代码排序");
            productName.put(BK_BYNAME, "所有品种，以名称排序");
            productName.put(BK_BYLABEL, "所有品种，以代码排序");
            productName.put(BK_BYSHIFT, "所有品种，以大智慧索引排序");
            productName.put(BK_MAXCOUNT, "全部板块数量");
        }
        return productName.get(code);
    }

    public static String getMarketName(int code) {
        if (maketName.size() == 0) {
            maketName.put(MK_SH, "上海证券");
            maketName.put(MK_SZ, "深圳证券");
            maketName.put(MK_ZJ, "中金期货");
            maketName.put(MK_SQ, "上海期货");
            maketName.put(MK_DL, "大连期货");
            maketName.put(MK_BH, "渤海商品");
            maketName.put(MK_SJ, "上海黄金");
            maketName.put(MK_ZZ, "郑州期货");
            maketName.put(MK_HZ, "恒指期货");
            maketName.put(MK_MY, "马来西亚");
            maketName.put(MK_JP, "日本期货");
            maketName.put(MK_HK, "香港市场");
            maketName.put(MK_GJ, "贵金属");
            maketName.put(MK_GZ, "广州贵金属");
            maketName.put(MK_NY, "纽约原油");
            maketName.put(MK_TJ, "天津贵金属");
            maketName.put(MK_ID, "国际指数");
            maketName.put(MK_WH, "外汇汇率");
            maketName.put(MK_LM, "伦敦LME");
            maketName.put(MK_CB, "CBOT期货");
            maketName.put(MK_CM, "纽约金属");
            maketName.put(MK_ICE, "美国ICE糖和棉花");
            maketName.put(MK_IP, "布伦特原油");
            maketName.put(MK_CE, "美国CME期货");
            maketName.put(MK_NP, "网际风内部");
            maketName.put(MAX_MARKET_COUNT, "所有市场数量");
        }
        return maketName.get(code);
    }

    public static int getBlockOrMarket(String stockCode, boolean getMarket) {
        if (stockCode.length() < 8) {
            if (stockCode.startsWith("88")) {
                if (getMarket)
                    return MK_SZ;
                return BK_THS;
            }

            return -1;
        }
        String market = stockCode.substring(0, 2);
        char[] securityCode = new char[8]; //
        stockCode.getChars(0, 8, securityCode, 0);
        switch (market) {
            case "SH":
            case "B$": {
                if (getMarket)
                    return MK_SH;

                if (securityCode[2] == '6') {
                    if (securityCode[3] == '8' && securityCode[4] == '8')        //SH01****
                        return BK_KCB;//科创版

                    return BK_SHAG;//上证A股
                } else if (securityCode[2] == '0') {
                    if (securityCode[3] == '0')
                        return BK_SHZS;//上证指数	SH00****
                    else if (securityCode[3] == '1')        //SH01****
                        return BK_SHZQ;//上证债券
                } else if (securityCode[2] == '1') {
                    if (securityCode[3] == 'A' || securityCode[3] == 'B' || securityCode[3] == 'C')
                        return BK_SHZS;//上证指数	SH1A****
                    else if (securityCode[3] == '0' || securityCode[3] == '1')
                        return BK_SHZZ;//上证转债
                    else if (securityCode[3] == '2' || securityCode[3] == '3')
                        return BK_SHZQ;//上证债券
                } else if (securityCode[2] == '2') {
                    if (securityCode[3] == '0')
                        return BK_SHHG;//上证回购
                } else if (securityCode[2] == '5') {
                    if (securityCode[3] == '0')
                        return BK_SHJJ;//上证基金
                    else if (securityCode[3] == '1' && securityCode[4] == '9' || securityCode[3] == '2' && securityCode[4] == '0')
                        return BK_SHKF;//开放式基金  519###, 520###
                    else if (securityCode[3] == '1')
                        return BK_SHETF;//上证ETF基金51###
                } else if (securityCode[2] == '9') {
                    if (securityCode[3] == '0')
                        return BK_SHBG;//上证B股
                    else if (securityCode[3] == '9')
                        return BK_ZHISHU; //BK_SHZS;        //板块指数
                }
            }
            break;
            //399###, 0;002###, 6;03####, 7;00####, 1;20####, 2;15####, 3;16####, 3;18####, 3;1#####, 4;30####, 5;######, 8
            case "SZ": {
                if (getMarket)
                    return MK_SZ;

                if (securityCode[2] == '0') {
                    if (securityCode[3] == '0') {
                        if (securityCode[4] == '2')
                            return BK_ZXQY;//中小版
                        else
                            return BK_SZAG;//深证A股
                    } else if (securityCode[3] == '3') {
                        return BK_SZQZ;//深证权证
                    } else if (securityCode[3] == '7' || securityCode[3] == '8') {
                        return BK_SZQZ;//深证债券
                    }
                } else if (securityCode[2] == '1') {
                    if (securityCode[3] == '0' || securityCode[3] == '1')
                        return BK_SZZQ;//深证债券
                    else if (securityCode[3] == '2')
                        return BK_SZZZ;//深证转债
                    else if (securityCode[3] == '3')
                        return BK_SZHG;//深证回购
                    else if (securityCode[3] == '5' && securityCode[4] == '9')
                        return BK_SZETF;//SZ159深证ETF基金
                    else if (securityCode[3] == '5' && securityCode[4] == '0')
                        return BK_SZJJ;//SZ150深证基金
                    else if (securityCode[3] == '6')
                        return BK_SZJJ;//SZ16深证LOF基金
                    else if (securityCode[3] == '8')
                        return BK_SZJJ;//SZ18深证基金
                } else if (securityCode[2] == '2') {
                    return BK_SZBG;//深证B股
                } else if (securityCode[2] == '3') {
                    if (securityCode[3] == '9' && securityCode[4] == '9')
                        return BK_SZZS;//深证指数
                    else if (securityCode[3] == '0')
                        return BK_CYB;//创业板
                    else
                        return BK_SZOT;//深证其它
                }
            }
            break;
            case "ZJ": {
                if (getMarket)
                    return MK_ZJ;
                return BK_ZJ;
            }
            case "SQ": {
                if (getMarket)
                    return MK_SQ;
                return BK_SQ;
            }
            case "DL": {
                if (getMarket)
                    return MK_DL;
                return BK_DL;
            }
            case "ZZ": {
                if (getMarket)
                    return MK_ZZ;
                return BK_ZZ;
            }
            case "BH": {
                if (getMarket)
                    return MK_BH;
                return BK_BOCE;
            }
            case "GJ": {
                if (getMarket)
                    return MK_GJ;
                return BK_GJ;
            }
            case "SJ": {
                if (getMarket)
                    return MK_SJ;
                return BK_SGE;
            }
            case "NY": {
                if (getMarket)
                    return MK_NY;
                return BK_NYMEX;
            }
            case "GZ":    //广东贵金属交易所
            {
                if (getMarket)
                    return MK_GZ;
                return BK_GZME;
            }
            case "HK": {
                if (getMarket)
                    return MK_HK;
                return BK_HK;
            }
            case "ID": {
                if (getMarket)
                    return MK_ID;
                return BK_ID;
            }
            case "WH": {
                if (getMarket)
                    return MK_WH;
                return BK_FOREX;
            }
            case "LM": {
                if (getMarket)
                    return MK_LM;
                return BK_LME;
            }
            case "CB": {
                if (getMarket)
                    return MK_CB;
                return BK_CBOT;
            }
            case "CM": {
                if (getMarket)
                    return MK_CM;
                return BK_COMEX;
            }
            case "IC": {
                if (getMarket)
                    return MK_ICE;
                return BK_ICE;
            }
            case "JP":        //日本期货
            {
                if (getMarket)
                    return MK_JP;
                return BK_TOCOM;
            }
            case "MY": {
                if (getMarket)
                    return MK_MY;
                return BK_BURSA;
            }
            case "CX": {
                if (getMarket)
                    return MK_CE;
                return BK_CME;
            }
            case "HZ":    //恒指
            {
                if (getMarket)
                    return MK_HZ;
                return BK_HKFE;
            }
            case "NP":    //网际风内部
            {
                if (getMarket)
                    return MK_NP;
                return BK_NEIBU;
            }
            case "88": {
                if (getMarket)
                    return MK_SZ;
                return BK_THS;
            }
            default:
                break;
        }
        return -1;
    }

    public static boolean isCanTrade(String stockCode) {
        int blockID = getBlockOrMarket(stockCode, false);
        return (blockID == BK_SHAG || //上证A股
                blockID == BK_SHHG ||   //上证回购
                blockID == BK_SZAG ||   //深证A股
                blockID == BK_SZHG ||   //深圳回购
                blockID == BK_ZXQY ||   //中小版
                blockID == BK_CYB);    //创业板
    }

    public static boolean isIndexA(String stockCode) {
        int blockID = getBlockOrMarket(stockCode, false);
        return (blockID == BK_SHZS ||    //上证指数
                blockID == BK_SZZS ||        //深圳指数
                blockID == BK_THS                //同花顺
        );
    }

    public static boolean isMarketA(String stockCode) {
        int blockID = getBlockOrMarket(stockCode, false);
        return (blockID == BK_SHAG || //上证A股
                blockID == BK_SHHG ||   //上证回购
                blockID == BK_SZAG ||   //深证A股
                blockID == BK_SZHG ||   //深圳回购
                blockID == BK_ZXQY ||   //中小版
                blockID == BK_CYB ||    //创业板
                blockID == BK_KCB ||    //科创板
                blockID == BK_SHZS ||    //上证指数
                blockID == BK_SZZS ||        //深圳指数
                blockID == BK_THS ||          //同花顺
                blockID == BK_SHETF ||      //上证ETF基金
                blockID == BK_SZETF         //深证ETF基金
        );
    }

    public static boolean isMarketAStockOrIndex(String stockCode) {
        int blockID = getBlockOrMarket(stockCode, false);
        return (blockID == BK_SHAG || //上证A股
                blockID == BK_SHHG ||   //上证回购
                blockID == BK_SZAG ||   //深证A股
                blockID == BK_SZHG ||   //深圳回购
                blockID == BK_ZXQY ||   //中小版
                blockID == BK_CYB ||    //创业板
                blockID == BK_KCB ||    //科创板
                blockID == BK_SHZS ||    //上证指数
                blockID == BK_SZZS ||        //深圳指数
                blockID == BK_THS ||          //同花顺
                blockID == BK_SHETF ||      //上证ETF基金
                blockID == BK_SZETF         //深证ETF基金

        );
    }

    public static boolean isMarketAStock(String stockCode) {
        int blockID = getBlockOrMarket(stockCode, false);
        return (blockID == BK_SHAG || //上证A股
                blockID == BK_SZAG ||   //深证A股
                blockID == BK_ZXQY ||   //中小版
//                blockID == BK_SHETF ||   //上证ETF基金
                blockID == BK_KCB || //科创版
                blockID == BK_CYB);    //创业板
    }

    ///////////////////////////// New Add 2020-09-14
    private static final Pattern managedStock = Pattern.compile("^SZ00.*|^SZ30.*|^SH60.*|^SH68.*");
    private static final Pattern managedETF = Pattern.compile("^SH51.*|^SZ159.*");
    private static final Pattern managedIndex = Pattern.compile("^SZ399.*|^SH1A.*|^SH00.*|^SH1B.*|^88.*");
    private static final Pattern managedRevert = Pattern.compile("^SZ131.*|^SH204.*");


    public static String getAuditCode() {
        return " stockCode REGEXP " +
                "'^SZ00|^SZ30|^SH60|^SH68|" +  //股票
                "^SH51|^SZ159|" +               //ETF
                "^SZ399|^SH1A|^SH00|^88'"; //主要指数
    }

    public static String getManagedCodeReg() {
        return " stockCode REGEXP " +
                "'^SZ00|^SZ30|^SH60|^SH68|" +  //股票
                "^SH51|^SZ159|" +               //ETF
                "^SZ399|^SH1A|^SH00|^SH1B|^88|" + //主要指数
                "^SZ131|^SH204' ";              //逆回购

//        " '^SZ00|^SZ30|^SH60|^SH51|^SZ200|^SZ399|^SZ159|^SH1A|^SH1B|^88|^SH688' "
    }

    /**
     * 股票加ETF
     *
     * @return
     */
    public static String getTradebleCodeReg() {
        return " stockCode REGEXP " +
                "'^SZ00|^SZ30|^SH60|^SH68|" +
                "^SH51|^SZ159|" +
                "^SZ131|^SH204' " +
                "";
    }


//    // For sql
//    /**
//     * 可交易标的
//     *
//     * @return
//     */
//    public static String getTradebleReg() {
//        return " stockCode REGEXP '^SZ00|^SZ30|^SH60|^SH688|^SH51|^SZ159'";
//    }

    public static String getSZ_A_StockRegexp() {
        return " stockCode REGEXP '^SZ00[0-9]{4}|^SZ30[0-9]{4}'";
    }

    public static String getMarket_A_IndexRegexp() {
        return " stockCode REGEXP '^SH51[0-9]{4}|^SZ399[0-9]{4}|^SH1A[0-9]{4}|^SH1B[0-9]{4}|^88' ";
    }

    public static String getMarketA_StockRegexp(String alias) {
        return " " + alias + ".stockCode REGEXP '^SZ00[0-9]{4}|^SZ30[0-9]{4}|^SH60[0-9]{4}|^SH68[0-9]{4}' ";
    }

    public static String getMarketA_StockRegexp() {
        return " stockCode REGEXP '^SZ00[0-9]{4}|^SZ30[0-9]{4}|^SH60[0-9]{4}|^SH68[0-9]{4}' ";
    }


    /**
     * @param stockCode
     * @return
     */
    public static boolean isManaged(String stockCode) {
        return isManagedStock(stockCode) || isManagedIndex(stockCode)
                || isManagedETF(stockCode) || isManagedRevert(stockCode);
    }


    public static boolean isManagedStock(String stockCode) {
        return managedStock.matcher(stockCode).matches();
    }

    public static boolean isManagedIndex(String stockCode) {
        return managedIndex.matcher(stockCode).matches();
    }

    public static boolean isManagedRevert(String stockCode) {
        return managedRevert.matcher(stockCode).matches();
    }

    public static boolean isManagedETF(String stockCode) {
        return managedETF.matcher(stockCode).matches();
    }

    public static boolean isTradeble(String stockCode) {
        return isManagedStock(stockCode) || isManagedETF(stockCode) || isManagedRevert(stockCode);
    }

    public static boolean isManagedStockOrETF(String stockCode) {
        return isManagedStock(stockCode) || isManagedETF(stockCode);
    }

    public static boolean isMarketSHA(String code) {
        return getShAPattern().matcher(code).matches();
    }

    public static boolean isMarketSZA(String code) {
        return getSzAPattern().matcher(code).matches();
    }

    public static Pattern getShAPattern() {
        if (shAPattern == null) {
            shAPattern = Pattern.compile("^SH.*|^60.*|^68.*|^204.*|^51.*|^73.*|^78.*|^1A.*");
        }
        return shAPattern;
    }

    public static Pattern getSzAPattern() {
        if (szAPattern == null) {
            szAPattern = Pattern.compile("^SZ.*|^003.*|^002.*|^001.*|^000.*|^30.*|^399.*");
        }
        return szAPattern;
    }

    /**
     * in public static boolean isSusbendSignal(String mark)
     * suspendCode = s.charAt(0);
     * }
     * if (suspendCode == 0) return false;
     * if (suspendCode == 72) return true; // X SUSP - 停牌
     * if (suspendCode == 66) return true; // B 整天停牌
     * //                if (suspendCode == 65) return true; // A 交易节休市
     * if (suspendCode == 67) return true; // C 全天收市
     * if (suspendCode == 68) return true; // D 暂停交易
     * //                if (suspendCode == 69) return true; // G DEL - 不可恢复交易的熔断阶段（上交所的N）
     * //                if (suspendCode == 70) return true; // H HOLIDAY - 放假
     * //                if (suspendCode == 71) return true; // P BREAK - 休市
     * if (suspendCode == 88) return true; // X 老版标志
     * if (suspendCode == 89) return true; // Y 老版标志
     *
     * @return
     */
    public static String decodeStatus(String stockCode, String status) {
        //以下仅国泰安数据适用. LJF 无状态标志
        try {
            if (status == null || status.isEmpty()) return "";
            if (isMarketSHA(stockCode)) {
                // SecurityPhaseTag[PHRASE_TAG_LEN];   ///< 当前品种交易状态
                ///< 该字段为8位字符串，左起每位表示特定的含义，无定义则填空格。
                ///< 第1位：‘S’表示启动（开市前）时段，‘C’表示集合竞价时段，‘T’表示连续交易时段，‘E’表示闭市时段，‘P’表示产品停牌，‘M’表示可恢复交易的熔断时段（盘中集合竞价），‘N’表示不可恢复交易的熔断时段（暂停交易至闭市），‘U’表示收盘集合竞价时段。
                ///< 第2位：‘0’表示此产品不可正常交易，‘1’表示此产品可正常交易，无意义填空格。
                ///< 第3位：‘0’表示未上市，‘1’表示已上市。
                ///< 第4位：‘0’表示此产品在当前时段不接受进行新订单申报，‘1’ 表示此产品在当前时段可接受进行新订单申报。无意义填空格。
                switch (status.charAt(0)) {
                    case 'N':       //‘N’表示不可恢复交易的熔断时段（暂停交易至闭市）
                        return "X"; //X SUSP - 停牌
                    case 'P':       //‘P’表示产品停牌
                        return "B"; //B 整天停牌
                    case 'M':       //‘M’表示可恢复交易的熔断时段（盘中集合竞价）
                        return "D"; //D 暂停交易
                }
                if (status.charAt(1) == '0') { //第2位：‘0’表示此产品不可正常交易
                    return "B"; //B 整天停牌
                }
                if (status.charAt(2) == '0') { //第3位：‘0’表示未上市，‘1’表示已上市。
                    return "B"; //B 整天停牌
                }
                if (status.charAt(3) == '0') { //第4位：‘0’表示此产品在当前时段不接受进行新订单申报，‘1’ 表示此产品在当前时段可接受进行新订单申报。无意义填空格。
                    return "B"; //B 整天停牌
                }
                return "";

            } else if (isMarketSZA(stockCode)) {
                // SecurityPhaseTag[PHRASE_TAG_LEN];   ///< 当前品种交易状态：产品所处的交易阶段代码：
                ///< 第 0 位：S=启动（开市前），O=开盘集合竞价， T=连续,B=休市
                ///<          C=收盘集合竞价,E=已闭市,H=临时停牌,A=盘后交易,V=波动性中断;
                ///< 第 1 位：0=正常状态,1=全天停牌"
                if (status.charAt(0) == 'H' || status.charAt(0) == 'V') {
                    //H=临时停牌,V=波动性中断;
                    return "D"; //D 暂停交易
                }
                if (status.charAt(1) == '1') {
                    //第 1 位：0=正常状态,1=全天停牌"
                    return "B"; //B 整天停牌
                }
                return "";
            }
        } catch (Exception e) {

        }
        return "";
    }


    public static boolean isClosedStatus(String stockCode, String dataSource, String status) {
        //以下仅国泰安数据适用. LJF 无状态标志
        try {
            if (status == null || status.isEmpty()) return false;
            if (dataSource == null) {
                if ("CLOSE".equals(status)) return true;
            }
            if (dataSource == null || "SecurityPhaseTag".equals(dataSource)) {
                if (isMarketSZA(stockCode)) {
                    // RealSZSEL2Quotation->SecurityPhaseTag[PHRASE_TAG_LEN];   ///< 当前品种交易状态
                    //< 当前品种交易状态：产品所处的交易阶段代码：
                    ///< 第 0 位：S=启动（开市前），O=开盘集合竞价， T=连续,B=休市
                    ///<          C=收盘集合竞价,E=已闭市,H=临时停牌,A=盘后交易,V=波动性中断;
                    ///< 第 1 位：0=正常状态,1=全天停牌"
                    if (status.charAt(0) == 'E') {
                        //‘E’表示闭市时段
                        return true;
                    }
                } else if (isMarketSHA(stockCode)) {
                    // RealSSEL2Quotation->SecurityPhaseTag
                    ///< 当前品种交易状态
                    ///< 该字段为8位字符串，左起每位表示特定的含义，无定义则填空格。
                    ///< 第1位：‘S’表示启动（开市前）时段，‘C’表示集合竞价时段，‘T’表示连续交易时段，‘E’表示闭市时段，‘P’表示产品停牌，‘M’表示可恢复交易的熔断时段（盘中集合竞价），‘N’表示不可恢复交易的熔断时段（暂停交易至闭市），‘U’表示收盘集合竞价时段。
                    ///< 第2位：‘0’表示此产品不可正常交易，‘1’表示此产品可正常交易，无意义填空格。
                    ///< 第3位：‘0’表示未上市，‘1’表示已上市。
                    ///< 第4位：‘0’表示此产品在当前时段不接受进行新订单申报，‘1’ 表示此产品在当前时段可接受进行新订单申报。无意义填空格。
                    if (status.charAt(0) == 'E' || status.charAt(0) == 'N') {
                        //‘E’表示闭市时段
                        //‘N’表示不可恢复交易的熔断时段（暂停交易至闭市）
                        return true;
                    }
                }
            }

            if ("TradeStatus".equals(dataSource)) {
                if ("CLOSE".equals(status)) return true;
            } else {
                if ("CLOSE".equals(status)) return true;
            }
            //logger.error("无法解析的状态:" + stockCode + "\t" + dataSource + "\t" + status);
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    /**
     * CAN BE SHARED
     * todo TBD
     *
     * @param mark
     * @return
     */
    public static boolean isSusbendSignal(String mark) {
        int suspendCode = 0;

        try {
            if (mark != null) {
                String s = mark.trim();
                if (s.length() == 0) return false;

                if (s.contains("(")) {
//                    s = s.replace(")", "");
                    s = s.replace("(", ",");
                    String[] code = s.split(",");
                    suspendCode = NU.parseInt(code[0], 0);
                } else if (NU.isNumber(s)) {
                    suspendCode = NU.parseInt(s, 0);
                } else {
                    suspendCode = s.charAt(0);
                }


                if (suspendCode == 0) return false;

                if (suspendCode == 72) return true; // X SUSP - 停牌
                if (suspendCode == 66) return true; // B 整天停牌
//                if (suspendCode == 65) return true; // A 交易节休市
                if (suspendCode == 67) return true; // C 全天收市
                if (suspendCode == 68) return true; // D 暂停交易
//                if (suspendCode == 69) return true; // G DEL - 不可恢复交易的熔断阶段（上交所的N）
//                if (suspendCode == 70) return true; // H HOLIDAY - 放假
//                if (suspendCode == 71) return true; // P BREAK - 休市　　

                if (suspendCode == 88) return true; // X 老版标志
                if (suspendCode == 89) return true; // Y 老版标志

                ///////////////////////////////////////////
//                if (suspendCode == 0 ) return false; // 0 首日上市　
//                if (suspendCode == 1 ) return false; // 1 增发新股　
//                if (suspendCode == 2 ) return false; // 2 上网定价发行　
//                if (suspendCode == 3 ) return false; // 3 上网竞价发行
//                if (suspendCode == 69 ) return false; // E START - 启动交易盘
//                if (suspendCode == 70 ) return false; // F PRETR - 盘前处理
//                if (suspendCode == 71 ) return false; // I OCALL - 开市集合竞价
//                if (suspendCode == 72 ) return false; // J ICALL - 盘中集合竞价
//                if (suspendCode == 73 ) return false; // K OPOBB - 开市订单簿平衡前期　　
//                if (suspendCode == 74 ) return false; // Y ADD - 新增产品　
//                if (suspendCode == 75 ) return false; // L IPOBB - 盘中订单簿平衡前期
//                if (suspendCode == 76 ) return false; // M OOBB - 开市订单簿平衡
//                if (suspendCode == 77 ) return false; // N IOBB - 盘中订单簿平衡
//                if (suspendCode == 78 ) return false; // O TRADE - 连续撮合　　
//                if (suspendCode == 79 ) return false; // Q VOLA - 波动性中断　　　
//                if (suspendCode == 82 ) return false; // R BETW - 交易间　
//                if (suspendCode == 83 ) return false; // S NOTRD - 非交易服务支持　　
//                if (suspendCode == 84 ) return false; // T FCALL - 固定价格集合竞价　　
//                if (suspendCode == 85 ) return false; // U POSTR - 盘后处理　　
//                if (suspendCode == 86 ) return false; // V ENDTR - 结束交易　
//                if (suspendCode == 87 ) return false; // W HALT - 暂停　
//                if (suspendCode == 100 ) return false; // d 集合竞价阶段结束到连续竞价阶段开始之前的时段（如有）
//                if (suspendCode == 113 ) return false; // q 可恢复交易的熔断时段(上交所的M)　
//                if (suspendCode == 65 ) return true; // A 交易节休市
//                if (suspendCode == 66 ) return true; // B 整天停牌
//                if (suspendCode == 67 ) return true; // C 全天收市
//                if (suspendCode == 68 ) return true; // D 暂停交易
//                if (suspendCode == 69 ) return true; // G DEL - 不可恢复交易的熔断阶段（上交所的N）
//                if (suspendCode == 70 ) return true; // H HOLIDAY - 放假
//                if (suspendCode == 71 ) return true; // P BREAK - 休市　　
//                if (suspendCode == 72 ) return true; // X SUSP - 停牌
//                if (suspendCode == 73 ) return true; // Z DEL - 可删除的产品
///////////////////////////////////////////
                return false;
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 国泰安 QTS only
     *
     * @return
     */
    public static String getStatusMeaning(String stockCode, String status) {
        //以下仅国泰安数据适用. LJF 无状态标志
        String msg = "";
        try {
            if (status == null || status.isEmpty()) return "";
            if (isMarketSHA(stockCode)) {
                // SecurityPhaseTag[PHRASE_TAG_LEN];   ///< 当前品种交易状态
                ///< 该字段为8位字符串，左起每位表示特定的含义，无定义则填空格。
                // TODO 从第 0 位读起 第1位->第0位
                ///< 第1位：‘S’表示启动（开市前）时段，‘C’表示集合竞价时段，‘T’表示连续交易时段，‘E’表示闭市时段，‘P’表示产品停牌，‘M’表示可恢复交易的熔断时段（盘中集合竞价），
                //      ‘N’表示不可恢复交易的熔断时段（暂停交易至闭市），‘U’表示收盘集合竞价时段。
                ///< 第2位：‘0’表示此产品不可正常交易，‘1’表示此产品可正常交易，无意义填空格。
                ///< 第3位：‘0’表示未上市，‘1’表示已上市。
                ///< 第4位：‘0’表示此产品在当前时段不接受进行新订单申报，‘1’ 表示此产品在当前时段可接受进行新订单申报。无意义填空格。
                switch (status.charAt(0)) {
                    case 'S':
                        msg = "启动（开市前）时段";
                    case 'C':
                        msg = "集合竞价时段";
                    case 'T':
                        msg = "连续交易时段";
                    case 'E':
                        msg = "闭市时段";
                    case 'P':
                        msg = "产品停牌";
                    case 'M':
                        msg = "可恢复交易的熔断时段(盘中集合竞价)";
                    case 'N':
                        msg = "不可恢复交易的熔断时段(暂停交易至闭市)";
                    case 'U':
                        msg = "收盘集合竞价时段";
                    default:
                        msg = "!";
                }

                if (status.charAt(1) == '1') {
                    msg += ", 正常交易";
                } else {
                    msg += ", 不可正常交易";
                }
                if (status.charAt(2) == '1') {
                    msg += ", 已上市";
                } else {
                    msg += ", 未上市";
                }

                if (status.charAt(3) == '0') { //第4位：‘0’表示此产品在当前时段不接受进行新订单申报，‘1’ 表示此产品在当前时段可接受进行新订单申报。无意义填空格。
                    msg += ", 不接受进行新订单申报";
                } else if (status.charAt(3) == '1') {
                    msg += ", 可接受进行新订单申报";
                } else {
                    msg += ", ";
                }
            } else if (isMarketSZA(stockCode)) {
                // SecurityPhaseTag[PHRASE_TAG_LEN];   ///< 当前品种交易状态：产品所处的交易阶段代码：
                ///< 第 0 位：S=启动（开市前），O=开盘集合竞价， T=连续,B=休市
                ///<          C=收盘集合竞价,E=已闭市,H=临时停牌,A=盘后交易,V=波动性中断;
                ///< 第 1 位：0=正常状态,1=全天停牌"
                switch (status.charAt(0)) {
                    case 'S':
                        msg = "启动(开市前)";
                    case 'O':
                        msg = "开盘集合竞价";
                    case 'T':
                        msg = "连续";
                    case 'B':
                        msg = "休市";
                    case 'C':
                        msg = "收盘集合竞价";
                    case 'E':
                        msg = "已闭市";
                    case 'H':
                        msg = "临时停牌";
                    case 'A':
                        msg = "盘后交易";
                    case 'V':
                        msg = "波动性中断";
                }
                if (status.charAt(1) == '1') {
                    msg += ", 整天停牌";
                } else {
                    msg += ", 正常状态";
                }
            }
        } catch (Exception e) {

        }
        return msg;
    }

    /**
     * 国泰安 QTS only
     *
     * @return
     */
    public static String getPrefixMeaning(String stockCode, String prefix) {
        String msg = ":";
        try {
            if (prefix == null || prefix.isEmpty()) return "";
            if (isMarketSHA(stockCode)) {
                //RealSSEL2_Static->SecurityStatus
                //char SecurityStatus[20];  产品状态标识,
                // TODO 位数从 0 为开始
                // 第0位对应：‘N’表示首日上市；
                // 第1位对应：‘D’表示除权；
                // 第2位对应：‘R'表示除息；
                // 第3位对应：‘D‘表示国内主板正常交易产品，‘S’表示风险警示产品，‘P’表示退市整理产品。
                if (prefix.charAt(0) == 'N') {
                    msg += "首日上市,";
                }
                if (prefix.charAt(1) == 'D') {
                    msg += "除权,";
                }
                if (prefix.charAt(2) == 'R') {
                    msg += "出息,";
                }
                switch (prefix.charAt(3)) {
                    case 'D':
                        msg += "正常交易";
                        break;
                    case 'S':
                        msg += "风险警示";
                        break;
                    case 'P':
                        msg += "退市整理";
                        break;
                }
            } else if (isMarketSZA(stockCode)) {
                //        char            SecurityStatusTag[20];              ///< 证券状态标识，该字段为20位字符串（后六位备用），
                ///< 每位表示特定的含义，“1”表示位数有业务意义，“0”表示该位数无业务意义。
                // TODO 位数从 1 为开始
                //  第1位对应：“1”表示停牌；
                /// 第2位对应：“1”表示除权；
                //  第3位对应：“1”表示除息；
                //  第4位对应：“1”表示ST;
                //  第5位对应：“1”表示*ST;
                /// 第6位对应：“1”表示上市首日；
                //  第7位对应：“1”表示公司再融资；
                //  第8位对应：“1”表示恢复上市首日；
                /// 第9位对应：“1”表示网络投票；
                //  第10位对应：“1”表示退市整理期；
                //  第12位对应：“1”表示增发股份上市；
                /// 第13位对应：“1”表示合约调整；
                //  第14位对应：“1”表示暂停上市后协议转让。
                if (prefix.charAt(0) == '1') {
                    msg += ",停牌";
                }
                if (prefix.charAt(1) == '1') {
                    msg += ",除权";
                }
                if (prefix.charAt(2) == '1') {
                    msg += ",除息";
                }
                if (prefix.charAt(3) == '1') {
                    msg += ",ST";
                }
                if (prefix.charAt(4) == '1') {
                    msg += ",*ST";
                }
                if (prefix.charAt(5) == '1') {
                    msg += ",上市首日";
                }
                if (prefix.charAt(6) == '1') {
                    msg += ",公司再融资";
                }
                if (prefix.charAt(7) == '1') {
                    msg += ",恢复上市首日";
                }
                if (prefix.charAt(8) == '1') {
                    msg += ",网络投票";
                }
                if (prefix.charAt(9) == '1') {
                    msg += ",退市整理期";
                }
                if (prefix.charAt(10) == '1') {
                    //Nothing
                }
                if (prefix.charAt(11) == '1') {
                    msg += ",增发股份上市";
                }
                if (prefix.charAt(12) == '1') {
                    msg += ",合约调整";
                }
                if (prefix.charAt(13) == '1') {
                    msg += ",暂停上市后协议转让";
                }

            }

        } catch (Exception e) {

        }
        return msg;
    }


    public static boolean isDrName(String stockCode, Database db){
        ResultSet rs = null;
        String sql = "select stockName from StockName_a where stockCode = '"+stockCode+"'";

        try {
            rs = db.dynamicSQL(sql);
            if (rs.next()) {
                String stockName = rs.getString(1);
                if(stockName == null || stockName.length() < 2) return false;


                if (stockName.matches("^XD.*|^XR.*|^DR.*")) {
                    int a = 0;
                }
                return stockName.matches("^XD.*|^XR.*|^DR.*");
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            Database.closeResultSet(rs);
        }
        return false;
    }

    public static boolean isDR(String dataSource, String stockCode, String prefix) {
        if (stockCode == null || prefix == null || prefix.isEmpty()) return false;
        if ("QTS".equals(dataSource)) {
            /**
             * SH
             该字段为20位字符串，每位表示允许对应的业务，无定义则填空格。
             第0位对应：‘N’表示首日上市。
             第1位对应：‘D’表示除权。
             第2位对应：‘R’表示除息。
             第3位对应：’D’表示国内主板正常交易产品，’S’表示股票风险警示产品，’P’表示退市整理产品，’T’表示退市转让产品，’U’表示优先股产品。
             第4位对应：‘Y’表示该产品为存托凭证。
             第5位对应：’L’表示债券投资者适当性要求类，’M’表示债券机构投资者适当性要求类。
             第6位对应：‘F’表示15:00闭市的产品，‘S’表示15:30闭市的产品，为空表示非竞价撮合平台挂牌产品，无意义。
             第7位对应：‘U’表示上市时尚未盈利的发行人的股票或存托凭证，发行人首次实现盈利后，取消该特别标识。该字段仅针对科创板产品有效。
             第8位对应：‘W’表示具有表决权差异安排的发行人的股票或存托凭证。该字段仅针对科创板产品有效。
             **/
            if (isMarketSHA(stockCode)) {
                if (prefix.length() > 2) {
                    return prefix.charAt(1) == 'D' || prefix.charAt(2) == 'R';
                }
            }

            /**
             SZ
             该字段为20位字符串（后六位备用），每位表示特定的含义：“1”表示位数有业务意义；“0”表示该位数无业务意义。
             // TODO 位数从 1 为开始
             第1位对应：“1”表示停牌；
             第2位对应：“1”表示除权；
             第3位对应：“1”表示除息；
             第4位对应：“1”表示ST；
             第5位对应：“1”表示*ST;
             第6位对应：“1”表示上市首日；
             第7位对应：“1”表示公司再融资；
             第8位对应：“1”表示恢复上市首日；
             第9位对应：“1”表示网络投票；
             第10位对应：“1”表示退市整理期；
             第12位对应：“1”表示增发股份上市；
             第13位对应：“1”表示合约调整；
             第14位对应：“1”表示暂停上市后协议转让。
             第15位对应：“1”表示实施双转单调整
             第16位对应：“1”表示特定债券转让
             第17位对应：“1”表示上市初期
             */
            if (isMarketSZA(stockCode)) {
                if (prefix.length() > 2) {
                    return prefix.charAt(1) == '1' || prefix.charAt(2) == '1';
                }
            }

        }
        return false;
    }

    public static boolean isStopTrade(String dataSource, String stockCode, String prefix) {
        if (stockCode == null || prefix == null || prefix.isEmpty()) return false;
        if ("QTS".equals(dataSource)) {
            /**
             * SH
             该字段为20位字符串，每位表示允许对应的业务，无定义则填空格。
             第0位对应：‘N’表示首日上市。
             第1位对应：‘D’表示除权。
             第2位对应：‘R’表示除息。
             第3位对应：’D’表示国内主板正常交易产品，’S’表示股票风险警示产品，’P’表示退市整理产品，’T’表示退市转让产品，’U’表示优先股产品。
             第4位对应：‘Y’表示该产品为存托凭证。
             第5位对应：’L’表示债券投资者适当性要求类，’M’表示债券机构投资者适当性要求类。
             第6位对应：‘F’表示15:00闭市的产品，‘S’表示15:30闭市的产品，为空表示非竞价撮合平台挂牌产品，无意义。
             第7位对应：‘U’表示上市时尚未盈利的发行人的股票或存托凭证，发行人首次实现盈利后，取消该特别标识。该字段仅针对科创板产品有效。
             第8位对应：‘W’表示具有表决权差异安排的发行人的股票或存托凭证。该字段仅针对科创板产品有效。
             **/
            if (isMarketSHA(stockCode)) {
                return false; //未知
            }

            /**
             SZ
             该字段为20位字符串（后六位备用），每位表示特定的含义：“1”表示位数有业务意义；“0”表示该位数无业务意义。
             // TODO 位数从 1 为开始
             第1位对应：“1”表示停牌；
             */
            if (isMarketSZA(stockCode)) {
                return prefix.charAt(0) == '1';
            }

        }
        return false;
    }


    public static String stockCodeMap(String stockCode) {
        if (stockCode.startsWith("SH00")) {
            return stockCode.replace("SH00", "SH1A");
        } else if (stockCode.equals("SH999999")) {
            return "SH1A0001";
        } else if (stockCode.equals("SH999998")) {
            return "SH1A0002";
        } else if (stockCode.equals("SH999997")) {
            return "SH1A0003";
        } else if (stockCode.equals("SH999996")) {
            return "SH1A0004";
        } else if (stockCode.equals("SH999995")) {
            return "SH1A0005";
        } else if (stockCode.equals("SH999994")) {
            return "SH1A0006";
        } else if (stockCode.equals("SH999993")) {
            return "SH1A0007";
        } else if (stockCode.equals("SH999992")) {
            return "SH1A0008";
        } else if (stockCode.equals("SH999991")) {
            return "SH1A0009";
        }
        return stockCode;
    }

    public static void main(String[] args) {
        System.out.println(getPrefixMeaning("SZ002336", "00010000000000000000"));

    }
}
