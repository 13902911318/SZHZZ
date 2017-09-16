package szhzz.Netty.Cluster.ExchangeDataType;

import szhzz.Utils.NU;
import szhzz.Utils.DawLogger;

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
        TradeConfigFile,
        SetupUDP;
    }

    public enum EVENT {
        Normal,
        Broadcast,
        HandDown;
    }

    public static boolean isBroadcast(Object event) {
        if (NU.parseInt(event, 0) > 1) return false;
        return EVENT.values()[NU.parseInt(event, 0)] == EVENT.Broadcast;
    }

}
