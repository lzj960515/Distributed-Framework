package com.my.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@Component
public class MyKafkaConsumer {

    /*@KafkaListener(topics = {"bug"}, groupId = "bug-boot-group")
    public void onMessage(ConsumerRecord<String, String> record){
        System.out.printf("收到消息：partition=%d offset = %d, key = %s, value = %s%n",
                record.partition(), record.offset(), record.key(), record.value());
    }*/

    /*@KafkaListener(topics = {"bug"}, groupId = "bug-boot-group")
    public void onMessage2(ConsumerRecord<String, String> record, Acknowledgment ack){
        //收到ack需要将ackMode设置为manual
        System.out.printf("线程 " + Thread.currentThread().getName() + "消费了该条消息 \n" +
                "收到消息：partition=%d offset = %d, key = %s, value = %s%n",
                record.partition(), record.offset(), record.key(), record.value());
        ack.acknowledge();
    }*/

    /**
     * 设置并发数后发现将有多个线程一起消费消息, 但最大线程数不会超过分区总数
     * topic ：bug 只有两个分区，虽然此时设置了4个并发量，但只会有两个线程消费
     */
    @KafkaListener(topics = {"bug"}, groupId = "bug-boot-group",  concurrency = "4")
    @KafkaHandler
    public void onMessage3(ConsumerRecord<String, String> record, Acknowledgment ack){
        //收到ack需要将ackMode设置为manual
        System.out.printf("线程 " + Thread.currentThread().getName() + "消费了该条消息 \n" +
                        "收到消息：partition=%d offset = %d, key = %s, value = %s%n",
                record.partition(), record.offset(), record.key(), record.value());
        ack.acknowledge();
    }



}
