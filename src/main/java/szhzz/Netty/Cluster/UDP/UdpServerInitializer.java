package szhzz.Netty.Cluster.UDP;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import szhzz.Netty.Cluster.Net.ClientHandler;
import szhzz.Netty.Cluster.Net.ExchangeDataDecoder;


/**
 * Created by Administrator on 2015/2/15.
 */
public class UdpServerInitializer extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

//        pipeline.addLast("framer0", new UdpStringDecoder());
//        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
//        pipeline.addLast("decoder", new StringDecoder(Charset.forName("UTF-8")));
//        pipeline.addLast("decoder2", new ExchangeDataDecoder());

        pipeline.addLast("framer", new UdpStringDecoder());
        pipeline.addLast("decoder2", new ExchangeDataDecoder());
        pipeline.addLast("handler", new ClientHandler());
    }
}
