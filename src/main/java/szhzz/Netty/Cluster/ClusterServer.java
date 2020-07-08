package szhzz.Netty.Cluster;

import szhzz.App.AppManager;
import szhzz.App.BeQuit;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Netty.Cluster.ExchangeDataType.StationPropertyWrap;
import szhzz.Netty.Cluster.Net.ServerHandler;
import szhzz.Utils.DawLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


/**
 * Created by HuangFang on 2015/3/15.
 * 11:23
 * <p>
 * <p>
 * 管理本地服务器
 * 和数个 client
 */
public class ClusterServer {
    private static DawLogger logger = DawLogger.getLogger(ClusterServer.class);
    private static ClusterServer onlyOne = new ClusterServer();
    private static AppManager App = AppManager.getApp();
    private NettyServer server = null;

    private String serverName = null;
    private int localLevel = 0;
    private String inetHost = null;
    private ClusterServer() {

    }

    private int port = 0;

    public static ClusterServer getInstance() {
        return onlyOne;
    }

    public static void main(String[] args) {
        App.setLog4J();
    }

    public int getLocalLevel() {
        return localLevel;
    }

    public void setLocalLevel(int localLevel) {
        this.localLevel = localLevel;
    }

    public String getServerName() {
        return serverName;
    }

    public void broadcast(NettyExchangeData msg) {
        ServerHandler.broadcast(msg);
    }

    public void saBye() {
        ServerHandler.sayBye();
    }


    public void Oio() throws IOException, ClassNotFoundException {
        ServerSocket socketConnection = null;
        socketConnection = new ServerSocket(11111);

        System.out.println("Server Waiting");

        Socket pipe = socketConnection.accept();

        ObjectInputStream serverInputStream = new
                ObjectInputStream(pipe.getInputStream());

        ObjectOutputStream serverOutputStream = new
                ObjectOutputStream(pipe.getOutputStream());

        NettyExchangeData data = (NettyExchangeData) serverInputStream.readObject();
    }

    /**
     * 服务器 进站
     *
     * @param data
     * @return 恢复远程的查询信息
     */
    public ArrayList<NettyExchangeData> answer(NettyExchangeData data) {
        if (Cluster.getInstance().isOffLine()) return null;
//        NettyExchangeData exDate = null;
//        switch (data.getNettyType()) {
//            case QueryServerLevel:
//                exDate = StationPropertyWrap.getStationProperty(data);
//                break;
//            default:
//
//        }
//        return exDate;
        return BusinessRuse.getInstance().answer(data);
    }

    public void closeOtheNodes() {
        ServerHandler.broadcast(StationPropertyWrap.getCloseOthersMsg());
    }

    boolean startServer() {
        if (server == null) {

            server = new NettyServer(inetHost, port);
            try {
                server.startup();
            } catch (Exception e) {
                logger.error(e);
            } finally {

            }
        }
        AppManager.logEvent("启动服务器 " +  (inetHost == null ? serverName : inetHost ) + " " + port);
        return server.isOnServer();
    }


//    public void startup(String serverName, int port, int localLevel) {
//        AppManager.logit("开启集群监测");
//
//        this.serverName = serverName;
//        this.localLevel = localLevel;
//        this.port = port;
//        startServer();
//        AppManager.logit("启动服务器 " + serverName + " " + port);
//    }

    BeQuit a = new BeQuit() {

        @Override
        public boolean Quit() {
            saBye();
            return true;
        }
    };

    public void setServerName(String serverName) {
        if (this.serverName == null) {
            this.serverName = serverName;
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (this.port == 0) {
            this.port = port;
        }
    }
    public void setPort(String inetHost, int port) {
        if (this.port == 0) {
            this.port = port;
        }
        this.inetHost = inetHost;
    }
}
