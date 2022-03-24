package com.my.rabbitmq.controller;

import com.my.rabbitmq.producer.MyProducer;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发送消息
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@AllArgsConstructor
@RestController
public class MessageController {

    private final MyProducer myProducer;

    @GetMapping("/send")
    public String send(){
        myProducer.sendMessage();
        return "ok";
    }

    @GetMapping("/send-order")
    public String sendOrder(){
        myProducer.sendOrder();
        return "ok";
    }

    @GetMapping("/send-delay")
    public String sendDelay(){
        myProducer.sendDelay();
        return "ok";
    }
}
