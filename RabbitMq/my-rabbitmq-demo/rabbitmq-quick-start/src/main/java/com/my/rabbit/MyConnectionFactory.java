package com.my.rabbit;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 连接工厂
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class MyConnectionFactory {

    public static Connection connection(){
        //username 和 password 可在管理端自行创建
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("8.129.54.157");
        factory.setPort(5672);
        factory.setUsername("liaozijian");
        factory.setPassword("lzj123456");
        try {
            return factory.newConnection();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            throw new RuntimeException("创建连接失败: "+e.getMessage());
        }
    }
}
