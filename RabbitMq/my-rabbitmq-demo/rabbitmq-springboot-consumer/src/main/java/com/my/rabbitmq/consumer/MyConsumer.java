package com.my.rabbitmq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.rabbitmq.domain.Order;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Scheduled;
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

    @RabbitListener(queues = {"${queue-name}"})
    @RabbitHandler
    public void onMessage(Message message, Channel channel) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Order order = objectMapper.readValue(message.getBody(), Order.class);
        System.out.println(order);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    @RabbitListener(queues = {"hello"})
    @RabbitHandler
    public void onMessage2(Message message, Channel channel) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Order order = objectMapper.readValue(message.getBody(), Order.class);
        System.out.println(order);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }


    //@RabbitListener(queues = {"springboot-delay-queue"})
    //@RabbitHandler
    public void onDelayMessage(Message message, Channel channel) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Order order = objectMapper.readValue(message.getBody(), Order.class);
        System.out.println(order);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    @RabbitListener(queues = {"dead-queue"})
    @RabbitHandler
    public void onDeadMessage(Message message, Channel channel) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Order order = objectMapper.readValue(message.getBody(), Order.class);
        System.out.println(order);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
