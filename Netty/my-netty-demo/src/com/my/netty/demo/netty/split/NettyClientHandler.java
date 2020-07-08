package com.my.netty.demo.netty.split;

import com.my.netty.demo.netty.code.User;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("MyClientHandler发送数据");
        //测试String编码
        for (int i =0 ;i<100;i++){
            ctx.writeAndFlush("你好，我是张三");
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("收到服务器消息:" + msg);
    }
}