package com.my.rabbitmq.producer;

import com.my.rabbitmq.domain.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 生产者
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@Component
public class MyProducer implements InitializingBean {

    private static final String SPRING_BOOT_EXCHANGE = "springboot-exchange";

    @Autowired
    private RabbitTemplate rabbitTemplate;


    public void sendMessage(){
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(SPRING_BOOT_EXCHANGE, "springboot-key.hello", "hello rabbitmq!", correlationData);
    }

    public void sendOrder(){
        String no = UUID.randomUUID().toString();
        CorrelationData correlationData = new CorrelationData(no);
        Order order = new Order();
        order.setNo(no);
        order.setMoney(100);

        rabbitTemplate.convertAndSend(SPRING_BOOT_EXCHANGE, "springboot-key.hello", order, correlationData);
    }

    public void sendOrder2(){
        String no = UUID.randomUUID().toString();
        CorrelationData correlationData = new CorrelationData(no);
        Order order = new Order();
        order.setNo(no);
        order.setMoney(100);

        rabbitTemplate.convertAndSend("hello", order, correlationData);
    }

    public void sendDelay(){
        String no = UUID.randomUUID().toString();
        CorrelationData correlationData = new CorrelationData(no);
        Order order = new Order();
        order.setNo(no);
        order.setMoney(100);

        rabbitTemplate.convertAndSend("delayExchange", "springboot-delay-queue-key", order, correlationData);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        //设置消息转换器
        /*Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);*/
    }
}
