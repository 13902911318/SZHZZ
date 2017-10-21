package szhzz.Netty.Cluster.Net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import szhzz.App.AppManager;
import szhzz.App.BeQuit;
import szhzz.Netty.Cluster.Cluster;
import szhzz.Netty.Cluster.ClusterServer;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Utils.DawLogger;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2015/2/15.
 */
//@Immutable
public class ServerHandler extends SimpleChannelInboundHandler<NettyExchangeData> {
    private static DawLogger logger = DawLogger.getLogger(ServerHandler.class);
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private ClusterServer clusterServer = null;


    public ServerHandler() {
        clusterServer = ClusterServer.getInstance();
    }

    public static boolean hasConnection(){
        if (Cluster.getInstance().isOffLine()) return false;
        return channels.size() > 0;
    }

    /**
     * 广播的方式,避免遗漏
     *
     * @param msg
     */
    public static void broadcast(NettyExchangeData msg) {
//        if (BusinessRuse.getInstance().isIndependent()) return;
        if (Cluster.getInstance().isOffLine()) return;

        for (Channel channel : channels) {
            if (channel.isWritable()) {
                channel.writeAndFlush(msg.encode());
            }else{
                logger.debug("!channel.isWritable() ");
            }
        }
    }

    public static int bypassSendTo(NettyExchangeData msg, LinkedList<String> host) {
        Channel channel = getBypassChannel(host);
        if (channel != null) {
            channel.writeAndFlush(msg.encode());
            logger.info("经由服务器端发送数据成功: 发往" + host.get(0) + " 请求类型=" + msg.getNettyType().name());
            return 1;
        }
        logger.info("经由服务器端发送数据失败 " + msg.getNettyType().name());
        return -1;
    }

    public static Channel getBypassChannel(LinkedList<String> host) {
        if (!Cluster.getInstance().isOffLine()) {
            for (Channel channel : channels) {
                for (String address : host) {
                    if (channel.remoteAddress().toString().contains(address)) {
                        if (channel.isWritable()) {
                            return channel;
                        }
                        break;
                    }
                }
            }
        }
        return null;
    }

    public static void sayBye() {
        for (Channel channel : channels) {
            channel.writeAndFlush("bye\r\n").addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        channels.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        channels.remove(ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }



    ///////////////////////////////////////////////
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyExchangeData msg) throws Exception {
        if (AppManager.isQuitApp()) return;
        ArrayList<NettyExchangeData> exDates = clusterServer.answer(msg);
        if(exDates!= null && exDates.size() > 0){
            for(NettyExchangeData exDate : exDates){
                if (exDate != null) {
                    ctx.writeAndFlush(exDate.encode());
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel incoming = ctx.channel();
        SocketAddress add = incoming.remoteAddress();
        channels.remove(incoming);
        ctx.close();
        logger.info(cause);
    }

    static BeQuit a = new BeQuit(1) {

        @Override
        public boolean Quit() {
            logger.debug("Quit Cluster Server channels count = " + channels.size());

            sayBye();
            int count = 0;
            while (count < 5 && channels.size() > 0) {
                try {
                    TimeUnit.MICROSECONDS.sleep(500);
                } catch (InterruptedException e) {

                }
                sayBye();
                count++;
            }
            if (channels.size() > 0) {
                logger.error("Cluster Server forced disconnect ");
            }
            return true;
        }
    };
}
