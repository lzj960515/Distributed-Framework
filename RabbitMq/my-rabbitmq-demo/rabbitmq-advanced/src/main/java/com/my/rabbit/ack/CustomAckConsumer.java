package com.my.rabbit.ack;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义消费者-ack
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class CustomAckConsumer extends DefaultConsumer {

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    public CustomAckConsumer(Channel channel) {
        super(channel);
    }

    static AtomicInteger count = new AtomicInteger(0);

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        System.out.println("自定义的消息消费端----");
        System.out.println("consumerTag=" + consumerTag);
        System.out.println("envelope=" + envelope);
        System.out.println("properties=" + properties);
        System.out.println("body=" + new String(body));
        if(count.incrementAndGet() == 3){
            System.err.println("ack================");
            //multiple，true: 签收当前消息与之前的所有消息，false: 只签收当前消息
            getChannel().basicAck(envelope.getDeliveryTag(),false);
        }
    }
}
