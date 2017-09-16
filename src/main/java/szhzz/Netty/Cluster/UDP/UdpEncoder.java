package szhzz.Netty.Cluster.UDP;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by Administrator on 2017/3/29.
 */
public class UdpEncoder extends MessageToMessageEncoder<NettyExchangeData> {
    private final InetSocketAddress remoteAddress;

    public UdpEncoder(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext,
                          NettyExchangeData logEvent, List<Object> out)
            throws Exception {
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(logEvent.encode().getBytes(CharsetUtil.UTF_8));
        out.add(new DatagramPacket(buf, remoteAddress));
    }
}