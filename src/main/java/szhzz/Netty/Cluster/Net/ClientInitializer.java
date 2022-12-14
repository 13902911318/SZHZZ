package szhzz.Netty.Cluster.Net;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;


/**
 * Created by Administrator on 2015/2/15.
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    private static ClientInitializer onlyOne = null;

    public static ClientInitializer getInstance() {
        if(onlyOne == null) return new ClientInitializer();
        return onlyOne;
    }

    public static void setOnlyOne(ClientInitializer onlyOne) {
        ClientInitializer.onlyOne = onlyOne;
    }

    private ClientInitializer(){}
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
//        pipeline.addLast("zDecode", new JdkZlibDecoder());
//        pipeline.addLast("zEncode", new JdkZlibEncoder());

//        pipeline.addLast("deflater", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.ZLIB));
//        pipeline.addLast("inflater", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.ZLIB));

        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(2* 1024 * 1024, Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", new StringDecoder(Charset.forName("UTF-8"))); //Charset.forName("UTF-8")
        pipeline.addLast("decoder2", new ExchangeDataDecoder());


        pipeline.addLast("encoder", new StringEncoder(Charset.forName("UTF-8")));
        pipeline.addLast("handler", new ClientHandler());
    }
}
