package com.my.rabbit.dead;

import com.my.rabbit.MyConnectionFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * 生产者 死信队列
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class DeadProducer {
    //声明正常的队列
    static String NORMAL_EXCHANGE_NAME = "my.normaldlx.exchange";
    static String ROUTING_KEY = "my.dlx.key";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();
        //消息十秒没有被消费，那么就会转到死信队列上
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties().builder()
                .deliveryMode(2)
                .expiration("10000")
                .build();
        String message = "Hello DEAD QUEUE! ";
        for (int i = 0; i < 100; i++) {
            channel.basicPublish(NORMAL_EXCHANGE_NAME, ROUTING_KEY, basicProperties, (message + i).getBytes());
            System.out.println(" [x] Sent '" + message + i + "'");
        }
//        channel.close();
//        connection.close();
    }
}
