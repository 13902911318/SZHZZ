package szhzz.Netty.Cluster.ExchangeDataType;

import szhzz.Netty.Cluster.Cluster;

/**
 * Created by Administrator on 2015/7/7.
 */
public class RegisterStockCodeWrap {


    public static NettyExchangeData getRegister(Iterable<String> stockCodes){
        StringBuilder sb = new StringBuilder();
        for(String s: stockCodes){
            if(sb.length() > 0)sb.append(";");
            sb.append(s);
        }
        return getRegister(sb.toString());
    }
    public static NettyExchangeData getRegister(String stockCodes) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
        eData.setMessage(stockCodes);
        eData.setNettyType(ClusterProtocal.FUNCTION.RegisterStockCode);

        return eData;
    }

    public static String[] getStockCode(NettyExchangeData  eData){
        Object o = eData.getMessage();
        if(o != null){
            return o.toString().split(";");
        }
        return null;
    }
}
