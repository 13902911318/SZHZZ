package szhzz.Netty.Cluster;

import szhzz.App.AppManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import szhzz.Config.Config;
import szhzz.Netty.Cluster.Net.ServerInitializer;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;


/**
 * Created by HuangFang on 2015/3/15.
 * 9:24
 */
public class NettyServer {
    private static DawLogger logger = DawLogger.getLogger(NettyServer.class);
    private static AppManager App = AppManager.getApp();
    private final int port;
    private boolean onServer = false;
    private boolean isNio = true;

    public NettyServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {
        App.setLog4J();
        new NettyServer(7522).startServer();
    }

    public void startServer() throws InterruptedException {
        Config systemCfg = App.getCfg();
        if (systemCfg != null && systemCfg.propertyEquals("ProxyType", "Oio")) {
            isNio = false;
        } else {
            isNio = true;
        }
        if (isNio) {
            startServerNio();
        } else {
            startServerOio();
        }
    }

    public void startServerNio() throws InterruptedException {
        if (onServer) return;

        onServer = true;
        EventLoopGroup bodsGroup = new NioEventLoopGroup();
        EventLoopGroup wookerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bodsGroup, wookerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ServerInitializer());
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
            bootstrap.childOption(ChannelOption.SO_REUSEADDR, true); //重要！


            bootstrap.bind(port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error(e);
        } finally {
            onServer = false;
            bodsGroup.shutdownGracefully();
            wookerGroup.shutdownGracefully();
            connectionListener.setCircleTime(10000);
        }
    }

    public void startServerOio() throws InterruptedException {
        if (onServer) return;

        onServer = true;
        EventLoopGroup bodsGroup = new OioEventLoopGroup();
        EventLoopGroup wookerGroup = new OioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bodsGroup, wookerGroup);
            bootstrap.channel(OioServerSocketChannel.class);
            bootstrap.childHandler(new ServerInitializer());
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
            bootstrap.childOption(ChannelOption.SO_REUSEADDR, true); //重要！


            bootstrap.bind(port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error(e);
        } finally {
            onServer = false;
            bodsGroup.shutdownGracefully();
            wookerGroup.shutdownGracefully();
            connectionListener.setCircleTime(10000);
        }
    }

    public void startup() {
        try {
            AppManager.executeInBack(new Runer());
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
                logger.info("Cluster Server restart...");
                startup();
            } finally {
            }
        }
    };

}
