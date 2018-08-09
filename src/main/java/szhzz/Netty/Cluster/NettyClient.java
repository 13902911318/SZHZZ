package szhzz.Netty.Cluster;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import szhzz.App.AppManager;
import szhzz.App.BeQuit;
import szhzz.Config.Config;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Netty.Cluster.Net.ClientInitializer;
import szhzz.Netty.Cluster.Net.ServerHandler;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by HuangFang on 2015/3/15.
 * 9:24
 */
public class NettyClient {
//    private static final ArrayList<NodeClient> allClients = new ArrayList<>();

    private static DawLogger logger = DawLogger.getLogger(NettyClient.class);
    private static AppManager App = AppManager.getApp();
    private static long requID = 0l;
    private final Object locker = new Object();
    private LinkedList<String> host = new LinkedList<>();
    private int hostIndex = 0;
    protected int port;
    private Channel channel = null;
    private EventLoopGroup group = null;
    private ClientInitializer clientInitializer = null;
    private boolean connected = false;
    private boolean autoReconnect = false;
    protected boolean isNio = true;
    private int retry = 0;
    private int connectionTimeout = 10;
    private ClientInspector  inspector = null;

    public static void main(String[] args) {
        App.setLog4J();
        NettyClient client = new NettyClient(args, 7521);
        client.start();
        NettyExchangeData data = new NettyExchangeData();
        data.appendRow();

        data.addData(0); //colErrCode
        data.addData("login ID"); //colLogonID
        data.addData("Request");  //colRequestID
        data.addData("FUN-id");  // colFunID
        data.addData("No Error");  //colMessage

        data.appendRow();
        data.appendRow();
        data.addData("first");
        data.addData("second");
        data.addData("3");
        client.send(data);
        client.sayBye();
    }


    public NettyClient(String[] host, int port) {
        Collections.addAll(this.host, host);
        hostIndex = 0;
        retry = this.host.size();
        this.port = port;
    }

    public void setHost(String[] host, int port) {
        this.host.clear();

        Collections.addAll(this.host, host);
        hostIndex = 0;
        retry = this.host.size();
        this.port = port;
    }

    public void connected() {
        connected = true;
        retry = Math.min(this.host.size() - hostIndex, 3);
        if(inspector!=null){
            inspector.connected(channel);
        }
   }

    public void disConnected() {
        if (host.size() > 1) {
            if (--retry == 0) {
                if (++hostIndex >= host.size()) {
                    hostIndex = 0;
                }
                retry = Math.min(this.host.size() - hostIndex, 3);
            }
        }
        connected = false;
        if(inspector!=null){
            inspector.disConnected();
        }
        if (autoReconnect) {
            start();
        }
    }

    protected void connect() {
        Config systemCfg = App.getCfg();//Change to CfgProvider
        if (systemCfg != null && systemCfg.propertyEquals("ProxyType", "Oio")) {
            isNio = false;
        } else {
            isNio = true;
        }
        if (isNio) {
            connectNio();
        } else {
            connectOio();
        }
    }

    protected void connectNio() {
        if (clientInitializer == null) {
            clientInitializer = new ClientInitializer();
        }
        group = new NioEventLoopGroup();
        try {

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout);
            bootstrap.handler(clientInitializer);


            logger.info("Try to connect " + host.get(hostIndex) + " " + port);
            ChannelFuture future = bootstrap.connect(host.get(hostIndex), port).sync(); // 等待建立连接
            channel = future.channel();   // 连接后获取 channel
            connected();
            logger.info("connected to " + host.get(hostIndex) + " " + port);

            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("Error on connect to " + host + " " + port, e);
        } catch (Exception e1) {
            logger.info("Connection false for " + e1.getClass().getSimpleName() + " " + host + " " + port);
        } finally {
            group.shutdownGracefully();
            group = null;
            disConnected();
        }
    }

    protected void connectOio() {
        if (clientInitializer == null) {
            clientInitializer = new ClientInitializer();
        }
        group = new OioEventLoopGroup();
        try {

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(OioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout);
            bootstrap.handler(clientInitializer);


            logger.info("Try to connect " + host.get(hostIndex) + " " + port);
            ChannelFuture future = bootstrap.connect(host.get(hostIndex), port).sync(); // 等待建立连接
            channel = future.channel();   // 连接后获取 channel
            connected();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("Error on connect to " + host + " " + port, e);
        } catch (Exception e1) {
            logger.info("Connection false for " + e1.getClass().getSimpleName() + " " + host + " " + port);
        } finally {
            group.shutdownGracefully();
            group = null;
            disConnected();
        }
    }

    public String getHost() {
        if (host.size() == 0) return "";
        if (hostIndex >= host.size()) {
            hostIndex = 0;
        }
        return host.get(hostIndex);
    }

    public boolean isConnected() {
        return connected;
    }

    public void sayBye() {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush("bye\r\n");
        }
    }

    public void flagSignal(String semaphore) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(semaphore);
        }
    }

    /**
     * @param msg
     * @return requID 回执号!
     */
    public long send(NettyExchangeData msg) {
        if (!connected) {
            //尝试经由服务器端发送
//            msg.setByPass();
//            return ServerHandler.bypassSendTo(msg, host);
            logger.debug("连接已断开");
            return -1;
        }
        if (!channel.isWritable()) {
            logger.info(new Exception("发送数据失败，[" + channel.remoteAddress() + "]\n" + msg.toString()));
            return -1;
        }
//        NettyExchangeData d = ((NettyExchangeData) msg);
        synchronized (locker) {
//            d.setRequestID(++requID);
            channel.writeAndFlush(msg.encode());
//            return requID;
            return 1;
        }
    }

    public void start() {
        autoReconnect = true;
        ConnectionListener.setCircleTime(100);
    }

    CircleTimer ConnectionListener = new CircleTimer() {
        @Override
        public void execTask() {
            try {
                if (autoReconnect && !connected) {
//                    connected = true;
                    connect();
                }
            } finally {
                setCircleTime(10 * 1000);
            }
        }
    };


    public void disconnectFromServer() {
        autoReconnect = false;
        ConnectionListener.stopTimer();

        sayBye(); //不能保证远端服务器会自动断开
        if (group != null) group.shutdownGracefully();
        channel = null;
        connected = false;
    }

    BeQuit quit = new BeQuit() {
        @Override
        public boolean Quit() {
            disconnectFromServer();
            return false;
        }
    };

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setInspector(ClientInspector inspector) {
        this.inspector = inspector;
    }
}
