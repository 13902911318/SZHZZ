package szhzz.Netty.Cluster.Net;

import szhzz.App.AppManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import szhzz.DataBuffer.DataConsumer;
import szhzz.DataBuffer.ObjBufferedIO;
import szhzz.Utils.DawLogger;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/5/6.
 */
public class ExchangeDataEncoder extends MessageToMessageEncoder<CharSequence> implements DataConsumer {
    private static DawLogger logger = DawLogger.getLogger(ExchangeDataEncoder.class);

    ObjBufferedIO pushBuffer = null;
    private long inNo = 0;
    private long outNo = 0;
    boolean isClosed = false;
    Channel privateChannel = null;
    List<Object> out = null;

    void push(Object o) {
        inNo++;
        if (pushBuffer != null) {
            pushBuffer.push(o);
        } else {
            in(o);
        }
    }

    void stop() {
        isClosed = true;
        if(pushBuffer !=null) {
            pushBuffer.close();
        }
        pushBuffer = null;
    }

    private void active(Channel channel, List<Object> out) {
        if(privateChannel == null) {
            privateChannel = channel;
            this.out = out;
            privateChannel.closeFuture().addListeners(new GenericFutureListener() {
                @Override
                public void operationComplete(Future future) throws Exception {
                    stop();
                }
            });
            pushBuffer = new ObjBufferedIO();
            try {
                pushBuffer.setReader(this, 50);
            } catch (InterruptedException e) {
                logger.error(e);
                pushBuffer = null;
            }
        }
    }

    public long in(Object obj) {
        if (isClosed) return -1;
        if (!privateChannel.isActive()) {
            stop();
            return -1;
        }

        if (!privateChannel.isWritable()) {
            if(!isClosed){
                AppManager.logit("[Proxy]" + privateChannel.remoteAddress().toString() + " 推送故障! " + outNo + "/" + inNo);
            }
        } else {
            outNo++;
            try {
                //阻塞式写入
                ChannelFuture future = privateChannel.writeAndFlush(obj.toString());
                future.await(1, TimeUnit.SECONDS);
                if(!future.isSuccess()){
                    logger.error("[Proxy]" + privateChannel.remoteAddress().toString() + " 推送故障! " + outNo + "/" + inNo, future.cause());
                }
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
        return 0;
    }

    protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out) throws Exception {
        if(privateChannel == null){
            active(ctx.channel(), out);
        }

        if(msg != null) {
            push(msg);
        }
    }


    @Override
    public long in(long dataID, Object obj) {
        return 0;
    }

}
