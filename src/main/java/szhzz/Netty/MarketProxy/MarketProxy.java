package szhzz.Netty.MarketProxy;

import szhzz.App.AppManager;
import szhzz.App.BeQuit;
import szhzz.DataBuffer.DataConsumer;
import szhzz.DataBuffer.ObjBufferedIO;
import szhzz.Netty.Cluster.ClientInspector;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Netty.Cluster.NettyRequystor;
import szhzz.Netty.Cluster.UDP.UdpServer;
import szhzz.Utils.DawLogger;

import java.util.HashMap;

/**
 * Created by HuangFang on 2015/3/15.
 * 11:23
 * <p>
 * 管理本地服务器
 * 和数个 client
 */
public class MarketProxy implements DataConsumer {
    public int tcpPort = 7523;

    private final static HashMap<Long, NettyRequystor> requests = new HashMap<>();
    private static DawLogger logger = DawLogger.getLogger(MarketProxy.class);
    private static MarketProxy onlyOne = new MarketProxy();
    private static AppManager App = AppManager.getApp();
    private ProxyServer server = null;
    private String serverName = "";
    private ProxyTcpClient proxyClient = null;
    private ObjBufferedIO dataBuffer = null;
    private UdpServer udpServer = null;
    private ClientInspector clientInspector = null;

    private MarketProxy() {
    }

    public static MarketProxy getInstance() {
        return onlyOne;
    }

    public static void main(String[] args) {
        App.setLog4J();
    }

    public String getServerName() {
        return serverName;
    }

//    public void setServerHandler(ProxyHandler serverHandler) {
//        this.serverHandler = serverHandler;
//    }

    /**
     *
     */
    public void closeServer() {
        if (isServerAlive()) {
            ProxyHandler.sayBye();
        }
        if (server != null) {
            server.closeServer();
        }
        server = null;
    }

    public void broadcast(NettyExchangeData data) {
        ProxyHandler.broadcast(data);
    }

    public void setUdp(boolean on) {
        ProxyHandler.setUdp(on);
    }

    public boolean startServer() {
        if (isConnectedToServer()) {
            App.logEvent("Can not start proxy Server, a remote Proxy server is connected.");
        }

        if (server != null) return true;

        App.logit("Start Market data proxy TCP server  on port " + tcpPort + "...");

        server = new ProxyServer(tcpPort);
        try {
            server.startup();
        } catch (Exception e) {
            logger.error(e);
        } finally {

        }
        return server.isOnServer();
    }

    public boolean isServerAlive() {
        return isServerStarted() && ProxyHandler.isAlive();
    }

    public boolean isServerStarted() {
        return (server != null && server.isOnServer());
    }


    //    public void connectToServer() {
//        if (isServerStarted()) {
//            szhzz.App.logEvent("Local Proxy Server started, can not connect to other Proxy server");
//            return;
//        }
//
//        ProxyTcpClient newClient = new ProxyTcpClient("localhost", tcpPort);
//        newClient.start(this);
//    }
    public void connectToServer(String host, int port, int connectionTimeout) {
        tcpPort = port;
        if (isServerStarted()) {
            App.logEvent("Local Proxy Server started, can not connect to other Proxy server");
            return;
        }

        if (proxyClient != null) {
            proxyClient.disconnectFromServer();
            proxyClient.setHost(host.split(";"), tcpPort);
        } else {
            proxyClient = new ProxyTcpClient(host.split(";"), tcpPort);
        }
        proxyClient.setInspector(clientInspector);
        proxyClient.setConnectionTimeout(connectionTimeout);
        proxyClient.start(this);
    }

    public void connectToServer(String host, int port) {
        connectToServer(host, port, 10);
    }


    public void connectToServer(String host) {
        connectToServer(host, tcpPort);
    }

//    public void clientConnect(ProxyTcpClient client) {
//        if (proxyClient != null && client != proxyClient) {
//            proxyClient.disconnectFromServer();
//        }
//        proxyClient = client;
//    }

    public void flagSignal(String semaphore) {
        if (proxyClient != null) {
            proxyClient.flagSignal(semaphore);
        }
    }

    public String getClientHost() {
        if (proxyClient != null) {
            return proxyClient.getHost();
        }
        return null;
    }

    public void clientDisconnect(ProxyTcpClient client) {
//        proxyClient = null;
    }

    public boolean isConnectedToServer() {
        return proxyClient != null && proxyClient.isConnected();
    }

    public void disconnectFromServer() {
        if (proxyClient != null) {
            proxyClient.disconnectFromServer();
        }
    }

    BeQuit a = new BeQuit() {

        @Override
        public boolean Quit() {
            ProxyHandler.sayBye();
            return true;
        }
    };

    /**
     * 需要缓存和线程隔离的情况下使用
     * 如果调用端已经有缓存则可直接调用 broadcast()
     *
     * @param data
     */
    public void pushBroadcast(NettyExchangeData data) {
        if (!isServerAlive()) return;

        if (dataBuffer == null) {
            dataBuffer = new ObjBufferedIO();
            try {
                dataBuffer.setReader(this, 10);
            } catch (InterruptedException e) {
                logger.error(e);
                dataBuffer = null;
            }
        }
        if (dataBuffer != null) {
            dataBuffer.push(data);
        } else {
            in(data);
        }
    }

    @Override
    public long in(Object obj) {
        try {
            broadcast((NettyExchangeData) obj);
            return 1;
        } catch (Exception e) {
            logger.error(e);
        }
        return -1;
    }

    @Override
    public long in(long dataID, Object obj) {
        return 0;
    }

    public void setupDdp(int port) {
        if (udpServer == null) {
            udpServer = new UdpServer(port);
            try {
                AppManager.executeInBack(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            udpServer.startServer();
                        } catch (InterruptedException e) {
                            logger.error(e);
                        }
                    }
                });
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
    }

    public void setClientInspector(ClientInspector clientInspector) {
        this.clientInspector = clientInspector;
        if (proxyClient != null) {
            proxyClient.setInspector(clientInspector);
        }
    }
}
