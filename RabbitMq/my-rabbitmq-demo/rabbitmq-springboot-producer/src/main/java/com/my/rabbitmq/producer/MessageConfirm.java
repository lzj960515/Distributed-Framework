package com.my.rabbitmq.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

/**
 * 消息确认
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class MessageConfirm implements RabbitTemplate.ConfirmCallback {
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        System.out.println("消息id: " + correlationData.getId());
        System.out.println("ack: " + ack);
        if(!ack){
            System.err.println("消息投递失败，原因：" + cause);
        }
    }
}
