package com.my.rabbit.topic;

import com.my.rabbit.MyConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Topic交换机-消费者
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class TopicConsumer {
    
    static String QUEUE_NAME = "hello-topic";
    static String EXCHANGE_NAME = "my-topic";
    static String EXCHANGE_TYPE = "topic";
    /**
     *  * :匹配一个单词 如 topic-key.* 可以匹配 topic-key.1 topic-key.a topic-key.a-b，但不能匹配 topic-key.a.b
     *  # :匹配多个单词 如 topic-key.# 可以匹配 topic-key.1 也可以匹配 topic-key.a.b topic-key.a.b.c
     *  也可以这样玩,放到前面 #.key 匹配 1.key 2.key a.b.key 或者放到中间
     */
    static String ROUTING_KEY = "topic-key.*";
    public static void main(String[] args) throws IOException, InterruptedException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME,true, false, false, null);

        channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
        //如果有多个队列绑定了该交换机，则生产者发消息时，每个队列都会接到消息
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        QueueingConsumer queueingConsumer = new QueueingConsumer(channel);

        channel.basicConsume(QUEUE_NAME,true,queueingConsumer);

        while (true){
            QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        }
    }
}
