package com.my.kafka;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@SpringBootApplication
public class MyKafkaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyKafkaApplication.class, args);
    }

    @Bean
    public ApplicationRunner runner(KafkaTemplate<String, String> kafkaTemplate){
        return args -> {
            for (int i = 0; i< 15; i++){
                //测试在手动ack的情况下发送多条消息
                //测试结果：如果消费者不ack，则会发生消息已经被消费，但偏移量未改变，导致下次消费者仍旧可以消费到消息
                ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("bug", "this is springboot message! " + i);
                future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        System.err.println("消息发送失败 " + throwable.getMessage());
                    }

                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        System.out.println("消息发送成功 topic:"+ result.getRecordMetadata().topic()
                                + " partition: " + result.getRecordMetadata().partition()
                                + "offset: " + result.getRecordMetadata().offset());
                    }
                });
            }
        };
    }
}
