package szhzz.PinYin;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2009-6-23
 * Time: 20:19:56
 * To change this template use File | Settings | File Templates.
 */
public class StockRegulate {

    public static String getPinYinHeadChar(String stockName) {
        //return CnToSpell.getPinYinHeadChar(regulatePinYinCode(stockName)).toUpperCase();
        return getPingYin.getPinYinHeadChar(regulatePinYinCode(stockName)).toUpperCase();

    }

    public static String regulateStockCode(String code) {
        if (!isStockCode(code)) return null;

        if (code.length() == 6) {
            if (code.startsWith("60") || code.startsWith("1A")) {
                return "SH" + code;
            } else if (code.startsWith("00") || code.startsWith("30") || code.startsWith("399")) {
                return "SZ" + code;
            } else {
                return null;
            }
        } else if (code.length() == 8) {
            return code;
        }
        return null;
    }

    public static String stockMarket(String code) {
        if (code.startsWith("60") || code.startsWith("1A") || code.startsWith("2040")|| code.startsWith("510")) {
            return "SH";
        } else if (code.startsWith("00") || code.startsWith("30") || code.startsWith("399") || code.startsWith("1318")) {
            return "SZ";
        }
        return "";
    }

    public static boolean isStockCode(String code) {
        if (code.length() == 6) {
            return !"".equals(stockMarket(code));
        } else if (code.length() == 8) {
            if (code.toUpperCase().startsWith("SH") || code.toUpperCase().startsWith("ZH")) {
                return isStockCode(code.substring(2));
            }
        }
        return false;
    }

    public static String regulatePinYinCode(String code) {
        String c = code.replace("Ｂ", "B");
        c = c.replace("Ａ", "A");
        return c.replaceAll("[*| ]", "");
    }

    /**
     * N：当股票名称前出现了N字，表示这只股是当日新上市的股票，字母N是英语New(新)的缩写。
     * 看到带有N字头的股票时，投资者除了知道它是新股，
     * 还应认识到这只股票的股价当日在市场上是不受涨跌幅限制的，
     * 涨幅可以高于10％，跌幅也可深于10％。这样就较容易控制风险和把握投资机会。
     * <p/>
     * XD：当股票名称前出现XD字样时，表示当日是这只股票的除息日，XD是英语Exclud(除去)Dividend(利息)的简写。
     * 在除息日的当天，股价的基准价比前一个交易日的收盘价要低，因为从中扣除了利息这一部分的差价。
     * <p/>
     * XR：当股票名称前出现XR的字样时，表明当日是这只股票的除权日。XR是英语Exclud(除去)Right(权利)的简写。
     * 在除权日当天，股价也比前一交易日的收盘价要低，原因由于股数的扩大，股价被摊低了。
     * <p/>
     * DR：当股票名称前出现DR字样时，表示当天是这只股票的除息、除权日。D是Dividend(利息)的缩写，
     * R是Right(权利)的缩写。有些上市公司分配时不仅派息而且送转红股或配股，所以出现同时除息又除权的现象。
     * <p/>
     * S：未按期完成股改的股票。每日最大涨跌幅度5%。
     * ST：连续两年年报亏损、或因其他重大违规被特别处理的股票。每日最大涨跌幅度5%。
     * ST：被特别处理后年报继续亏损，有退市风险的股票。每日最大涨跌幅度5%。
     *
     * @param stockName
     * @return
     */
    public static boolean isXDR(String stockName) {
        return (stockName.startsWith("XD") || stockName.startsWith("XR") || stockName.startsWith("DR"));
    }


    /**
     * @param stockCode
     * @return
     */
    public static String stockCatigory(String stockCode) {
        if (stockCode.startsWith("88")) return "BAND";
        if (stockCode.startsWith("SH")) return stockCatigory_SH(stockCode);
        if (stockCode.startsWith("SZ")) return stockCatigory_SZ(stockCode);
        return "";
    }

    public static String stockCatigory_SZ(String stockCode) {
        String code = stockCode.substring(2, 5);
        // 具体见下表所示：
        // 0×××A股；
        // 1×××企业债券、 国债回购、国债现货；
        // 2×××B股及B股权证；
        // 3×××转配股权证；
        // 4×××基金；
        // 5×××可转换债券；
        // 6×××国债期货；
        // 7×××期权；
        // 8×××配股权证；
        // 9×××新股配售
        if (code.startsWith("0")) return "SZA";    // 深圳A股；
        if (code.startsWith("1")) return "SZZQ";    // 企业债券、 国债回购、国债现货；
        if (code.startsWith("2")) return "SZB";  // B股及B股权证；
        if (code.startsWith("399")) return "IND";    // 指数；
        if (code.startsWith("30")) return "SZC";    // 创业版；
        if (code.startsWith("3")) return "ZPZ";    // 转配股权证；
        if (code.startsWith("4")) return "SZJ";    // 基金；
        if (code.startsWith("5")) return "KZZQ";    // 可转换债券；
        if (code.startsWith("6")) return "GZQH";    // 国债期货；
        if (code.startsWith("7")) return "QQ";    // 期权；
        if (code.startsWith("8")) return "PGQZ";    // 配股权证；
        if (code.startsWith("9")) return "XGPS";    // 新股配售
//        StockApp.logit("Alarm stockCode " + stockCode + " Not belong to any cat ");
        return "";
    }

    public static String stockCatigory_SH(String stockCode) {
        String code = stockCode.substring(2, 5);
        // 在上海证券交易所上市的证券，根据上交所\\\\"证券编码实施方案\\\\"，采用6位数编制方法，前3位数为区别证券品种，具体见下表所列：
        // 001×××国债现货；
        // 201×××国债回购；
        // 110×××
        // 120×××企业债券；
        // 129×××
        // 100×××可转换债券；
        // 310×××国债期货；
        // 500×××
        // 550×××基金；
        // 600×××A股；
        // 700×××配股；
        // 710×××转配股；
        // 701×××转配股再配股；
        // 711×××转配股再转配股；
        // 720×××红利；
        // 730×××新股申购；
        // 735×××新基金申购；
        // 900×××B股；
        // 737×××新股配售。

        ////////////////////////
        if (code.startsWith("001")) return "GZZS";    // ×××国债,指数；
        if (code.startsWith("201")) return "GZHG";    // ×××国债回购；
        if (code.startsWith("110")) return "QYZQ";    // ×××企业债券；
        if (code.startsWith("120")) return "QYZQ";    // ×××企业债券；
        if (code.startsWith("129")) return "KZZQ";    // ×××可转换债券；
        if (code.startsWith("100")) return "KZZQ";    // ×××可转换债券；
        if (code.startsWith("310")) return "GZQH";    // ×××国债期货；
        if (code.startsWith("500")) return "SHJ";    // ×××基金；
        if (code.startsWith("550")) return "SHJ";    // ×××基金；
        if (code.startsWith("60")) return "SHA";    // ×××A股；
//        if (code.startsWith("601")) return "SHA";    // ×××A股；
        if (code.startsWith("700")) return "PG";    // ×××配股；
        if (code.startsWith("710")) return "ZPG";    // ×××转配股；
        if (code.startsWith("711")) return "ZPG";    // ×××转配股；
        if (code.startsWith("701")) return "ZPG";    // ×××转配股；
        if (code.startsWith("720")) return "HL";    // ×××红利；
        if (code.startsWith("730")) return "XGSG";    // ×××新股申购；
        if (code.startsWith("735")) return "XJSG";    // ×××新基金申购；
        if (code.startsWith("900")) return "SHB";    // ×××B股；
        if (code.startsWith("737")) return "XGPS";    // 新股配售；
//        StockApp.logit("Alarm stockCode " + stockCode + " Not belong to any cat ");
        return "";
    }

    public static boolean isStockCat(String cat) {
        return (cat.equals("SZA") || cat.equals("SZB") || cat.equals("SHA") || cat.equals("SHB") || cat.equals("SZC"));
    }


    public static boolean isIndexCat(String cat) {
        return (cat.equals("IND")) || (cat.equals("BAND"));
    }
}
