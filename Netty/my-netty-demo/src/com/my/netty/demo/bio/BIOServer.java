package com.my.netty.demo.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * BIO server demo
 *
 * @author Zijian Liao
 * @since 1.0
 */
public class BIOServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        for (;;){
            System.out.println("等待客户端连接....");
            //此处阻塞
            Socket socket = serverSocket.accept();
            System.out.println("有客户端连接了");
            byte[] bytes = new byte[1024];
            //此处阻塞
            int len = socket.getInputStream().read(bytes);
            if (len != -1)
                System.out.println("接收到客户端的信息：" + new String(bytes, 0, len));
            System.out.println("向客户端发送一条信息...");
            socket.getOutputStream().write("Hello Client".getBytes());
            socket.getOutputStream().flush();
            System.out.println("信息已发送");
            socket.close();
        }
    }
}
