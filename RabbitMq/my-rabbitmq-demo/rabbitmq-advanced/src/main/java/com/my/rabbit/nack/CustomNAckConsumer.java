package com.my.rabbit.nack;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义消费者-nack
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class CustomNAckConsumer extends DefaultConsumer {

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    public CustomNAckConsumer(Channel channel) {
        super(channel);
    }


    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Map<String, Object> headers = properties.getHeaders();
        if ("3".equals(headers.get("num").toString())) {
            System.out.println("body=" + new String(body));
            System.err.println("nack i  = " + headers.get("num"));
            //requeue：重回队列，一般不这样做，否则将出现死循环 消息消费失败-> 重回队列 -> 消费消息 -> 消息消费失败-> 重回队列 -> ...
            getChannel().basicNack(envelope.getDeliveryTag(), false, true);
        } else {
            System.out.println("body=" + new String(body));
            System.out.println("ack i  = " + headers.get("num"));
            //multiple，true: 签收当前消息与之前的所有消息，false: 只签收当前消息
            getChannel().basicAck(envelope.getDeliveryTag(), false);
        }


    }
}
