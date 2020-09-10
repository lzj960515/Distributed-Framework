package com.my.rabbit.fonout;

import com.my.rabbit.MyConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 扇形交换机-生产者
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class FanoutProducer {
    /**
     * 扇形交换机routingKey无效
     */
    static String ROUTING_KEY = "everything";
    static String EXCHANGE_NAME = "my-fanout";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();
        String message = "Hello fanout!";

        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY,null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
        channel.close();
        connection.close();
    }
}
