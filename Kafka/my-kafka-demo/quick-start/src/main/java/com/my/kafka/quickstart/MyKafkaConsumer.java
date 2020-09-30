package com.my.kafka.quickstart;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Properties;

/**
 * 消费者
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class MyKafkaConsumer {
    /**
     * 集群地址
     */
    private static final String HOST = "172.20.140.22:9092,172.20.140.22:9093,172.20.140.22:9094";

    private static final String AUTO_COMMIT = "false";

    public static void main(String[] args) {
        //参数配置
        Properties props = new Properties();
        //配置地址
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, HOST);
        //配置分组名
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "bug-group3");
        //是否自动提交
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, AUTO_COMMIT);
        //如果是自动提交，配置提交间隔
        //props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        //心跳时间，服务端broker通过心跳确认consumer是否故障，如果发现故障，就会通过心跳下发
        //rebalance的指令给其他的consumer通知他们进行rebalance操作，这个时间可以稍微短一点
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 1000);
        //服务端broker多久感知不到一个consumer心跳就认为他故障了，默认是10秒
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10 * 1000);
        //poll的最大间隔时间，如果两次poll操作的间隔超过了这个时间，broker就会认为这个consumer处理能力太弱，
        //会将其踢出消费组，将分区分配给别的consumer消费
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 30 * 1000);
        //把发送的key从字符串序列化为字节数组
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        //把发送消息value从字符串序列化为字节数组
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        //若指定新的分组， earliest 表示从头消费 latest 表示消费最新消息，默认为latest
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        //将参数封装到consumer中
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        String topic = "bug";
        //订阅主题 订阅模式 注意：订阅模式和分配模式不能同时使用
//        consumer.subscribe(Arrays.asList(topic));
//        TopicPartition topicPartition = new TopicPartition(topic, 0);
        //指定分区消费，分配模式
//        consumer.assign(Collections.singletonList(topicPartition));
        //指定offset消费
//        consumer.seek(topicPartition, 0);
        //消息回溯消费
//        consumer.seekToBeginning(Arrays.asList(topicPartition));


        //从指定时间点开始消费
        /*Map<TopicPartition, Long> map = new HashMap<>();
        List<PartitionInfo> topicPartitions = consumer.partitionsFor(topic);
        //从5分钟前前开始消费
        long fetchDataTime = System.currentTimeMillis() - 1000 * 60 * 5;
        for (PartitionInfo par : topicPartitions) {
            map.put(new TopicPartition(topic, par.partition()), fetchDataTime);
        }
        List<TopicPartition> partitions = new ArrayList<>();
        List<Long> offsets = new ArrayList<>();
        Map<TopicPartition, OffsetAndTimestamp> parMap = consumer.offsetsForTimes(map);
        for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry : parMap.entrySet()) {
            TopicPartition partition = entry.getKey();
            OffsetAndTimestamp value = entry.getValue();
            if (partition == null || value == null) continue;
            Long offset = value.offset();
            System.out.println("partition-" + partition.partition() + "|offset-" + offset);
            System.out.println();
            //根据消费里的timestamp确定offset
            //没有这行代码会导致下面的报错信息
            partitions.add(partition);
            offsets.add(offset);
        }
        //设置分区
        consumer.assign(partitions);
        //设置每个分区的offset
        for(int i = 0;i < offsets.size(); i++){
            consumer.seek(partitions.get(i), offsets.get(i));
        }*/

        //开始消费
        while(true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("收到消息：partition=%d offset = %d, key = %s, value = %s%n",
                        record.partition(), record.offset(), record.key(), record.value());
            }
            if (isManual() && records.count() > 0) {
                // 提交offset
                consumer.commitSync();
            }
        }
    }

    private static boolean isManual(){
        return "false".equals(AUTO_COMMIT);
    }
}
