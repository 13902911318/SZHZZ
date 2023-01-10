package szhzz.Netty.Cluster.Net;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import szhzz.Netty.Cluster.BusinessRuse;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Utils.DawLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/4.
 */
public class ExchangeDataDecoder extends MessageToMessageDecoder<String> {
    private static DawLogger logger = DawLogger.getLogger(ExchangeDataDecoder.class);
    protected NettyExchangeData data = null;

    @Override
    protected final void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        decode_(ctx, msg, out);
    }

    boolean isEmpty(NettyExchangeData node) {
        return node == null || node.isEmpty();
    }

    protected void decode_(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        // System.out.println(msg);
        if ("bye".equals(msg)) {
            ctx.writeAndFlush("bye\r\n").addListener(ChannelFutureListener.CLOSE);
            return;
        }
        if (msg.startsWith("TRUCRIPT-KEY")) {
            String returnKey = BusinessRuse.getInstance().getPassword(msg);
            if (returnKey != null && ctx.channel().isWritable()) {
                ctx.channel().writeAndFlush(returnKey + "\r\n");
            }
            return;
        }

//        System.out.println(msg);

        if (NettyExchangeData.isBeggingOfData(msg)) {
            if (!isEmpty(data)) {
//                System.out.println("数据序列错误");
//                logger.info("ID=" + data.getSerialNo(), new Exception("数据序列错误!") );
                if (data.isSameCharset()) {
                    data.setErrorCode(10240);
                    data.setMessage("错误的信息头,尝试恢复");
                    out.add(data);
                    logger.info(msg + "<-错误的信息头 ID=" + data.getSerialNo() + "\n" + data.toString());
                } else {
                    logger.error(data.toString(), new Exception("中文编码错误!"));
                }
            }
            data = new NettyExchangeData();
        } else if (NettyExchangeData.isEndOfDate(msg)) {
            if (!isEmpty(data)) {
                if (data.isSameCharset()) {
                    out.add(data);
                } else {
                    logger.error(data.toString(), new Exception("中文编码错误!"));
                }
            } else {
                logger.error(" 错误的信息结尾 " + msg);
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

    public static void main(String[] args) {
        String s = "\tk\n"  ;
        s = s.replaceAll("\t|\n", "");
        System.out.println(s);
    }
}
