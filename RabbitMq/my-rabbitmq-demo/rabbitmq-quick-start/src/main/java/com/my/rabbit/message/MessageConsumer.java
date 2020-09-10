package com.my.rabbit.message;

import com.my.rabbit.MyConnectionFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 自定义消息属性-消费者， 这里直接把topic的demo拿过来
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class MessageConsumer {

    static String QUEUE_NAME = "hello-message1";
    static String EXCHANGE_NAME = "my-message";
    static String EXCHANGE_TYPE = "topic";
    /**
     *  * :匹配一个单词 如 topic-key.* 可以匹配 topic-key.1 topic-key.a topic-key.a-b，但不能匹配 topic-key.a.b
     *  # :匹配多个单词 如 topic-key.# 可以匹配 topic-key.1 也可以匹配 topic-key.a.b topic-key.a.b.c
     *  也可以这样玩,放到前面 #.key 匹配 1.key 2.key a.b.key 或者放到中间
     */
    static String ROUTING_KEY = "message-key.*";
    public static void main(String[] args) throws IOException, InterruptedException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME,true, false, false, null);

        channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);

        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        QueueingConsumer queueingConsumer = new QueueingConsumer(channel);

        channel.basicConsume(QUEUE_NAME,true,queueingConsumer);

        while (true){
            QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
            AMQP.BasicProperties properties = delivery.getProperties();
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
            System.out.println("properties: " + properties.toString());
        }
    }
}
