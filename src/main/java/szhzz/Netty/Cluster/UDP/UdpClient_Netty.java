package szhzz.Netty.Cluster.UDP;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import szhzz.App.AppManager;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;

import java.net.InetSocketAddress;
import java.util.Hashtable;

/**
 * Created by Administrator on 2017/3/29.
 */
public class UdpClient_Netty extends UdpClient_Abstract {
    private static DawLogger logger = DawLogger.getLogger(UdpClient_Netty.class);
    private static AppManager App = AppManager.getApp();

    private Hashtable<String, Client> clients = new Hashtable<>();

    public static void main(String[] args) {
        App.setLog4J();
        UdpClient_Netty client = new UdpClient_Netty();
        client.addRemote("195.168.0.125");
        client.ConnectionListener.execTask();
    }


    UdpClient_Netty() {
    }

    @Override
    public void addRemote(String host) {
        if (host.isEmpty()) {
            host = "127.0.0.1";
        }
        host = host.replace("/", "");
        if (host.indexOf(":") > 0) {
            host = host.substring(0, host.indexOf(":"));
        }
        if (clients.containsKey(host)) return;

        clients.put(host, new Client(host, getPort()));
    }

    @Override
    public void removeRemote(String host) {
        Client c = clients.remove(host);
        if (c != null) {
            close();
        }
    }

    @Override
    public void send(NettyExchangeData data) {
        for (Client c : clients.values()) {
            c.writeAndFlush(data);
        }
    }

    public void close() {
        for (Client c : clients.values()) {
            if (c != null) {
                c.close();
            }
        }
        clients.clear();
    }


    private class Client {
        NioEventLoopGroup group = null;
        Channel channel = null;
        String host = "";
        int port = 7530;

        Client(String host, int port) {
            this.host = host;
            this.port = port;

            group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group);
                bootstrap.channel(NioDatagramChannel.class);
                bootstrap.option(ChannelOption.SO_BROADCAST, true); // (4)
                bootstrap.handler(new UdpEncoder(new InetSocketAddress(host, port)));

                logger.info("Try to connect " + host + " " + port);

                channel = bootstrap.bind(0).sync().channel();   // channel
            } catch (Exception e1) {
                logger.info("Error on connect to " + host + " " + port);
                logger.error(e1);
            }
        }

        ChannelFuture writeAndFlush(NettyExchangeData data) {
            return channel.writeAndFlush(data);
        }

        void close() {
            group.shutdownGracefully();
        }
    }

    CircleTimer ConnectionListener = new CircleTimer() {
        int count = 0;

        @Override
        public void execTask() {
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

            send(data);

            if (++count < 3) {
                setCircleTime(10 * 1000);
            } else {
                close();
                System.out.println("Test finished");
                System.exit(0);
            }
        }
    };

}
