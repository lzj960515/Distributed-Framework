package com.my.rabbit.custom;

import com.my.rabbit.MyConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 消费者
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class Consumer {

    static String QUEUE_NAME = "hello-topic";
    static String EXCHANGE_NAME = "my-topic";
    static String EXCHANGE_TYPE = "topic";
    static String ROUTING_KEY = "topic-key.*";

    public static void main(String[] args) throws IOException, InterruptedException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        channel.basicConsume(QUEUE_NAME, true, new CustomConsumer(channel));

    }
}
