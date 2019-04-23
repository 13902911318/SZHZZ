package szhzz.Netty.Cluster.Net;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import szhzz.Netty.Cluster.BusinessRuse;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Utils.DawLogger;

import java.util.List;

/**
 * Created by Administrator on 2016/5/4.
 */
public class ExchangeDataDecoder extends MessageToMessageDecoder<String> {
    private static DawLogger logger = DawLogger.getLogger(ExchangeDataDecoder.class);
    private NettyExchangeData data = null;

    @Override
    protected final void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        if ("bye".equals(msg)) {
            ctx.writeAndFlush("bye\r\n").addListener(ChannelFutureListener.CLOSE);
            return;
        }
        if (msg.startsWith("TRUCRIPT-KEY")) {
            String returnKey = BusinessRuse.getInstance().getPassword(msg);
            if(returnKey != null && ctx.channel().isWritable()){
                ctx.channel().writeAndFlush(returnKey + "\r\n");
            }
            return;
        }

//        System.out.println(msg);

        if (NettyExchangeData.isBeggingOfData(msg)) {
            if (data != null) {
//                System.out.println("数据序列错误");
//                logger.info("ID=" + data.getSerialNo(), new Exception("数据序列错误!") );
                logger.error(msg + " 数据序列错误 ID=" + data.getSerialNo() + data.encode());
            }
            data = new NettyExchangeData();
        } else if (NettyExchangeData.isEndOfDate(msg)) {
            if (data != null) {
                if (data.isSameCharset()) {
                    out.add(data);
                } else {
                    logger.error(data.toString(), new Exception("中文编码错误!"));
                }
            } else {
                logger.error(msg + " 数据序列错误 ID=" + data.getSerialNo());
            }
            data = null;
        } else {
            if (data != null) {
                try {
                    data.decode(msg);
                } catch (Exception ignored) {

                }
            } else {
                logger.error(" 正文 数据序列错误 ");
                logger.info("[" + msg + "]");
            }
        }

    }
}
