package szhzz.Netty.Proxy;

import com.sun.istack.internal.NotNull;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import szhzz.App.AppManager;
import szhzz.Config.CfgProvider;
import szhzz.Config.Config;
import szhzz.Config.SharedCfgProvider;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;

import java.util.Hashtable;

/**
 * Created by HuangFang on 2015/3/15.
 * 9:24
 */
public class ProxyServer {

    private static DawLogger logger = DawLogger.getLogger(ProxyServer.class);
    private static AppManager App = AppManager.getApp();
    private final int port;
    private boolean onServer = false;
    private EventLoopGroup bodsGroup = null;
    private EventLoopGroup wookerGroup = null;
    private ProxyInitializer proxyInitializer = null;
    private boolean isNio = true;
    private Channel serverChannel = null;
    private boolean serverClosed = false;
    private static Hashtable<String,String> ipToName= new Hashtable<>();


    public ProxyServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {
        App.setLog4J();
        new ProxyServer(7521).startServer();
    }

    private static void addIpToName(){
        try {
            Config cfg = SharedCfgProvider.getInstance("net").getCfg("Group");
            for (String computer : cfg.getChildrenNames()) {
                Config child = cfg.getChild(computer);
                String[] ips = child.getProperty("IP", "").split(";");
                for (String ip : ips) {
                    ipToName.put(ip, computer);
                }
            }
        }catch (Exception e){}
    }

    public static String getNameClint(Channel channel) {
        String name = null;
        if(channel == null) return name;

        String netAdd = channel.remoteAddress().toString();
        String[] add = netAdd.split(":");
        String ip = add[0].replace("/", "");
        if (add.length > 0) {
            name = ipToName.get(ip);;
        }
        if (name == null) {
            name = netAdd;
        }
        return name;
    }

    public void startServer() throws InterruptedException {
        serverClosed = false;

        Config systemCfg = CfgProvider.getInstance("Schedule").getCfg("System");
        addIpToName();

        isNio = !(systemCfg != null && systemCfg.propertyEquals("ProxyType", "Oio"));

        if (isNio) {
            bodsGroup = new NioEventLoopGroup();
            wookerGroup = new NioEventLoopGroup();
        } else {
            bodsGroup = new OioEventLoopGroup();
            wookerGroup = new OioEventLoopGroup();
        }
        proxyInitializer = new ProxyInitializer();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bodsGroup, wookerGroup);
            if (isNio) {
                bootstrap.channel(NioServerSocketChannel.class);
            } else {
                bootstrap.channel(OioServerSocketChannel.class);
            }
            bootstrap.childHandler(proxyInitializer);
            //bootstrap.childOption(ChannelOption.SO_BROADCAST, true); // (6)
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true);      //速度下降
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            AppManager.getApp().logEvent("[Proxy] " + (isNio ? "Nio" : "Oio") + " Server start ! " + port);
            onServer = true;
            serverChannel = bootstrap.bind(port).sync().channel();
            serverChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error(e);
        } finally {
            onServer = false;
            bodsGroup.shutdownGracefully().sync();
            wookerGroup.shutdownGracefully().sync();
            AppManager.getApp().logEvent("[Proxy] Server closed ! " + port);

            if (!serverClosed) {
                connectionListener.setCircleTime(10000);
            }
        }
    }

    public void closeServer() {
        serverClosed = true;
        serverChannel.close(); //disconnect();
    }


    public void startup() {
        try {
            App.executeInBack(new Runer());
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public boolean isOnServer() {
        return onServer;
    }

    class Runer implements Runnable {
        @Override
        public void run() {
            try {
                startServer();
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
    }

    CircleTimer connectionListener = new CircleTimer() {
        @Override
        public void execTask() {
            try {
                if (!serverClosed) {
                    startup();
                }
            } finally {
            }
        }
    };
}
