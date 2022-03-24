package com.my.rabbit.message;

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
 * 自定义消息属性-生产者
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class MessageProducer {
    
    static String ROUTING_KEY = "message-key.a";
    static String EXCHANGE_NAME = "my-message";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();
        String message = "Hello topic!";
        Map<String, Object> headers = new HashMap<>(2,1);
        headers.put("name","jack");
        headers.put("age",20);
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
                .expiration("10000")
                .contentEncoding("UTF-8")
                .messageId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .headers(headers)
                .build();
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, basicProperties, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
        channel.close();
        connection.close();
    }
}
