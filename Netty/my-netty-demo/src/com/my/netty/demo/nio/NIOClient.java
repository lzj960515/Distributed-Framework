package com.my.netty.demo.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * nio client demo
 *
 * @author Zijian Liao
 * @since 1.0
 */
public class NIOClient {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        //此时客户端注册到selector,并且是对connect感兴趣
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        // 客户端连接服务器,其实方法执行并没有实现连接，需要调用channel.finishConnect()才能完成连接
        socketChannel.connect(new InetSocketAddress("127.0.0.1",8080));
        while (true){
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if(selectionKey.isConnectable()){
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    // 如果正在连接，则完成连接
                    if (channel.isConnectionPending()) {
                        channel.finishConnect();
                    }
                    ByteBuffer bufferToWrite = ByteBuffer.wrap("Hello Server".getBytes());
                    channel.write(bufferToWrite);
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                }else if(selectionKey.isReadable()){
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    int len = channel.read(byteBuffer);
                    if(len != -1)
                        System.out.println("接收到服务端的数据：" + new String(byteBuffer.array(), 0, len));
                }
            }
        }
    }
}
