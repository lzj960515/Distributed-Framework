package com.my.rabbitmq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.rabbitmq.constant.RabbitConstant;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 消费者
 *
 * @author Zijian Liao
 * @since
 */
@Component
public class MyConsumer {

    @RabbitListener(queues = {RabbitConstant.SPRING_BOOT_QUEUE})
    @RabbitHandler
    public void onMessage(Message message, Channel channel) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String msg = objectMapper.readValue(message.getBody(), String.class);
        System.out.println(msg);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

}
