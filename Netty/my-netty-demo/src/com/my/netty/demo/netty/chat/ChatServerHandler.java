package com.my.netty.demo.netty.chat;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天室服务端处理器
 *
 * @author Zijian Liao
 * @since 1.0
 */
public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static Map<String, String> userMap = new ConcurrentHashMap<>();
    private static Map<String, Channel> channelMap = new ConcurrentHashMap<>();


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
//        String message = "[客户端]" + channel.remoteAddress() + " 上线了";
//        System.out.println(message);
//        channelGroup.writeAndFlush(message);
        channelGroup.add(channel);
    }

    private void userLogin(String username, Channel channel) {
        String address = channel.remoteAddress().toString();
        userMap.put(address, username);
        channelMap.put(username, channel);
        channelGroup.writeAndFlush(username + " 进入了聊天室");
    }

    private void sendMessageToAll(String message, Channel channel) {
        String address = channel.remoteAddress().toString();
        String username = userMap.get(address);
        channelGroup.writeAndFlush(username + ":" + message);
    }

    private void sendMessageToOne(String username, String message, Channel channel) {
        String fromUser = userMap.get(channel.remoteAddress().toString());
        Channel toChannel = channelMap.get(username);
        String finalMessage = "用户" + fromUser + "向您发送了一条消息：" + message;
        toChannel.writeAndFlush(finalMessage);
        channel.writeAndFlush("您向" + username + "发送了一条消息：" + message);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String messageJson) throws Exception {
        Channel channel = ctx.channel();
        WrapMessage wrapMessage = JSON.parseObject(messageJson, WrapMessage.class);
        System.out.println("接收到服务端消息:" + wrapMessage);
        int type = wrapMessage.getType();
        switch (type) {
            case 1:
                userLogin(wrapMessage.getUsername(), channel);
                break;
            case 2:
                sendMessageToAll(wrapMessage.getMessage(), channel);
                break;
            case 3:
                sendMessageToOne(wrapMessage.getUsername(), wrapMessage.getMessage(), channel);
                break;
        }
//
//        String clientMessage = "[客户端]" + channel.remoteAddress() + " ：" + message;
//        channelGroup.forEach(ch -> {
//            if (ch != channel) {
//                ch.writeAndFlush(clientMessage);
//            } else {
//                channel.writeAndFlush("[自己]：" + message);
//            }
//        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String address = channel.remoteAddress().toString();
        String username = userMap.get(address);
        String message = username + " 下线了";
        userMap.remove(address);
        channelMap.remove(username);
        channelGroup.writeAndFlush(message);
        System.out.println("channelGroup size=" + channelGroup.size());
    }
}
