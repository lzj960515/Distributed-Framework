package com.my.rabbit.dead;

import com.my.rabbit.MyConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 消费掉死信队列的消息
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class DeadQueueConsumer {
    static String dlxExchangeName = "tuling.dlx.exchange";
    static String dlxQueueName = "tuling.dlx.queue";
    static String exchangeType = "topic";
    public static void main(String[] args) throws IOException, InterruptedException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(dlxExchangeName,exchangeType,true,false,null);
        channel.queueDeclare(dlxQueueName,true,false,false,null);
        channel.queueBind(dlxQueueName,dlxExchangeName,"#");

        QueueingConsumer queueingConsumer = new QueueingConsumer(channel);

        channel.basicConsume(dlxQueueName, true, queueingConsumer);
        while (true) {
            QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        }
    }
}
