package com.my.rabbitmq.config;

import com.my.rabbitmq.constant.RabbitConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

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

    //-----声明延时队列----
    @Bean
    public CustomExchange delayExchange() {
        return new CustomExchange("delayExchange", "direct",true, false);
    }

    @Bean
    public Queue delayQueue(){
        Map<String,Object> queueArgs = new HashMap<>();
        //设置队列中的消息10s没有被消费就会过期
        queueArgs.put("x-message-ttl",10000);
        return new Queue("springboot-delay-queue", true, false, false, queueArgs);
    }

    @Bean
    public Binding delayBinding(){
        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with("springboot-delay-queue-key").noargs();
    }
}
