package szhzz.Netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import szhzz.Utils.DawLogger;

/**
 * Created by Administrator on 2015/1/18.
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {
    private static DawLogger logger = DawLogger.getLogger(TimeServerHandler.class);
    @Override
    public void channelActive(final ChannelHandlerContext ctx) { // (1)
        final ChannelFuture f = ctx.writeAndFlush(new UnixTime());
        f.addListener(ChannelFutureListener.CLOSE);


//        final ByteBuf time = ctx.alloc().buffer(4); // (2)
//        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
//
//        final ChannelFuture f = ctx.writeAndFlush(time); // (3)
//        f.addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) {
//                assert f == future;
//                ctx.close();
//            }
//        }); // (4)
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
