package com.my.netty.demo.netty.chat;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.Scanner;

/**
 * 聊天室客户端
 *
 * @author Zijian Liao
 * @since 1.0
 */
public class ChatClient {

    public static void main(String[] args) throws Exception {
        //客户端需要一个事件循环组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            //创建客户端启动对象
            //注意客户端使用的不是 ServerBootstrap 而是 Bootstrap
            Bootstrap bootstrap = new Bootstrap();
            //设置相关参数
            bootstrap.group(group) //设置线程组
                    .channel(NioSocketChannel.class) // 使用 NioSocketChannel 作为客户端的通道实现
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new StringDecoder());

                            pipeline.addLast(new ChatClientHandler());
                        }
                    });
            System.out.println("netty client start");
            //启动客户端去连接服务器端
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 9000).sync();

            //--------以下为业务逻辑代码
            Channel channel = channelFuture.channel();
            Scanner scanner = new Scanner(System.in);

            System.out.print("您的用户名为：");
            String username = scanner.nextLine();
            WrapMessage wrapMessage = new WrapMessage();
            wrapMessage.setType(1);
            wrapMessage.setUsername(username);
            ByteBuf buf = Unpooled.copiedBuffer(JSON.toJSONString(wrapMessage), CharsetUtil.UTF_8);
            channel.writeAndFlush(buf);
            System.err.println("按任意键开始发送消息，按[q]退出");
            String s = scanner.nextLine();
            if("q".equals(s)){
                return;
            }
            System.out.print("您的消息类型（2.群发消息 3.私聊）为：");
            while (scanner.hasNextLine()){
                String type = scanner.nextLine();
                int messageType = Integer.parseInt(type);
                wrapMessage = new WrapMessage();
                wrapMessage.setType(messageType);
                if(messageType == 2){
                    System.out.print("您想发送的消息：");
                }else if(messageType == 3){
                    System.out.print("您想发送的用户：");
                    username = scanner.nextLine();
                    wrapMessage.setUsername(username);
                    System.out.print("您想发送的消息：");
                }
                String message = scanner.nextLine();
                wrapMessage.setMessage(message);
                buf = Unpooled.copiedBuffer(JSON.toJSONString(wrapMessage), CharsetUtil.UTF_8);
                channel.writeAndFlush(buf);
                s = scanner.nextLine();
                if("q".equals(s)){
                    return;
                }
                System.out.print("您的消息类型（1.注册 2.群发消息 3.私聊）为：");
            }

            //对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
