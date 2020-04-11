package szhzz.Netty;

/**
 * Created by Administrator on 2015/1/18.
 */

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import szhzz.Netty.Cluster.Net.ClientHandler;
import szhzz.Utils.DawLogger;

public class TimeClientHandler extends ChannelInboundHandlerAdapter {
    private static DawLogger logger = DawLogger.getLogger(TimeClientHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        UnixTime m = (UnixTime) msg; // (1)

        System.out.println(m);
        ctx.close();


//        try {
//            long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
//            System.out.println(new Date(currentTimeMillis));
//            ctx.close();
//        } finally {
//            m.release();
//        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.info("exceptionCaught!");
        if (!(cause instanceof java.io.IOException)) {
            if(!cause.getMessage().contains("远程主机强迫关闭了一个现有的连接")){
                logger.error(cause);
            }
        }
        ctx.close();
    }
}