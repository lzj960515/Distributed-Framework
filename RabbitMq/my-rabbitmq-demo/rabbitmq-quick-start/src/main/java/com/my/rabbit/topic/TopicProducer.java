package com.my.rabbit.topic;

import com.my.rabbit.MyConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Topic交换机-生产者
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class TopicProducer {
    
    static String ROUTING_KEY = "topic-key.a";
    static String EXCHANGE_NAME = "my-topic";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();
        String message = "Hello topic!";

        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY,null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
        channel.close();
        connection.close();
    }
}
