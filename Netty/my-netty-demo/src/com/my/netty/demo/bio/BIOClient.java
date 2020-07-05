package com.my.netty.demo.bio;

import java.io.IOException;
import java.net.Socket;

/**
 * BIO client demo
 *
 * @author Zijian Liao
 * @since 1.0
 */
public class BIOClient {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 8080);
        System.out.println("准备向服务端发送信息....");
        socket.getOutputStream().write("Hello Server".getBytes());
        socket.getOutputStream().flush();
        System.out.println("信息发送完毕");
        byte[] bytes = new byte[1024];
        //此处阻塞
        int len = socket.getInputStream().read(bytes);
        if (len != -1)
            System.out.println("接收到服务端返回的信息：" + new String(bytes, 0, len));
        socket.close();
        System.out.println("连接结束");
    }
}
