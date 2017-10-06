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
import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2015/2/15.
 */
//@Immutable
public class ServerHandler extends SimpleChannelInboundHandler<NettyExchangeData> {
    //ChannelInboundMessageHandlerAdapter
    //SimpleChannelInboundHandler
    private static DawLogger logger = DawLogger.getLogger(ServerHandler.class);
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private ClusterServer clusterServer = null;
    //private NettyExchangeData data = null;


    public ServerHandler() {
        clusterServer = ClusterServer.getInstance();
//        clusterServer.setServerHandler(this);
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
            }
//            channel.writeAndFlush(msg.encode()).addListeners(new GenericFutureListener() {
//                //TODO 不需要？
//                @Override
//                public void operationComplete(Future future) throws Exception {
//                    if (!future.isSuccess()) {
//                        logger.error("发送数据失败 [" + channel.remoteAddress() + "]\n" + msg, future.cause());
//                    }
//                }
//            });
        }
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
//        channelMap.put(ctx, null);

        //incoming.writeAndFlush("[SERVER]" + incoming.remoteAddress() + "has joined!\n");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
//        for (Channel channel : channels) {
        //incoming.writeAndFlush("[SERVER]" + incoming.remoteAddress() + " has left!\n");
//        }
        channels.remove(ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
//        if (AppManager.isQuitApp()) return;
//
//        Channel channel = ctx.channel();
//
//        if ("bye".equals(msg)) {
//            ctx.writeAndFlush("bye\r\n");
//            ctx.close();
//            channelMap.remove(channel);
//            return;
//        }
//
//
//        if (NettyExchangeData.BoD.equals(msg)) {
//            channelMap.put(channel, new NettyExchangeData());
//        } else if (NettyExchangeData.EoD.equals(msg)) {
//            NettyExchangeData data = channelMap.get(channel);
//            if (data != null && !ClusterStation.isIndependent()) {
//                if (data.getNettyType() == ClusterProtocal.FUNCTION.Disconnect) {
//                    ctx.writeAndFlush("bye\r\n");
//                    ctx.close();
//                } else {
//                    if (data.isSameLanguage()) {
//                        //TODO 改为异步 ? 数据量不大的时候，这样较为简单
//                        NettyExchangeData exDate = clusterServer.answer(data);   //答复客户端的查询
//                        if (!ctx.channel().isWritable()) {
//                            logger.error(new Exception("回复数据失败。[" + channel.remoteAddress() + "]"));
//                        } else if (exDate != null) {
//                            ctx.channel().writeAndFlush(exDate.encode());
//                        }
//                    }
//                }
//            }
//            channelMap.remove(channel);
//        } else {
//            NettyExchangeData data = channelMap.get(channel);
//            if (data != null) {
//                data.decode(msg);
//            } else {
//                int a = 0;
//            }
//        }
//    }

    ///////////////////////////////////////////////
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyExchangeData msg) throws Exception {
        if (AppManager.isQuitApp()) return;
        NettyExchangeData exDate = clusterServer.answer(msg);
        if (exDate != null) {
            ctx.writeAndFlush(exDate.encode());
        }
    }

//    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
//        if (AppManager.isQuitApp()) return;
//
//        Channel incoming = ctx.channel();
//
//        if ("bye".equals(msg)) {
//            ctx.writeAndFlush("bye\r\n");
//            ctx.close();
//            return;
//        }
//
//
//        if (NettyExchangeData.BoD.equals(msg)) {
//            data = new NettyExchangeData();
//        } else if (NettyExchangeData.EoD.equals(msg)) {
//            if (!ClusterStation.isIndependent()) {
//                //if (ClusterProtocal.FUNCTION.values()[data.getFunID().intValue()] == ClusterProtocal.FUNCTION.Disconnect) {
//                if (data == null) {
//                    logger.error("数据失配 1");
//                    return;
//                }
//                if(data.getNettyType() == ClusterProtocal.FUNCTION.Disconnect){
//                    ctx.writeAndFlush("bye\r\n");
//                    ctx.close();
//                } else {
//                    if (data.isSameLanguage()) {
//                        Object o = clusterServer.answer(data);
//                        if (o != null) {
//                            if (o instanceof NettyExchangeData) {
//                                NettyExchangeData exDate = (NettyExchangeData) o;
//                                exDate.setIpAddress(data.getIpAddress()); //数据进入本站的时候设置了 data.getIpAddress()
//                                exDate.setRequestID(data.getRequestID().intValue());
//                                ctx.writeAndFlush(exDate.encode());
//                            }
//                        } else {
//                            ctx.writeAndFlush(o.toString());
//                        }
//                    } else {
//                        int a = 0;
//                    }
//                }
//            }
//            data = null;
//        } else {
//            if (data != null) {
//                data.decode(msg);
//            }else{
//                logger.error("数据失配");
//            }
//        }
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        cause.printStackTrace();
        Channel incoming = ctx.channel();
        SocketAddress add = incoming.remoteAddress();
        channels.remove(incoming);
        ctx.close();
        logger.info(cause);
    }

    BeQuit a = new BeQuit(1) {

        @Override
        public boolean Quit() {
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
