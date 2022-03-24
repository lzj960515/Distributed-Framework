package com.my.rabbitmq;

import com.my.rabbitmq.producer.MyProducer;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Zijian Liao
 * @since
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProducerApplication.class)
public class Test {

    @Autowired
    private MyProducer myProducer;

    @org.junit.Test
    public void sendMessage(){
        myProducer.sendOrder2();
    }
}
