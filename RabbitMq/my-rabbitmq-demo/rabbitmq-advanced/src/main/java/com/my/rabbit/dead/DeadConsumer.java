package com.my.rabbit.dead;

import com.my.rabbit.MyConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 消费者 死信队列
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class DeadConsumer {

    /**
     * 声明正常的队列
     */
    static String NORMAL_EXCHANGE_NAME = "my.normaldlx.exchange";
    static String EXCHANGE_TYPE = "topic";
    static String NORMAL_QUEUE_NAME = "my.normaldlx.queue";
    static String ROUTING_KEY = "my.dlx.#";

    /**
     * 声明死信队列
     */
    static String DLX_EXCHANGE_NAME = "my.dlx.exchange";
    static String DLX_QUEUE_NAME = "my.dlx.queue";

    public static void main(String[] args) throws IOException, InterruptedException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();

        //声明正常交换机
        channel.exchangeDeclare(NORMAL_EXCHANGE_NAME, EXCHANGE_TYPE, true, false, null);
        Map<String, Object> queueArgs = new HashMap<>();
        //正常队列上绑定死信队列
        queueArgs.put("x-dead-letter-exchange", DLX_EXCHANGE_NAME);
        //消息数量大于4时，将之前的消息转移到死信队列
//        queueArgs.put("x-max-length", 4);
        channel.queueDeclare(NORMAL_QUEUE_NAME, true, false, false, queueArgs);
        channel.queueBind(NORMAL_QUEUE_NAME, NORMAL_EXCHANGE_NAME, ROUTING_KEY);

        //声明死信队列
        channel.exchangeDeclare(DLX_EXCHANGE_NAME, EXCHANGE_TYPE, true, false, null);
        channel.queueDeclare(DLX_QUEUE_NAME, true, false, false, null);
        channel.queueBind(DLX_QUEUE_NAME, DLX_EXCHANGE_NAME, "#");
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        //autoAck设为false
        channel.basicConsume(NORMAL_QUEUE_NAME, false, new CustomDeadConsumer(channel));
    }
}
