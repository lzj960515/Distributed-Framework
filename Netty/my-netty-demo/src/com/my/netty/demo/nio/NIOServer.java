package com.my.netty.demo.nio;

import com.my.netty.demo.netty.code.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * nio server demo
 *
 * @author Zijian Liao
 * @since 1.0
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {
        User user = new User();
        user.setUsername("lzj");
        user.setAge(19);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //配置为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        //创建一个多路复用器selector
        Selector selector = Selector.open();
        //将ServerSocketChannel注册到selector上,并且设置selector对客户端连接感兴趣
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT,user);
        System.out.println(serverSocketChannel.isRegistered());
        for(;;){
            System.out.println("等待事件发生....");
            //轮询selector的key,此处阻塞
            selector.select();
            System.out.println("有事件发生了");
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                Object attachment = selectionKey.attachment();
                System.out.println(attachment);
                //删除本次已处理的key，防止下次select重复处理
                iterator.remove();
                handle(selectionKey);
            }
        }
    }

    public static void handle(SelectionKey selectionKey) throws IOException {
        if(selectionKey.isAcceptable()){
            System.out.println("有客户端连接事件发生了");
            //由于此处为对accept感兴趣，只有ServerSocketChannel注册到selector上时是对accept感兴趣，
            //所以这里的channel是ServerSocketChannel
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            //NIO非阻塞体现：此处accept方法是阻塞的，但是这里因为是发生了连接事件，所以这个方法会马上执行完，不会阻塞
            SocketChannel socketChannel = serverSocketChannel.accept();//同BIO返回一个连接客户端的channel(socket)
            //将SocketChannel配置为非阻塞，并且对read事件感兴趣
            socketChannel.configureBlocking(false);
            socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
        }else if(selectionKey.isReadable()){
            System.out.println("有客户端数据可读事件发生了");
            //由于此处为对read感兴趣，只有SocketChannel注册到selector上时是对read感兴趣
            //所以这里的channel是SocketChannel
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            //以下为读取客户端发送的数据与向客户端发送数据操作
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            //NIO非阻塞体现:首先read方法不会阻塞，其次这种事件响应模型，当调用到read方法时肯定是发生了客户端发送数据的事件
            int len = channel.read(byteBuffer);
            if(len != -1)
                System.out.println("接收到客户端的数据：" + new String(byteBuffer.array(), 0, len));
            ByteBuffer bufferToWrite = ByteBuffer.wrap("Hello Client".getBytes());
            channel.write(bufferToWrite);
        }
    }
}
