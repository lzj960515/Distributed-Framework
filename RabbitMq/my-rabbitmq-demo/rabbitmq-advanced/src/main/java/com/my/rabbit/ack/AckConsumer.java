package com.my.rabbit.ack;

import com.my.rabbit.MyConnectionFactory;
import com.my.rabbit.custom.CustomConsumer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;

/**
 * 消费者
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class AckConsumer {

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
        /**
         * prefetchSize：消息大小(rabbitmq还没有该功能，所以一般是填写0表示不限制)
         * prefetchCount：消息的阈值，每次过来几条消息(一般是填写1 一条 一条的处理消息)
         * global: true表示是channel级别的限制,false表示consumer级别的限制(channel的限制rabbitmq 还没有该功能)
         */
        channel.basicQos(0, 5, false);
        //autoAck设为false
        channel.basicConsume(QUEUE_NAME, false, new CustomAckConsumer(channel));

    }
}
