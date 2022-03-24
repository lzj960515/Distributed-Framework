package com.my.rabbit.confirm;

import com.rabbitmq.client.ConfirmListener;

import java.io.IOException;

/**
 * 消息确认监听器
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class MyConfirmListener implements ConfirmListener {
    @Override
    public void handleAck(long deliveryTag, boolean multiple) throws IOException {
        System.out.println("消息已被签收，deliveryTag: " + deliveryTag + " multiple: "+ multiple);
    }

    @Override
    public void handleNack(long deliveryTag, boolean multiple) throws IOException {
        System.err.println("消息未被签收，deliveryTag: " + deliveryTag + " multiple: "+ multiple);
    }
}
