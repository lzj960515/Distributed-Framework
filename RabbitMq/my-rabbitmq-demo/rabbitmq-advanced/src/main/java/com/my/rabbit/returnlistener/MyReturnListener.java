package com.my.rabbit.returnlistener;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ReturnListener;

import java.io.IOException;
import java.util.Arrays;

/**
 * 消息退回监听器
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class MyReturnListener implements ReturnListener {
    @Override
    public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
        System.out.println("--消息不可达记录---");
        System.out.println("replyCode: " + replyCode);
        System.out.println("replyText: " + replyText);
        System.out.println("exchange: " + exchange);
        System.out.println("routingKey: " + routingKey);
        System.out.println("properties: " + properties);
        System.out.println("body: " + new String(body));
    }
}
