package com.my.rabbit.confirm;

import com.my.rabbit.MyConnectionFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 生产者-confirm模式
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class ConfirmProducer {

    static String ROUTING_KEY = "topic-key.a";
    static String EXCHANGE_NAME = "my-topic";
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();
        //开启确认模式
        channel.confirmSelect();
        //添加监听器
        channel.addConfirmListener(new MyConfirmListener());
        String message = "Hello topic!";
        /**
         * deliveryMode 1.不持久化 2.持久化  若队列没有持久化，消息持久化无意义，重启队列都没了
         * expiration 过期时间 单位毫秒 使用该参数后则持久化无效
         * contentEncoding 消息内容编码
         * messageId 消息id
         * correlationId 关联id
         * headers 消息头
         */
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
                .deliveryMode(2)
                .expiration("5000")
                .contentEncoding("UTF-8")
                .messageId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .build();
        while (true){
            TimeUnit.SECONDS.sleep(1);
            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY,basicProperties, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }

//        channel.close();
//        connection.close();
    }
}
