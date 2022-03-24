package com.my.rabbit.nack;

import com.my.rabbit.MyConnectionFactory;
import com.my.rabbit.returnlistener.MyReturnListener;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 生产者-nack
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class NAckProducer {
    static String ROUTING_KEY = "dead-key";
//    static String EXCHANGE_NAME = "my-topic";
    static String EXCHANGE_NAME = "dead-topic-ex";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();

        for (int i = 0; i < 100; i++) {
            Map<String,Object> header = new HashMap<>(2);
            header.put("num", i+ "");
            AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                    .headers(header)
                    .build();

            String message = "Hello topic! i = "+ i;
            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, true, properties, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }
        channel.close();
        connection.close();
    }
}
