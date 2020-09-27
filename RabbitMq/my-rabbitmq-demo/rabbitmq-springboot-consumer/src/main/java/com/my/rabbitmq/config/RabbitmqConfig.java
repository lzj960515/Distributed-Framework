package com.my.rabbitmq.config;

import com.my.rabbitmq.constant.RabbitConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitmq配置
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@Configuration
public class RabbitmqConfig {


    @Bean
    public TopicExchange springbootExchange(){
        return new TopicExchange(RabbitConstant.SPRING_BOOT_EXCHANGE, true, false);
    }

    @Bean
    public Queue springbootQueue(){
        return new Queue(RabbitConstant.SPRING_BOOT_QUEUE, true, false, false);
    }

    @Bean
    public Binding springbootBinding(){
        return BindingBuilder.bind(springbootQueue()).to(springbootExchange()).with(RabbitConstant.SPRING_BOOT_ROUTING_KEY);
    }
}
