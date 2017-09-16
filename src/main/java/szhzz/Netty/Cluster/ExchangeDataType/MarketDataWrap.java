package szhzz.Netty.Cluster.ExchangeDataType;


import szhzz.Utils.FT;

/**
 * Created by Administrator on 2015/7/6.
 * <p>
 * 行情数据
 */
public class MarketDataWrap {
    private static long sNo = 0L;
    private static long lostCount = 0L;
    private static long lastSerialNo = -1L;

    public static NettyExchangeData wrapMarketData(long FunID, String data) { //String stockCode,
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
        eData.setFunID(FunID); //ClusterProtocal.FUNCTION.UserData.ordinal()
        eData.setNettyType(ClusterProtocal.FUNCTION.MarketData);
        eData.setSerialNo(++sNo);

        eData.appendRow();
        eData.appendRow();
        eData.addData(data);
        return eData;
    }

    public static String getStockCode(NettyExchangeData data) {
        String source = data.getExtData(1);
        if ("LJF".equals(source) || "WJF".equals(source)) {
            return data.getExtData(2) + data.getExtData(3);
        } else {
            String[] array = getMarketDate(data).split(",");
            if (array.length < 4) return "";
            return StockCodeConvert.toStdStockCode(array[4]);
        }

    }

    public static String getWindStockCode(NettyExchangeData data) {
        String source = data.getExtData(0);
        if ("LJF".equals(source) || "WJF".equals(source)) {
            return data.getExtData(2) + "." + data.getExtData(1);
        } else {
//            return data.getExtData(2) + "." + data.getExtData(1);
            String[] array = getMarketDate(data).split(",");
            if (array.length < 4) return "";
            return StockCodeConvert.stockCodeToWind(array[4]);
        }
    }

    public static String getMarketDate(NettyExchangeData data) {
        if (data == null) return null;
        Object rawData = data.getDataValue(0, 0);
        if (rawData != null) {
            return rawData.toString();
        } else {
            return "";
        }
    }

    /**
     *
     * @param data
     */
    public static void checkSerialNo(NettyExchangeData data) {
        long no = data.getSerialNo();
        if(no < 0) return;

        data.setSerialNo(-1);
        if (lastSerialNo > 0) { //no 在客户端不一定从1开始
            if ((lastSerialNo + 1) != no) {
                lostCount++;
            }
        }
        lastSerialNo = no;

    }

    public static long getFunID(NettyExchangeData data) {
        return data.getFunID();
    }

    public static long getLostCount() {
        return lostCount;
    }
    public static String getLostPercent() {
        return FT.format00(100*lostCount/lastSerialNo) + "%";
    }
}

