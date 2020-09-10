package com.my.rabbit.direct;

import com.my.rabbit.MyConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 直接交换机-生产者
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class DirectProducer {
    /**
     * routingKey必须与消费者中声明的相同，否则消息无法正确发送
     */
    static String ROUTING_KEY = "direct-key";
    static String EXCHANGE_NAME = "my-direct";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();
        String message = "Hello Direct!";

        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY,null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
        channel.close();
        connection.close();
    }
}
