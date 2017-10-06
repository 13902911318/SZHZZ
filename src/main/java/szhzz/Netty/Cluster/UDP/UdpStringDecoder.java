package szhzz.Netty.Cluster.UDP;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Utils.DawLogger;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by Administrator on 2017/3/29.
 */
public class UdpStringDecoder extends MessageToMessageDecoder<DatagramPacket> {
    private static DawLogger logger = DawLogger.getLogger(UdpStringDecoder.class);
    byte RETURN = (byte) '\r';
    byte newLine = (byte) '\n';
    int count = 0;
    private NettyExchangeData data = null;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> out) throws Exception {
        ByteBuf data = datagramPacket.content();

//        String s = data.slice(0, data.readableBytes()).toString(CharsetUtil.UTF_8);
//        System.out.println(s);
//        System.out.println("=================================================");

        String line;
        int start = 0;
        int length = data.indexOf(start, data.readableBytes(), RETURN);
        int len = data.readableBytes();

        if (length > 0) {
            // \r\n 格式
            while (length > 0) {
                while (data.getByte(start) == newLine) {
                    start++;
                    length--;
                    if (length <= 0) {
                        int a = 0;
                    }
                }

//                System.out.println("len=" + len + " start=" + start + " length=" + length);
                line = data.slice(start, length).toString(Charset.forName("UTF-8"));
//                System.out.println(line);
                out.add(line);
//                decode_(channelHandlerContext, line, out);

                start += length;
                start++;
                length = data.indexOf(start, len, RETURN) - start;
            }

            if (start < data.readableBytes()) {
                if (data.getByte(start) == newLine) {
                    start++;
                }
                if (start < data.readableBytes()) {
                    line = data.slice(start, data.readableBytes() - start).toString(Charset.forName("UTF-8"));
                    out.add(line);
//                    decode_(channelHandlerContext, line, out);
                }
            }
        } else {
            // \n
            length = data.indexOf(start, data.readableBytes(), newLine);
            while (length >= 0) {

                line = data.slice(start, length).toString(Charset.forName("UTF-8"));
                out.add(line);
//                decode_(channelHandlerContext, line, out);

                start += length;
                start++;
                int index = data.indexOf(start, data.readableBytes(), newLine);
                length = index - start;
            }

            if (start < data.readableBytes()) {
                if (start < data.readableBytes()) {
                    line = data.slice(start, data.readableBytes() - start).toString(Charset.forName("UTF-8"));
                    out.add(line);
//                    decode_(channelHandlerContext, line, out);
                }
            }
        }

        //========================
//
//        if(++count > 10000){
//            AppManager.logit("UDP data +10000");
//            count = 0;
//        }
    }


    protected void decode_(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        if ("bye".equals(msg)) {
            ctx.writeAndFlush("bye\r\n").addListener(ChannelFutureListener.CLOSE);
            return;
        }

        //System.out.println(msg);

        if (NettyExchangeData.isBeggingOfData(msg)) {
            if (data != null) {
                System.out.println("数据序列错误");
                logger.error(new Exception("数据序列错误!"));
            }
            data = new NettyExchangeData();
        } else if (NettyExchangeData.isEndOfDate(msg)) {
            if (data != null) {
                if (data.isSameCharset()) {
                    out.add(data);
                } else {
                    logger.error(msg, new Exception("中文编码错误!"));
                }
            } else {
                logger.error(new Exception("数据序列错误!"));
            }
            data = null;
        } else {
            if (data != null) {
                data.decode(msg);
            } else {
                logger.error(new Exception("数据序列错误!"));
            }
        }
    }
}

