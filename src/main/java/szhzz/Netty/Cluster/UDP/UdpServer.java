package szhzz.Netty.Cluster.UDP;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import szhzz.App.AppManager;
import szhzz.Utils.DawLogger;

import java.net.InetSocketAddress;

/**
 * Created by HuangFang on 2015/3/15.
 * 9:24
 */
public class UdpServer {
    private static DawLogger logger = DawLogger.getLogger(UdpServer.class);
    private static AppManager App = AppManager.getApp();
    private final int port;
    private boolean onServer = false;

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

    public void startServer() throws InterruptedException {
        wookerGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(wookerGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new UdpServerInitializer());

            bootstrap.localAddress(new InetSocketAddress(port));

            onServer = true;
            Channel c = bootstrap.bind().syncUninterruptibly().channel();
            c.closeFuture().await();
        } catch (Exception e) {
            logger.error(e);
        } finally {
            onServer = false;
            wookerGroup.shutdownGracefully();
        }
    }

    public void stop() {
        onServer = false;
        wookerGroup.shutdownGracefully();
    }

    public boolean isOnServer() {
        return onServer;
    }

    //TODO 状态自检报告
}
