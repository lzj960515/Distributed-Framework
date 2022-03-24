# Kafka安装并配置SASL_PLAINTEXT认证

注：本文只作kafka安装过程中的踩坑处理，并不会有任何使用方面的知识。

> 小伙伴可以简单过一下，以后要是安装时遇到了问题，也有个地方找嘛

## 环境

内网服务器一台：ubuntu 18.04  ip: 192.168.2.15

外网服务器一台：ubutun 18.04  内网ip: 192.168.2.11 外网ip:123.123.123.123(非真实，仅做案例)

## 1. 安装Zookeeper

```shell
wget https://archive.apache.org/dist/zookeeper/zookeeper-3.5.9/apache-zookeeper-3.5.9-bin.tar.gz
tar -zxf apache-zookeeper-3.5.9-bin.tar.gz
cd apache-zookeeper-3.5.9-bin/
cp conf/zoo_sample.cfg conf/zoo.cfg
# 启动zookeeper
bin/zkServer.sh start
```

## 2.安装Kafka

### 2.1 下载压缩包解压

```shell
wget https://archive.apache.org/dist/kafka/2.4.1/kafka_2.11-2.4.1.tgz
cd kafka_2.11-2.4.1/
```

### 2.2 修改配置

vim config/server.properties

```properties
#broker.id属性在kafka集群中必须要是唯一
broker.id=0
#kafka部署的机器ip和提供服务的端口号
listeners=PLAINTEXT://192.168.2.15:9092   
#kafka的消息存储文件
log.dir=/usr/local/data/kafka-logs
#kafka连接zookeeper的地址
zookeeper.connect=192.168.2.15:2181
```

### 2.3 启动

```shell
#后台启动，运行日志在logs/erver.log文件里
./bin/kafka-server-start.sh -daemon config/server.properties
# 停止
./bin/kafka-server-stop.sh
```

## 3. 配置外网访问

由于kafka的emmm我也不知道啥特性，总之会遇到一个这么个情况，虽然使用nat将2.11的请求转到了2.15，但是客户端后面与kafka交互真就使用2.15了。

什么意思呢，就是说我的外网地址是：123.123.123.123，第一次连接之后，后面客户端却用192.168.2.15这个ip与kafka交互，很明显不得行呀。

所以我们就要让后面的交互也使用外网地址：123.123.123.123

### 3.1 修改配置

vim config/server.properties

```properties
advertised.listeners=PLAINTEXT://123.123.123.123:9092
```

> 增加该配置后，客户端就会用这个地址与kafka交互

### 3.2 配置NAT转发

进来的：

在192.168.2.11服务器上配置：

```shell
iptables -t nat -A PREROUTING -d  192.168.2.11  -p tcp -m tcp --dport 9092 -j DNAT --to-destination  192.168.2.15:9092
```

出去的：

在192.168.2.15服务器上配置：

```shell
root iptables -t nat -A POSTROUTING  --dst  192.168.2.15  -p tcp --dport 9092 -j SNAT --to-source  192.168.2.11
```

## 4. 配置SASL_PLAINTEXT认证

### 4.1 在config下新建文件`jaas.conf`

```
KafkaServer {
    org.apache.kafka.common.security.plain.PlainLoginModule required
    username="admin"
    password="admin"
    user_admin="admin";
};
```

> 账号密码的格式为 user_name=password, 所以user_admin="admin"表示账号admin和密码admin
>
> 这里可以配多个，比如再加个user_zhangsan="123456"
>
> 注意：一定要有一条与username和password的一致

### 4.2 修改配置

vim config/server.properties

```properties
listeners=SASL_PLAINTEXT://192.168.2.15:9092

advertised.listeners=SASL_PLAINTEXT://123.123.123.123:9092
security.inter.broker.protocol=SASL_PLAINTEXT
sasl.mechanism.inter.broker.protocol=PLAIN
sasl.enabled.mechanisms=PLAIN
```

> 将之前的协议PLAINTEXT改为SASL_PLAINTEXT

### 4.3 修改启动脚本

vim bin/kafka-run-class.sh

```shell
if [ -z "$KAFKA_OPTS" ]; then
  KAFKA_OPTS="-Djava.security.auth.login.config=$base_dir/config/jaas.conf"
fi
```

> 为KAFKA_OPTS加上jvm参数

接下来只需重启服务即可

## 5. SpringBoot使用SASL_PLAINTEXT连接Kafka

### 5.1 修改配置

```yaml
spring:
  kafka:
    bootstrap-servers: 123.123.123.123:9092
    producer: # 生产者
      # 开启sasl认证
      properties:
        sasl.mechanism: PLAIN
        security.protocol: SASL_PLAINTEXT
    consumer:
      # 开启sasl认证
      properties:
        sasl.mechanism: PLAIN
        security.protocol: SASL_PLAINTEXT
```

### 5.2 在resources目录下新建kafka-jaas.conf文件

```properties
KafkaClient {
 org.apache.kafka.common.security.plain.PlainLoginModule required
 username="admin"
 password="admin";
};
```

### 5.3 启动时配置sasl认证

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IOException {
        // 启动时配置sasl认证
        final File file = ResourceUtils.getFile("classpath:kafka-jaas.conf");
        System.setProperty("java.security.auth.login.config", file.getAbsolutePath());
        SpringApplication.run(Application.class, args);
    }

}
```