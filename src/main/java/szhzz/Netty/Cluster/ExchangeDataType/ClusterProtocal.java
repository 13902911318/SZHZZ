package szhzz.Netty.Cluster.ExchangeDataType;

import szhzz.Utils.DawLogger;
import szhzz.Utils.NU;

import static szhzz.Netty.Cluster.ExchangeDataType.ClusterProtocal.EVENT.Broadcast;

/**
 * Created by HuangFang on 2015/4/5.
 * 9:20
 */
public class ClusterProtocal {
    private static DawLogger logger = DawLogger.getLogger(ClusterProtocal.class);
    private static NettyExchangeData closeOthersMsg = null;

    public enum FUNCTION {
        NoAction,
        QueryServerLevel,
        AnswerServerLevel,
        CloseOthers,
        Disconnect,
        SendMessage,
        UserData,
        TradePlan,
        TextFile,
        OrderState,
        SqlUpdate,
        StopBuy,
        StopSale,
        Shutdown,
        MarketData,
        MarketSynCallBack,
        AccountQuery,
        UpdateConfigValue,
        StopApp,
        RegisterStockCode,
        OrderHandDown,
        CancelHandDown,
        LogInformation,
        AmAuctionConfigFile,
        SetupUDP,
        LoginThroughGate,  //
        AnswerLoginFromGate, //
        ClientQuery,
        AnswerClient,
        PushSynCallBack,
        ////////////////////////////////////////////////////
        ProxyOrder,
        ProxySubmited,
        ProxyOrderCallBack,
        ProxyCancel,
        ProxyCancelDeleved,
        ProxyCancelCallBack,
        TradeConfigFile; //
    }

    public enum EVENT {
        Cluster,
        Broadcast,
        HandDown,
        TradeProxy;
    }

    public static boolean isBroadcast(Object event) {
        if (NU.parseInt(event, -1) < 0) return false;
        return EVENT.values()[NU.parseInt(event, 0)] == Broadcast;
    }

    public static boolean isCluster(Object event) {
        if (NU.parseInt(event, -1) < 0) return false;
        return EVENT.values()[NU.parseInt(event, -1)] == EVENT.Cluster;
    }

    public static boolean isTradeProxy(Object event) {
        if (NU.parseInt(event, -1) < 0) return false;
        return EVENT.values()[NU.parseInt(event, -1)] == EVENT.TradeProxy;
    }

    public static EVENT getEvent(Object event) {
        int eventOrdinal = NU.parseInt(event, -1);
        if (eventOrdinal < 0) return Broadcast;
        return EVENT.values()[eventOrdinal];

    }
}
