package szhzz.Netty.Cluster;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import szhzz.App.AppManager;
import szhzz.Config.Config;
import szhzz.Netty.Cluster.Net.ServerInitializer;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by HuangFang on 2015/3/15.
 * 9:24
 */
public class NettyServer {
    private static DawLogger logger = DawLogger.getLogger(NettyServer.class);
    private static AppManager App = AppManager.getApp();
    private final int port;
    private String inetHost = null;
    private AtomicBoolean onServer = new AtomicBoolean(false);
    private boolean isNio = true;
    private ChannelInitializer<SocketChannel> serverInitializer = null;

    public NettyServer(int port) {
        this.port = port;
    }
    public NettyServer(String inetHost, int port) {
        this.inetHost = inetHost;
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
        if (!onServer.compareAndSet(false, true)) return;

        if(serverInitializer == null){
            serverInitializer = new ServerInitializer();
        }
        EventLoopGroup bodsGroup = new NioEventLoopGroup();
        EventLoopGroup wookerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bodsGroup, wookerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(serverInitializer);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
            bootstrap.childOption(ChannelOption.SO_REUSEADDR, true); //重要！

            if(inetHost != null){
                bootstrap.bind(inetHost, port).sync().channel().closeFuture().sync();
            }else{
                bootstrap.bind(port).sync().channel().closeFuture().sync();
            }
        } catch (InterruptedException e) {
            logger.error(e);
        } finally {
            bodsGroup.shutdownGracefully();
            wookerGroup.shutdownGracefully();
            connectionListener.setCircleTime(10000); //delay and restart
            onServer.set(false);
        }
    }

    public void startServerOio() throws InterruptedException {
        if (!onServer.compareAndSet(false, true)) return;

        EventLoopGroup bodsGroup = new OioEventLoopGroup();
        EventLoopGroup wookerGroup = new OioEventLoopGroup();
        if(serverInitializer == null){
            serverInitializer = new ServerInitializer();
        }
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bodsGroup, wookerGroup);
            bootstrap.channel(OioServerSocketChannel.class);
            bootstrap.childHandler(serverInitializer);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
            bootstrap.childOption(ChannelOption.SO_REUSEADDR, true); //重要！


            if(inetHost != null){
                bootstrap.bind(inetHost, port).sync().channel().closeFuture().sync();
            }else{
                bootstrap.bind(port).sync().channel().closeFuture().sync();
            }

        } catch (InterruptedException e) {
            logger.error(e);
        } finally {
            bodsGroup.shutdownGracefully();
            wookerGroup.shutdownGracefully();
            connectionListener.setCircleTime(10000);
            onServer.set(false);
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
        return onServer.get();
    }

    public void setServerInitializer(ChannelInitializer<SocketChannel> serverInitializer) {
        this.serverInitializer = serverInitializer;
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
