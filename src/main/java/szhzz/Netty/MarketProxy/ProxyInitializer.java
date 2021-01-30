package szhzz.Netty.MarketProxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import szhzz.Netty.Cluster.Net.ExchangeDataDecoder;

import java.nio.charset.Charset;


/**
 * Created by Administrator on 2015/2/15.
 */
public class ProxyInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        //使用 zip 后无法与C++ 程序兼容

//        pipeline.addLast("zDecode", new JdkZlibDecoder());
//        pipeline.addLast("zEncode", new JdkZlibEncoder());

/// 不使用
        //pipeline.addBefore("framer", "deflater", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.ZLIB));

//        pipeline.addLast("deflater", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.ZLIB));
//        pipeline.addLast("inflater", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.ZLIB));


        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(2 * 1024 * 1024, Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", new StringDecoder(Charset.forName("UTF-8")));  //Charset.forName("UTF-8")
        pipeline.addLast("decoder2", new ExchangeDataDecoder());


        pipeline.addLast("encoder", new StringEncoder(Charset.forName("UTF-8")));
        pipeline.addLast("handler", ProxyHandler.getInstance());
    }
}
