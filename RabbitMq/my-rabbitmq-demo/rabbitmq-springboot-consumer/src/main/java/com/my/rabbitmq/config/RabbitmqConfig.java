package com.my.rabbitmq.config;

import com.my.rabbitmq.constant.RabbitConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.HashMap;
import java.util.Map;

/**
 * rabbitmq配置
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@AutoConfigureAfter(value = EnvConfig.class)
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
    public Queue hello(){
        return new Queue("hello", true);
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
        //绑定死信交换机
        queueArgs.put("x-dead-letter-exchange", "dead-exchange");
        queueArgs.put("x-dead-letter-routing-key", "dead-key");
        return new Queue("springboot-delay-queue", true, false, false, queueArgs);
    }

    @Bean
    public Binding delayBinding(){
        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with("springboot-delay-queue-key").noargs();
    }

    //------声明死信队列------
    @Bean
    public Queue deadQueue(){
        return new Queue("dead-queue", true, false, false);
    }

    @Bean
    public DirectExchange deadExchange(){
        return new DirectExchange("dead-exchange", true, false);
    }

    @Bean
    public Binding deadBinding(){
        return BindingBuilder.bind(deadQueue()).to(deadExchange()).with("dead-key");
    }

}
