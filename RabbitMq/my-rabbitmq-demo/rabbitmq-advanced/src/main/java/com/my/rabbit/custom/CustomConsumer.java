package com.my.rabbit.custom;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

/**
 * 消费者-自定义
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class CustomConsumer extends DefaultConsumer {
    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    public CustomConsumer(Channel channel) {
        super(channel);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        System.out.println("自定义的消息消费端----");
        System.out.println("consumerTag=" + consumerTag);
        System.out.println("envelope=" + envelope);
        System.out.println("properties=" + properties);
        System.out.println("body=" + new String(body));
    }
}
