package szhzz.PinYin;

import szhzz.STOCK.MarketInformation;

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
            if (MarketInformation.isMarketSHA(code)) {
                return "SH" + code;
            } else if (MarketInformation.isMarketSZA(code)) {
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
        if (MarketInformation.isMarketSHA(code)) {
            return "SH";
        } else if (MarketInformation.isMarketSZA(code)) {
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
        if (code.startsWith("510")) return "QQ";    // ×××期权；
        if (code.startsWith("60")) return "SHA";    // ×××A股；
        if (code.startsWith("68")) return "SHA";    // ×××创业板；
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

    public static boolean isFundCat(String cat) {
        return (cat.equals("SHJ") || cat.equals("SZJ") );
    }

    public static boolean isFutureCat(String cat) {
        return (cat.equals("QQ"));
    }

    public static boolean isIndexCat(String cat) {
        return (cat.equals("IND")) || (cat.equals("BAND"));
    }
}
