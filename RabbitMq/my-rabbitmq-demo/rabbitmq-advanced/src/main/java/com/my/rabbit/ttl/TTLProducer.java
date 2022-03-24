package com.my.rabbit.ttl;

import com.my.rabbit.MyConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 延时队列-消费者
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class TTLProducer {

    static String QUEUE_NAME = "hello-ttl";
    static String EXCHANGE_NAME = "my-ttl";
    static String EXCHANGE_TYPE = "direct";
    static String ROUTING_KEY = "ttl-key";

    public static void main(String[] args) throws IOException, InterruptedException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();

        //声明队列
        Map<String,Object> queueArgs = new HashMap<>();
        //设置队列中的消息10s没有被消费就会过期
        queueArgs.put("x-message-ttl",10000);
        //队列的长度
        queueArgs.put("x-max-length",4);
        channel.queueDeclare(QUEUE_NAME, true, false, false, queueArgs);
        //绑定队列
        channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        String message = "hello ttl";
        channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY,null,(message).getBytes());

//        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
//        QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
//
//        channel.basicConsume(QUEUE_NAME, true, queueingConsumer);
//
//        while (true) {
//            QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
//            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//            System.out.println(" [x] Received '" + message + "'");
//        }
    }
}
