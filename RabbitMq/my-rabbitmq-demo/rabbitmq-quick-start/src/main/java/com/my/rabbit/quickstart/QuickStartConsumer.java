package com.my.rabbit.quickstart;

import com.my.rabbit.MyConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import java.nio.charset.StandardCharsets;

/**
 * 消费者
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class QuickStartConsumer {

    private final static String QUEUE_NAME = "hello2";

    public static void main(String[] argv) throws Exception {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();
        /**
         * durable:是否持久化，rabbitmq的队列是放在内存当中的，重启之后队列将会丢失，为true则会将队列持久化到磁盘上
         *      注意：该参数只是判断队列持久化与否，与消息无关，虽然队列持久化了，但消息是不会持久化的，若队列中存在消息，重启之后消息会丢失
         *      若想消息持久化，可以使用自定义消息，见Message的demo
         * exclusive: 是否独占，为true表示该队列只能有一个消费者消费，若有第二个消费者再次声明，将会抛出异常
         *      为false时可以有多个消费者一起消费，每条消息只能由一个消费者消费，默认是轮询的方式，一个消费者一条
         *      注意：开启此参数后(true)，消费者断开队列将自动删除，durable的效果被无效
         *
         * autoDelete:是否自动删除，为true表示当最后一个消费者连接断开之后自动删除，durable的效果被无效
         */
        channel.queueDeclare(QUEUE_NAME, true, true, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
        channel.basicConsume(QUEUE_NAME,true,queueingConsumer);
        while (true) {
            QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        }
//        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//            String message = new String(delivery.getBody(), "UTF-8");
//            System.out.println(" [x] Received '" + message + "'");
//        };
//        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}
