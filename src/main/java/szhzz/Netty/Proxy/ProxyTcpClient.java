package szhzz.Netty.Proxy;

import szhzz.App.AppManager;
import szhzz.Netty.Cluster.NettyClient;
import szhzz.Utils.DawLogger;

/**
 * Created by Administrator on 2015/11/6.
 */
public class ProxyTcpClient extends NettyClient {
    private static DawLogger logger = DawLogger.getLogger(ProxyTcpClient.class);

    AppManager App = AppManager.getApp();
    private MarketProxy marketProxy = null;

    public void start(MarketProxy marketProxy) {
        this.marketProxy = marketProxy;
        super.start();
    }

    public ProxyTcpClient(String[] host, int port) {
        super(host, port);
    }


    public void connected() {
        super.connected();
        if (marketProxy != null) {
            App.logEvent("Connected to(" + (isNio ? "Nio" : "Oio") + ") market proxy " + getHost());
        }
    }


    public void disConnected() {
        if (marketProxy != null) {
            App.logEvent("Disconnect from market proxy " + getHost());
            marketProxy.clientDisconnect(this);
        }
        super.disConnected();
    }
}
