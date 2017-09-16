package szhzz.Netty.Proxy;

import szhzz.App.AppManager;
import szhzz.App.BeQuit;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import szhzz.DataBuffer.DataConsumer;
import szhzz.DataBuffer.ObjBufferedIO;
import szhzz.Netty.Cluster.ExchangeDataType.*;
import szhzz.Netty.Cluster.UDP.UdpClient_Abstract;
import szhzz.Utils.DawLogger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2015/2/15.
 */

//@Immutable
public class ProxyHandler extends SimpleChannelInboundHandler<NettyExchangeData> {
    //ChannelInboundMessageHandlerAdapter
    //SimpleChannelInboundHandler
    private static DawLogger logger = DawLogger.getLogger(ProxyHandler.class);
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Hashtable<Channel, BufferedOutChanel> channelBuffer = new Hashtable<>();
    private static HashSet<String> feededList = new HashSet<String>();
    //    private static UdpClient_Netty udpClient = null;
    private static boolean useUdp = false;
//    private boolean isNio = false;
//    private static MarketProxy nodeStation = null;

    public static void setUdp(boolean useUdp) {
        UdpClient_Abstract.setInstance("UdpClient"); //UdpClient_Netty

        ProxyHandler.useUdp = useUdp;
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
                logger.error("Proxy Server forced disconnect ");
            }
            return true;
        }
    };

    public ProxyHandler() {
//        Config systemCfg = CfgProvider.getInstance("系统策略").getCfg("System");
//        if (systemCfg != null && systemCfg.propertyEquals("ProxyType", "Nio"))
//            isNio = true;
    }

    /**
     * @param data
     */
    public static void broadcast(NettyExchangeData data) {
        String msg = data.encode();
        for (Channel channel : channels) {
            //TODO UDP
            if (useUdp) {
                try {
                    UdpClient_Abstract.getInstance().send(data);
                } catch (Exception e) {
                    logger.error("UdpClient_Netty error!");
                }
            } else {
                synchronized (channelBuffer) {
                    BufferedOutChanel out = channelBuffer.get(channel);
                    if (out != null) {
                        out.push(msg);
                    } else {
                        logger.error("connection missed!");
                    }
                }
            }
        }
    }

    public static void sayBye() {
        for (Channel channel : channels) {
            channel.writeAndFlush("bye\r\n").addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static boolean isAlive() {
        return !channels.isEmpty();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        Channel incoming = ctx.channel();

        AppManager.getApp().logEvent("[Proxy]" + incoming.remoteAddress() + " 已经连接!");

//        channels.add(incoming);
//        channelBuffer.put(incoming, new BufferedOutChanel(incoming));
        try {
            feedData(incoming);
        } catch (Exception e) {
            logger.error(e);
        }

//        if (MyDate.getToday().isOpenTime(60)) {
//            channels.add(incoming);
//            channelBuffer.put(incoming, new BufferedOutChanel(incoming));
//        } else {
//            try {
//                feedData(incoming);
//            } catch (Exception ignored) {
//
//            }
//        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        channels.remove(incoming);
        if (useUdp) UdpClient_Abstract.getInstance().removeRemote(incoming.remoteAddress().toString());

        synchronized (channelBuffer) {
            BufferedOutChanel out = channelBuffer.remove(incoming);
            if (out != null) {
                out.stop();
            }
        }
        AppManager.getApp().logEvent("已经离开 [Proxy]" + incoming.remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyExchangeData msg) throws Exception {
        if (AppManager.isQuitApp()) return;
        if (msg.getNettyType() == ClusterProtocal.FUNCTION.RegisterStockCode) {

            BufferedOutChanel out = channelBuffer.get(ctx.channel());
            if (out != null) {
                //只增加不减少
                out.addRegister(RegisterStockCodeWrap.getStockCode(msg));
            }
        }
//        else if(msg.getNettyType() == ClusterProtocal.FUNCTION.RequistUdpData) {
//            UdpClient.addRemote(ctx.channel().remoteAddress().toString());
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        Channel incoming = ctx.channel();
        channels.remove(incoming);
        if (useUdp) UdpClient_Abstract.getInstance().removeRemote(incoming.remoteAddress().toString());

        synchronized (channelBuffer) {
            BufferedOutChanel out = channelBuffer.remove(incoming);

            if (out != null) {
                out.stop();
            }
        }
        ctx.close();
    }


    private void feedData(Channel incoming) throws InterruptedException {
    }

    private class BufferedOutChanel implements DataConsumer {
        ObjBufferedIO pushBuffer = null;
        long in = 0;
        long out = 0;
        boolean isClosed = false;
        private Channel privateChannel = null;
        private HashSet<String> register = null;

        void addRegister(String[] stockCodes) {
            if (stockCodes == null) return;

            if (register == null) {
                register = new HashSet<String>();
            }
            Collections.addAll(register, stockCodes);
        }

        void removeRegister(String stockCode) {
            if (register != null) {
                register.remove(stockCode);
            }
        }

        void push(final Object o, String stockCode) {
            if (register != null && !register.contains(stockCode)) {
                return;
            }
            push(o);
        }

        void push(final Object o) {
            if (isClosed) return;

            // 不确定的问题 : 会变化吗？
//            Channel channel,
//            if (channel != privateChannel) {
//                logger.error("channel != privateChannel! " +
//                        channel.remoteAddress().toString() + ":" +
//                        privateChannel.remoteAddress().toString());
//
//                privateChannel = channel;
//            }
            in++;
            if (pushBuffer != null) {
                pushBuffer.push(o);
            } else {
                in(o);
            }
        }

        void stop() {
            isClosed = true;
            if (pushBuffer != null) {
                pushBuffer.close();
            }
            pushBuffer = null;
//            if(privateChannel!=null){
//                channelBuffer.remove(privateChannel.remoteAddress());
//            }
        }

        BufferedOutChanel(Channel channel) {
            privateChannel = channel;

//            privateChannel.closeFuture().addListeners(new GenericFutureListener() {
//                @Override
//                public void operationComplete(Future future) throws Exception {
//                    AppManager.logit("Connection drop " + privateChannel.remoteAddress());
//                    BufferedOutChanel.this.stop();
//                }
//            });
//            pushBuffer = new ObjBufferedIO();
//            try {
//                pushBuffer.setReader(this, 100);
//            } catch (InterruptedException e) {
//                logger.error(e);
//                pushBuffer = null;
//            }

        }

        public long in(final Object obj) {
            if (isClosed) return -1;
//            if (!privateChannel.isActive()) {
//                stop();
//                return -1;
//            }

//            if (!privateChannel.isWritable()) {
//                if (privateChannel.isActive() && !isClosed) {
//                    AppManager.logit("[Proxy]" + privateChannel.remoteAddress().toString() + " 推送阻塞! " + out + "/" + in);
//                }
//            } else {
            out++;
            //不需理会 isWritable()？
            ChannelFuture future = privateChannel.writeAndFlush(obj.toString());
//            if (isNio) {
//                if (!future.isSuccess() && privateChannel.isActive()) {
//                    logger.error("[Proxy]" + privateChannel.remoteAddress().toString() + " 推送失败! " + out + "/" + in, future.cause());
//                }
//            }
//            }
            return 0;
        }

        @Override
        public long in(long dataID, Object obj) {
            return 0;
        }


//        CircleTimer circleTimer = new CircleTimer() {
//            @Override
//            public void execTask() {
//                AppManager.getApp().logit(privateChannel.remoteAddress().toString() + " 推送! " + out + "/" + in);
//            }
//        };
    }

}
