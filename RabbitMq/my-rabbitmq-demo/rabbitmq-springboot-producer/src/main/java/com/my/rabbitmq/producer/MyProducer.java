package com.my.rabbitmq.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        rabbitTemplate.convertAndSend(SPRING_BOOT_EXCHANGE, "springboot-key.hello", "hello rabbitmq!");
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        //设置消息转换器
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
    }
}
