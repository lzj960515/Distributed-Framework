package com.my.rabbit.quickstart;

import com.my.rabbit.MyConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 生产者
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class QuickStartProducer {

    static String QUEUE_NAME = "hello2";

    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();
        String message = "Hello World!";
        for (int i = 0; i < 5; i++) {
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        }
        System.out.println(" [x] Sent '" + message + "'");
        channel.close();
        connection.close();
    }
}
