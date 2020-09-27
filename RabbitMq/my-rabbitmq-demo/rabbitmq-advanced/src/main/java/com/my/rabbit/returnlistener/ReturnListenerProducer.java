package com.my.rabbit.returnlistener;

import com.my.rabbit.MyConnectionFactory;
import com.my.rabbit.confirm.MyConfirmListener;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 生产者
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class ReturnListenerProducer {
    static String ROUTING_KEY = "topic-key.a";
    static String EXCHANGE_NAME = "my-topic";
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Connection connection = MyConnectionFactory.connection();
        Channel channel = connection.createChannel();
        //添加退回监听器
        channel.addReturnListener(new MyReturnListener());
        String message = "Hello topic!";
        while (true){
            TimeUnit.SECONDS.sleep(1);
            //mandatory设置为true
            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, true, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }

//        channel.close();
//        connection.close();
    }
}
