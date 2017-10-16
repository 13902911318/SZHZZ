package szhzz.Netty.Cluster.Net;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import szhzz.DataBuffer.DataConsumer;
import szhzz.DataBuffer.ObjBufferedIO;
import szhzz.Netty.Cluster.BusinessRuse;
import szhzz.Netty.Cluster.ClusterClients;
import szhzz.Netty.Cluster.ClusterServer;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Utils.DawLogger;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/2/15.
 * TODO 改为 SimpleChannelInboundHandler<NettyExchangeData> ？
 */
public class ClientHandler extends SimpleChannelInboundHandler<NettyExchangeData> implements DataConsumer {
    private static DawLogger logger = DawLogger.getLogger(ClientHandler.class);
    protected ObjBufferedIO dataBuffer = null;

    public ClientHandler() {

    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        setBufferSize(10); //?
        ctx.fireChannelActive();
    }

    /**
     * ChannelInboundHandlerAdapter  you  are  responsible  to  release
     * resources  after  you  handled  the  received message.  ??
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        // NettyExchangeData  msg
//        if (dataBuffer != null) {
//            dataBuffer.push(msg);
//        } else {
//            in(msg);
//        }
//    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyExchangeData msg) throws Exception {
        // NettyExchangeData  msg
        if(msg.isByPass()){// 这些信息本来就应该有服务器处理
            logger.info("经由服务器端接收数据成功: 来自" + msg.getHostName() + " 请求类型=" + msg.getNettyType().name());
            ArrayList<NettyExchangeData> exDates = ClusterServer.getInstance().answer(msg);
            if(exDates!=null && exDates.size() > 0) {
                for(NettyExchangeData exDate : exDates) {
                    if (exDate != null) {
                        logger.info("经由服务器端回答数据成功 " + exDate.getNettyType().name());
                        ctx.writeAndFlush(exDate.encode());
                    }
                }
            }
            return ;
        }

        if (dataBuffer != null) {
            dataBuffer.push(msg);
        } else {
            in(msg);
        }
    }

    public void setBufferSize(int bufferSize) {
        if (bufferSize <= 0) {
            dataBuffer = null;
            return;
        }
        dataBuffer = new ObjBufferedIO();
        try {
            dataBuffer.setReader(this, bufferSize);
        } catch (InterruptedException e) {
            logger.error(e);

            dataBuffer.close();
            dataBuffer = null;
        }
    }

    @Override
    public long in(Object obj) {
        ClusterClients.getInstance().callBack((NettyExchangeData) obj);
        return 0;
    }

    @Override
    public long in(long dataID, Object obj) {
        return 0;
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info("Client Error!");
        if (!(cause instanceof java.io.IOException)) {
            logger.error(cause);
        }
        ctx.fireExceptionCaught(cause);
    }
}
