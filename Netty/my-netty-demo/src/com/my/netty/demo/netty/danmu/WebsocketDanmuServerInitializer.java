package com.my.netty.demo.netty.danmu;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 服务端 ChannelInitializer
 */
public class WebsocketDanmuServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("http-decodec", new HttpRequestDecoder());
        pipeline.addLast("http-encodec", new HttpResponseEncoder());
        pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
        //自定义handler
        pipeline.addLast("http-request", new HttpRequestHandler("/ws"));
        pipeline.addLast("WebSocket-protocol", new WebSocketServerProtocolHandler("/ws"));
        //自定义handler
        pipeline.addLast("WebSocket-request", new TextWebSocketFrameHandler());
    }
}
