package com.my.netty.demo.netty.code;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("MyClientHandler发送数据");
        //测试String编码
//        ctx.writeAndFlush("Hello Server");
        //测试自定义Long编码器
//        ctx.writeAndFlush(10000L);
        //测试Object编码
        ctx.writeAndFlush(new User("lzj",18));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到服务器消息:" + msg);
    }
}