package szhzz.Netty.Cluster.ExchangeDataType;


/**
 * Created by Administrator on 2016/4/12.
 *
 * 委托，成交，持仓，资金等信息交换
 */
public class AccountQueryWrap{

    /**
     *
     * @param acc
     * @param data
     * @return
     */
//    public static NettyExchangeData wrap(AccountClient acc, ExchangeData data) {
//        NettyExchangeData nettyData = new NettyExchangeData(data);
//        nettyData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
//        nettyData.setMessage("AccountQuery");
//
//        nettyData.setNettyType(ClusterProtocal.FUNCTION.AccountQuery);
//
//        //用于接收端定位相同的账号实例
//        nettyData.setExtData(acc.getClass().getSimpleName(), 2);   //账户的实例类型
//        nettyData.setExtData(acc.getAccountName(), 3);              //账户名称
//        nettyData.setExtData(acc.getOperationAccount(), 4);        //账户的操作账号
//
//        return nettyData;
//    }

    /**
     * 数据类型判别
     *
     * @param data
     * @return
     */
    public static boolean isTypeOf(NettyExchangeData data) {
        return ClusterProtocal.FUNCTION.AccountQuery == data.getNettyType();
    }

    /**
     * 获取账户的实例类型
     *
     * @param data
     * @return
     */
    public static String getClassName(NettyExchangeData data) {
        Object o = data.getExtData(2);
        if(o == null) return "";
        return o.toString();
    }

    /**
     * 获取账户名称
     * @param data
     * @return
     */
    public static String getAccountName(NettyExchangeData data) {
        Object o = data.getExtData(3);
        if(o == null) return "";
        return o.toString();
    }

    /**
     * 获取账户的操作账号
     * @param data
     * @return
     */
    public static String getOperationAccount(NettyExchangeData data) {
        if(isTypeOf(data)) {
            Object o = data.getExtData(4);
            if (o != null){
                return o.toString();
            }
        }
        return null;
    }
}
