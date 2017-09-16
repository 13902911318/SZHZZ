package szhzz.Netty.Cluster.ExchangeDataType;

/**
 * Created with IntelliJ IDEA.
 * User: HuangFang
 * Date: 13-11-23
 * Time: 上午11:33
 * To change this template use File | Settings | File Templates.
 */
public class StockCodeConvert {
    public static String stockCodeToWind(String stockCode) {
        if (stockCode.startsWith("S")) {
            return stockCode.substring(2) + "." + stockCode.substring(0, 2);
        }
        return stockCode;
    }


    public static String toStdStockCode(String stockCode) {
//        String s = stockCodeMap(toStdStockCode_(stockCode));
//        if ("SH990814".equals(s)) {
//            int a = 0;
//        }

        return stockCodeMap(toStdStockCode_(stockCode));
    }

    private static String toStdStockCode_(String stockCode) {
        if (stockCode.indexOf('.') == 6) {
            return stockCode.substring(7) + stockCode.substring(0, 6);
        } else if (stockCode.startsWith("SH") || stockCode.startsWith("SZ")) {// 标准代码格式
            return stockCode;
        } else if (stockCode.endsWith("SH") || stockCode.endsWith("SZ")) {
            return stockCode.substring(6) + stockCode.substring(0, 5);
        } else if (stockCode.startsWith("60") || stockCode.startsWith("204")) {
            return "SH" + stockCode;       //上海A股
        } else if (stockCode.startsWith("002") || stockCode.startsWith("001") || stockCode.startsWith("000") || stockCode.startsWith("300") || stockCode.startsWith("399")) {
            return "SZ" + stockCode;     //深圳A
        } else if (stockCode.startsWith("900") || stockCode.startsWith("93") || stockCode.startsWith("999")) {
            return "SH" + stockCode;  //3—上海B股
        } else if (stockCode.startsWith("200")) {
            return "SZ" + stockCode;  //4—深圳B股
        }

        return stockCode;
    }


    public static String toSecurityCode(String stockCode) {
        return stockCode.replaceAll("SH|SZ|\\.", "");
    }


    public static String stockCodeMap(String stockCode) {
        if (stockCode.startsWith("SH00")) {
            return stockCode.replace("SH00", "SH1A");
        } else if (!stockCode.startsWith("SH999")) {
            return stockCode;
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
        System.out.println(toSecurityCode("SH600030"));
        String s = toSecurityCode("SH600030");
        System.out.println(toSecurityCode("SZ000001"));
        System.out.println(toSecurityCode("600030.SH"));
    }
}
