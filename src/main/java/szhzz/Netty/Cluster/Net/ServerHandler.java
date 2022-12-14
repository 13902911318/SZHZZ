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
import szhzz.Netty.Cluster.ClusterClients;
import szhzz.Netty.Cluster.ClusterServer;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Netty.Cluster.ExchangeDataType.StationPropertyWrap;
import szhzz.Utils.DawLogger;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


/**
 * Created by Administrator on 2015/2/15.
 */
//@Immutable
public class ServerHandler extends SimpleChannelInboundHandler<NettyExchangeData> {
    private static DawLogger logger = DawLogger.getLogger(ServerHandler.class);
    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    static final HashMap<String, Channel> namedChannels = new HashMap<>();
    ClusterServer clusterServer = null;


    public ServerHandler() {
        clusterServer = ClusterServer.getInstance();
    }

    public static boolean hasConnection() {
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
            } else {
                logger.debug("!channel.isWritable() ");
            }
        }
    }

    public static void broadcast(NettyExchangeData msg, List<String> host) {
        if (Cluster.getInstance().isOffLine()) return;
        Channel channel;
        for (Channel channel_ : channels) {
            channel = null;
            for (String address : host) {
                if (channel_.remoteAddress().toString().contains(address)) {
                    if (channel_.isWritable()) {
                        channel = channel_;
                        break;
                    }
                }
            }
            if (channel == null) continue;
            channel.writeAndFlush(msg.encode());
        }
    }

    /**
     *
     * @param stationName
     * @return
     */
    public static boolean hasByPassChannal(String stationName) {
        return getBypassChannel(stationName) != null;
    }

    /**
     * 标志 2
     *
     * @param msg
     * @param distHostName
     * @return
     */
    public static int bypassSendTo(NettyExchangeData msg, String distHostName) {
        Channel channel = getBypassChannel(distHostName);
        if (channel != null && channel.isWritable()) {
            if(StationPropertyWrap.isRouterDebug(msg)) {
                StationPropertyWrap.addRouter(msg, "2. " + AppManager.getHostName() + ".ServerHandler.bypassSendTo");
                logger.info("标志 2 ID=" + msg.getRequestID() + " " +
                        AppManager.getHostName() + "->" + msg.getIpAddress());
            }
            channel.writeAndFlush(msg.encode());
            logger.info("经由服务器端发送数据成功: 发往" + distHostName + " 请求类型=" + msg.getNettyType().name());
            return 1;
        }

        logger.info("经由服务器端发送数据失败: 发往" + distHostName + " 请求类型=" + msg.getNettyType().name());
        return -1;
    }

    public static Channel getBypassChannel(String stationName) {
        return namedChannels.get(stationName);

//            for (Channel channel : channels) {
//                for (String address : host) {
//                    String debug = channel.remoteAddress().toString();
//                    if (channel.remoteAddress().toString().contains(address)) {
//                        if (channel.isWritable()) {
//                            return channel;
//                        }
//                        break;
//                    }
//                }
//            }
//        }
        //return null;
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

    /**
     * @param ctx the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *            belongs to
     * @param msg the message to handle
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyExchangeData msg) throws Exception {
        if (AppManager.isQuitApp()) return;
        //标志 6
        if (msg.isByPass()) {
            if(StationPropertyWrap.isRouterDebug(msg)) {
                StationPropertyWrap.addRouter(msg, "6. " + AppManager.getHostName() + "." + this.getClass().getSimpleName() + ".channelRead0");
                logger.info("标志 6 ID=" + msg.getRequestID() + "  " +
                        msg.getIpAddress() + "<-" + msg.getHostName());
            }
            ClusterClients.getInstance().callBack(msg);
            return;
        }

        ArrayList<NettyExchangeData> exDates = answer(msg);
        if (exDates != null && exDates.size() > 0) {
            for (NettyExchangeData exDate : exDates) {
                if (exDate != null) {
                    ctx.writeAndFlush(exDate.encode());
                }
            }
        }
        String hostName = msg.getHostName();
        if(!namedChannels.containsKey(hostName)){
            namedChannels.put(hostName, ctx.channel());
        }
    }

    public ArrayList<NettyExchangeData> answer(NettyExchangeData data) {
        return clusterServer.answer(data);
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
