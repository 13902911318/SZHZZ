package szhzz.Netty.Cluster.UDP;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import szhzz.App.AppManager;
import szhzz.App.BeQuit;
import szhzz.Netty.Cluster.NettyServer;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;

/**
 * Created by HuangFang on 2015/3/15.
 * 9:24
 */
public class UdpServer {
    private static DawLogger logger = DawLogger.getLogger(UdpServer.class);
    private static AppManager App = AppManager.getApp();
    private final int port;
    private String hostname = null;
    private boolean onServer = false;
    private ChannelInitializer<Channel> serverInitializer = null;
    private boolean autoConnect = false;
    private EventLoopGroup wookerGroup = null;

    public static void main(String[] args) throws InterruptedException {
        try {
            App.setLog4J();
        } catch (Exception e) {

        }
        new UdpServer(7530).startServer();
        System.exit(0);
    }


    public UdpServer(int port) {
        this.port = port;
    }

    public UdpServer(String hostname, int port) {
        this.port = port;
        this.hostname = hostname;
    }


    void startServer() {
        if (serverInitializer == null) {
            serverInitializer = new UdpServerInitializer();
        }
        wookerGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(wookerGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(serverInitializer);

            if (hostname == null) {
                bootstrap.localAddress(new InetSocketAddress(port));
            } else {
                bootstrap.localAddress(new InetSocketAddress(hostname, port));
            }

            onServer = true;
            Channel c = bootstrap.bind().syncUninterruptibly().channel();
            c.closeFuture().await();
        } catch (Exception e) {
            logger.error(e);
        } finally {
            onServer = false;
            wookerGroup.shutdownGracefully();
            if (autoConnect) connectionListener.setCircleTime(10000);
        }
    }

    public void stop() {
        onServer = false;
        autoConnect = false;
        wookerGroup.shutdownGracefully();
    }

    public boolean isOnServer() {
        return onServer;
    }

    public void setServerInitializer(ChannelInitializer<Channel> serverInitializer) {
        this.serverInitializer = serverInitializer;
    }

    public void startup() {
        autoConnect = true;
        try {
            AppManager.executeInBack(new Runnable() {
                @Override
                public void run() {
                    startServer();
                }
            });
        } catch (Exception e) {
            logger.error(e);
        }
    }

    CircleTimer connectionListener = new CircleTimer() {
        @Override
        public void execTask() {
            try {
                logger.info("UDP Server restart...");
                startServer();
            } finally {
            }
        }
    };

    BeQuit quit = new BeQuit() {
        @Override
        public boolean Quit() {
            stop();
            return true;
        }
    };
}
