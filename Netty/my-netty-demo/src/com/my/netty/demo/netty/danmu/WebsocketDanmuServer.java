package com.my.netty.demo.netty.danmu;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Websocket 聊天服务器-服务端
 */
public class WebsocketDanmuServer {

    private int port;

    public WebsocketDanmuServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new WebsocketDanmuServerInitializer())  //(4)
                    .option(ChannelOption.SO_BACKLOG, 10000)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
            System.out.println("弹幕系统 启动了" + port);

            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync(); // (7)
            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("弹幕系统 关闭了");
        }
    }

    public static void main(String[] args) throws Exception {
        new WebsocketDanmuServer(8080).run();
    }
}