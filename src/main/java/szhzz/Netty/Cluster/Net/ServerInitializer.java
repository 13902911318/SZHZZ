package szhzz.Netty.Cluster.Net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;


/**
 * Created by Administrator on 2015/2/15.
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

//        pipeline.addLast("zDecode", new JdkZlibDecoder());
//        pipeline.addLast("zEncode", new JdkZlibEncoder());

//        pipeline.addLast("deflater", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
//        pipeline.addLast("inflater", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));

//        pipeline.addLast("deflater", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.ZLIB));
//        pipeline.addLast("inflater", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.ZLIB));

        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(256 * 1024 , Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", new StringDecoder(Charset.forName("UTF-8")));
        pipeline.addLast("decoder2", new ExchangeDataDecoder());

//        pipeline.addLast("encode", new ObjectEncoder());
//        pipeline.addLast("decode", new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
//        pipeline.addLast("handler", new ClientHandler());

        pipeline.addLast("encoder", new StringEncoder(Charset.forName("UTF-8")));
        pipeline.addLast("handler", new ServerHandler());
    }
}
