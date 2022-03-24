package com.my.rabbit.fonout;

import com.my.rabbit.MyConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 扇形交换机-消费者
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class FanoutConsumer2 {

    static String QUEUE_NAME = "hello-fanout";
    static String EXCHANGE_NAME = "my-fanout";
    static String EXCHANGE_TYPE = "fanout";
    /**
     * 扇形交换机routingKey无效
     */
    static String ROUTING_KEY = "fanout-key";
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
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" hello-fanout [x] Received '" + message + "'");
        }
    }
}
