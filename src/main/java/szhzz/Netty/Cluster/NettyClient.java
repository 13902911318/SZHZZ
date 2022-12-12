package szhzz.Netty.Cluster;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import szhzz.App.AppManager;
import szhzz.App.BeQuit;
import szhzz.Config.Config;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Netty.Cluster.ExchangeDataType.StationPropertyWrap;
import szhzz.Netty.Cluster.Net.ClientInitializer;
import szhzz.Netty.Cluster.Net.ServerHandler;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;
import szhzz.Utils.NU;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import static szhzz.Netty.Cluster.ExchangeDataType.ClusterProtocal.isCluster;

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
    private LinkedList<String> hosts = new LinkedList<>();
    private LinkedList<String> IPs = null;
    private int hostIndex = 0;
//    protected int port;
    private Channel channel = null;
    private EventLoopGroup group = null;
    private ChannelInitializer clientInitializer = null;
    private boolean connected = false;
    private boolean autoReconnect = false;
    protected boolean isNio = true;
    private int retry = 0;
    private int connectionTimeout = 1000;
    private ClientInspector inspector = null;
    private int circleTime = 10 * 1000;
    private AtomicBoolean lockOrSkip = new AtomicBoolean(false);


    public static void main(String[] args) {
        App.setLog4J();
        NettyClient client = new NettyClient(args);
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


    public NettyClient(String[] host) {
        setHosts(host);
    }

    public LinkedList<String> getHosts() {
        return hosts;
    }

//    public int getPort() {
//        return port;
//    }

    public void setHosts(String[] hosts) {
        this.hosts.clear();
        IPs = null;
        addHost(hosts);
    }


//    public void setHost(String[] host, int port, int hostIndex) {
//        setHost(host, port);
//        this.hostIndex = hostIndex;
//    }

    public void addHost(String[] host) {
        Collections.addAll(this.hosts, host);
        hostIndex = 0;
        retry = this.hosts.size();
        IPs = null;
    }

    public void connected() {
        connected = true;
        retry = Math.min(this.hosts.size() - hostIndex, 3);
        if (inspector != null) {
            inspector.connected(channel);
        }
    }

    public void disConnected() {
        if (hosts.size() > 1) {
            if (--retry == 0) {
                if (++hostIndex >= hosts.size()) {
                    hostIndex = 0;
                }
                retry = Math.min(this.hosts.size() - hostIndex, 3);
            }
        }
        connected = false;
        if (inspector != null) {
            inspector.disConnected();
        }
        if (autoReconnect) {
            ConnectionListener.setCircleTime(circleTime);
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

    String getIp(){
        String address = hosts.get(hostIndex);
        return address.substring(0, address.indexOf(":"));
    }

    LinkedList<String> getIps(){
        if(IPs == null){
            IPs = new LinkedList<>();
            for(String address : hosts){
                IPs.add(address.substring(0, address.indexOf(":")));
            }
        }
        return IPs;
    }

    int getPort(){
        String address = hosts.get(hostIndex);
        return NU.parseInt(address.substring(address.indexOf(":")+1), -1);
    }

    protected void connectNio() {
        if (!lockOrSkip.compareAndSet(false, true)) return;

        String ip = getIp();
        int port = getPort();
        try {
            if (clientInitializer == null) {
                clientInitializer = ClientInitializer.getInstance();
            }
            group = new NioEventLoopGroup();

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout);
            bootstrap.handler(clientInitializer);


            logger.info("Try to connect (Nio)" + ip + " " + port);
            ChannelFuture future = bootstrap.connect(ip, port).sync(); //阻塞,等待建立连接
            channel = future.channel();   // 连接后获取 channel

            connected();
            logger.info("connected to " + ip + " " + port);

            channel.closeFuture().sync();  //阻塞直到断开
        } catch (InterruptedException e) {
            logger.error("Error on connect to " + ip + " " + port, e);
        } catch (Exception e1) {
            logger.info("Connection false for " + e1.getClass().getSimpleName() + " " + ip + " " + port);
        } finally {
            if (group != null) {
                group.shutdownGracefully();
            }
            group = null;

            lockOrSkip.set(false);
            disConnected();
        }
    }

    protected void connectOio() {
        if (!lockOrSkip.compareAndSet(false, true)) return;
        String ip = getIp();
        int port = getPort();
        try {
            if (clientInitializer == null) {
                clientInitializer = ClientInitializer.getInstance();
            }
            group = new OioEventLoopGroup();

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(OioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout);
            bootstrap.handler(clientInitializer);

            logger.info("Try to connect (Oio)" + ip + " " + port);
            ChannelFuture future = bootstrap.connect(ip, port).sync(); // 等待建立连接
            channel = future.channel();   // 连接后获取 channel
            connected();
            logger.info("Connected to " + ip + " " + port);
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("Error on connect to " + ip + " " + port, e);
        } catch (Exception e1) {
            logger.info("Connection false for " + e1.getClass().getSimpleName() + " " + ip + " " + port);
        } finally {
            if (group != null) {
                group.shutdownGracefully();
            }
            group = null;
            lockOrSkip.set(false);
            disConnected();
        }
    }

    public String getHost() {
        if (hosts.size() == 0) return "";
        if (hostIndex >= hosts.size()) {
            hostIndex = 0;
        }
        return hosts.get(hostIndex);
    }

    public boolean isConnected() {
        return connected;
    }

    public void sayBye() {
        autoReconnect = false;
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
     * 标志 1
     *
     * @param msg
     * @return requID 回执号!
     */
    public long send_old(NettyExchangeData msg) {
        if (!isConnected()) {
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

    public long send(NettyExchangeData msg) {
        if (!isConnected() ) {
            //尝试经由服务器端发送
            if(isCluster(msg.getEvent())) {
                msg.setByPass();
//                msg.setRequestID(++requID);
                if (StationPropertyWrap.isRouterDebug(msg)) {
//            logger.info("标志 1 ID=" + msg.getRequestID() + " " +
//                    AppManager.getHostName() + "->" + msg.getIpAddress());
                    StationPropertyWrap.addRouter(msg, "1. " + AppManager.getHostName() + "." + this.getClass().getSimpleName() + ".send");
                }
                int debug = ServerHandler.bypassSendTo(msg, hosts);
                return 1; // 如果对方未开机，不是错误
            }
            logger.info(new Exception("发送数据失败，[未连接]\n" + msg.toString()));
        }
        if (channel == null) {
            logger.info(new Exception("发送数据失败，[连接已断开]\n" + msg.toString()));
            return -1;
        } else if (!channel.isWritable()) {
            logger.info(new Exception("发送数据失败，[" + channel.remoteAddress() + "]\n" + msg.toString()));
            return -1;
        }
//        NettyExchangeData d = ((NettyExchangeData) msg);
        synchronized (locker) {
//            d.setRequestID(++requID);
//            msg.setRequestID(++requID);
            channel.writeAndFlush(msg.encode());
//            return requID;
            return 1;
        }
    }


    CircleTimer ConnectionListener = new CircleTimer() {
        @Override
        public void execTask() {
            if (autoReconnect && !isConnected()) {
//                        if (!AppManager.getApp().isSilentTime()) {
                connect();//阻塞直到断开连接
//                        }
            }
        }
    };

    public void start() {
        autoReconnect = true;
        ConnectionListener.setCircleTime(100);
    }

    public void disconnectFromServer() {
        autoReconnect = false;
        ConnectionListener.stopTimer();

        sayBye(); //不能保证远端服务器会自动断开
        if (group != null) group.shutdownGracefully();
        channel = null;
        lockOrSkip.set(false);
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

    public void setClientInitializer(ChannelInitializer clientInitializer) {
        this.clientInitializer = clientInitializer;
    }

    public void setTimer(int circleTime) {
        this.circleTime = circleTime;
    }

    public int getHostIndex() {
        return hostIndex;
    }

    public void setHostIndex(int hostIndex) {
        if (hostIndex >= hosts.size()) hostIndex = 0;
        this.hostIndex = hostIndex;
    }
}
