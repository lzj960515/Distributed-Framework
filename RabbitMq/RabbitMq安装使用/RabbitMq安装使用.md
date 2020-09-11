[RabbitMQ基本概念参考文档]( http://rabbitmq.mr-ping.com/AMQP/AMQP_0-9-1_Model_Explained.html )

## 安装

### 1. 安装RabbitMq所需的环境

```shell
yum install build-essential openssl openssl-devel unixODBC unixODBC-devel make gcc gcc- c++ kernel-devel m4 ncurses-devel tk tc xz -y
```

### 2. 下载安装包

```shell
wget www.rabbitmq.com/releases/erlang/erlang-18.3-1.el7.centos.x86_64.rpm
wget http://repo.iotti.biz/CentOS/7/x86_64/socat-1.7.3.2-5.el7.lux.x86_64.rpm
wget www.rabbitmq.com/releases/rabbitmq-server/v3.6.5/rabbitmq-server-3.6.5-1.noarch.rpm
```

### 3.安装

- 安装erlang语言环境

  ```shell
  rpm -ivh erlang-18.3-1.el7.centos.x86_64.rpm
  ```

- 安装socat加解密软件

  ```shell
  rpm -ivh socat-1.7.3.2-5.el7.lux.x86_64.rpm
  ```

- 安装rabbitmq

  ```shell
  rpm -ivh rabbitmq-server-3.6.5-1.noarch.rpm
  ```

  > 默认安装到了/usr/lib/rabbitmq/lib/rabbitmq_server-3.6.5 目录下

### 4. 修改配置

- 修改rabbit.app配置

  ```shell
  vim /usr/lib/rabbitmq/lib/rabbitmq_server-3.6.5/ebin/rabbit.app
  # 将<<guest>> 修改为 guest , 否则只能通过localhost访问
  {loopback_users, [<<"guest">>]} -> {loopback_users, ["guest"]}
  ```

- 修改本机系统文件

  ```shell
  vim /etc/rabbitmq/rabbitmq-env.conf
  #添加 NODENAME=rabbit
  ```

### 5. 验证服务器是否可用

- 启动服务

  ```shell
  [root@centos7lzj rabbitmq]# rabbitmq-server start &
  [1] 14460
  [root@centos7lzj rabbitmq]# 
                RabbitMQ 3.6.5. Copyright (C) 2007-2016 Pivotal Software, Inc.
    ##  ##      Licensed under the MPL.  See http://www.rabbitmq.com/
    ##  ##
    ##########  Logs: /var/log/rabbitmq/rabbit.log
    ######  ##        /var/log/rabbitmq/rabbit-sasl.log
    ##########
                Starting broker...
   completed with 0 plugins.
  [root@centos7lzj rabbitmq]# lsof -i:5672
  COMMAND   PID     USER   FD   TYPE DEVICE SIZE/OFF NODE NAME
  beam    14559 rabbitmq   48u  IPv6  45280      0t0  TCP *:amqp (LISTEN)
  
  ```

- 执行管控台插件，可以浏览器管理端控制

  ```shell
  [root@centos7lzj rabbitmq]# rabbitmq-plugins enable rabbitmq_management
  The following plugins have been enabled:
    mochiweb
    webmachine
    rabbitmq_web_dispatch
    amqp_client
    rabbitmq_management_agent
    rabbitmq_management
  
  Applying plugin configuration to rabbit@centos7lzj... started 6 plugins.
  ```

### 6. 在浏览器中访问

ip:15672 如： 172.20.140.111:15672

账号：guest

密码：guest



## 常用命令

- 启动服务： rabbitmqctl start_app
- 关闭服务：rabbitmqctl stop_app

## 代码Demo

由于所有demo都要创建连接，自定义一个获取连接的工厂类

```java
public class MyConnectionFactory {

    public static Connection connection(){
        //username 和 password 可在管理端自行创建
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("172.20.140.111");
        factory.setPort(5672);
        factory.setUsername("lzj");
        factory.setPassword("123456");
        try {
            return factory.newConnection();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            throw new RuntimeException("创建连接失败: "+e.getMessage());
        }
    }
}
```

### Quick-Start

- 消费者

  ```java
  import com.my.rabbit.MyConnectionFactory;
  import com.rabbitmq.client.Channel;
  import com.rabbitmq.client.Connection;
  import com.rabbitmq.client.QueueingConsumer;
  
  import java.nio.charset.StandardCharsets;
  
  /**
   * 消费者
   *
   * @author Zijian Liao
   * @since 1.0.0
   */
  public class QuickStartConsumer {
  
      private final static String QUEUE_NAME = "hello2";
  
      public static void main(String[] argv) throws Exception {
          Connection connection = MyConnectionFactory.connection();
          Channel channel = connection.createChannel();
          /**
           * durable:是否持久化，rabbitmq的队列是放在内存当中的，重启之后队列将会丢失，为true则会将队列持久化到磁盘上
           *      注意：该参数只是判断队列持久化与否，与消息无关，虽然队列持久化了，但消息是不会持久化的，若队列中存在消息，重启之后消息会丢失
           *      若想消息持久化，可以使用自定义消息，见Message的demo
           * exclusive: 是否独占，为true表示该队列只能有一个消费者消费，若有第二个消费者再次声明，将会抛出异常
           *      为false时可以有多个消费者一起消费，每条消息只能由一个消费者消费，默认是轮询的方式，一个消费者一条
           *      注意：开启此参数后(true)，消费者断开队列将自动删除，durable的效果被无效
           * autoDelete:是否自动删除，为true表示当最后一个消费者连接断开之后自动删除，durable的效果被无效
           */
          channel.queueDeclare(QUEUE_NAME, true, false, true, null);
          System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
          QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
          channel.basicConsume(QUEUE_NAME,true,queueingConsumer);
          while (true) {
              QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
              String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
              System.out.println(" [x] Received '" + message + "'");
          }
      }
  }
  ```

- 生产者

  ```java
  import com.my.rabbit.MyConnectionFactory;
  import com.rabbitmq.client.Channel;
  import com.rabbitmq.client.Connection;
  
  import java.io.IOException;
  import java.util.concurrent.TimeoutException;
  
  /**
   * 生产者
   *
   * @author Zijian Liao
   * @since 1.0.0
   */
  public class QuickStartProducer {
  
      static String QUEUE_NAME = "hello1";
  
      public static void main(String[] args) throws IOException, TimeoutException {
          Connection connection = MyConnectionFactory.connection();
          Channel channel = connection.createChannel();
          String message = "Hello World!";
          for (int i = 0; i < 5; i++) {
              channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
          }
          System.out.println(" [x] Sent '" + message + "'");
          channel.close();
          connection.close();
      }
  }
  ```

### 直接交换机

- 消费者

  ```java
  import com.my.rabbit.MyConnectionFactory;
  import com.rabbitmq.client.Channel;
  import com.rabbitmq.client.Connection;
  import com.rabbitmq.client.QueueingConsumer;
  
  import java.io.IOException;
  import java.nio.charset.StandardCharsets;
  
  /**
   * 直接交换机-消费者
   *
   * @author Zijian Liao
   * @since 1.0.0
   */
  public class DirectConsumer {
      static String QUEUE_NAME = "hello-direct";
      static String EXCHANGE_NAME = "my-direct";
      static String EXCHANGE_TYPE = "direct";
      static String ROUTING_KEY = "direct-key";
      public static void main(String[] args) throws IOException, InterruptedException {
          Connection connection = MyConnectionFactory.connection();
          Channel channel = connection.createChannel();
  
          channel.queueDeclare(QUEUE_NAME,true, false, false, null);
  
          channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
  
          channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
  
          QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
  
          channel.basicConsume(QUEUE_NAME,true,queueingConsumer);
  
          while (true){
              QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
              String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
              System.out.println(" [x] Received '" + message + "'");
          }
      }
  }
  ```

- 生产者

  ```java
  import com.my.rabbit.MyConnectionFactory;
  import com.rabbitmq.client.Channel;
  import com.rabbitmq.client.Connection;
  
  import java.io.IOException;
  import java.util.concurrent.TimeoutException;
  
  /**
   * 直接交换机-生产者
   *
   * @author Zijian Liao
   * @since 1.0.0
   */
  public class DirectProducer {
      /**
       * routingKey必须与消费者中声明的相同，否则消息无法正确发送
       */
      static String ROUTING_KEY = "direct-key";
      static String EXCHANGE_NAME = "my-direct";
      public static void main(String[] args) throws IOException, TimeoutException {
          Connection connection = MyConnectionFactory.connection();
          Channel channel = connection.createChannel();
          String message = "Hello Direct!";
  
          channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY,null, message.getBytes());
          System.out.println(" [x] Sent '" + message + "'");
          channel.close();
          connection.close();
      }
  }
  ```

### Topic交换机

- 生产者

  ```java
  import com.my.rabbit.MyConnectionFactory;
  import com.rabbitmq.client.Channel;
  import com.rabbitmq.client.Connection;
  import com.rabbitmq.client.QueueingConsumer;
  
  import java.io.IOException;
  import java.nio.charset.StandardCharsets;
  
  /**
   * Topic交换机-消费者
   *
   * @author Zijian Liao
   * @since 1.0.0
   */
  public class TopicConsumer {
      
      static String QUEUE_NAME = "hello-topic";
      static String EXCHANGE_NAME = "my-topic";
      static String EXCHANGE_TYPE = "topic";
      /**
       *  * :匹配一个单词 如 topic-key.* 可以匹配 topic-key.1 topic-key.a topic-key.a-b，但不能匹配 topic-key.a.b
       *  # :匹配多个单词 如 topic-key.# 可以匹配 topic-key.1 也可以匹配 topic-key.a.b topic-key.a.b.c
       *  也可以这样玩,放到前面 #.key 匹配 1.key 2.key a.b.key 或者放到中间
       */
      static String ROUTING_KEY = "topic-key.*";
      public static void main(String[] args) throws IOException, InterruptedException {
          Connection connection = MyConnectionFactory.connection();
          Channel channel = connection.createChannel();
  
          channel.queueDeclare(QUEUE_NAME,true, false, false, null);
  
          channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
          //如果有多个队列绑定了该交换机，则生产者发消息时，每个队列都会接到消息
          channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
  
          System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
          QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
  
          channel.basicConsume(QUEUE_NAME,true,queueingConsumer);
  
          while (true){
              QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
              String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
              System.out.println(" [x] Received '" + message + "'");
          }
      }
  }
  ```

- 生产者

  ```java
  import com.my.rabbit.MyConnectionFactory;
  import com.rabbitmq.client.Channel;
  import com.rabbitmq.client.Connection;
  
  import java.io.IOException;
  import java.util.concurrent.TimeoutException;
  
  /**
   * Topic交换机-生产者
   *
   * @author Zijian Liao
   * @since 1.0.0
   */
  public class TopicProducer {
      
      static String ROUTING_KEY = "topic-key.a";
      static String EXCHANGE_NAME = "my-topic";
      public static void main(String[] args) throws IOException, TimeoutException {
          Connection connection = MyConnectionFactory.connection();
          Channel channel = connection.createChannel();
          String message = "Hello topic!";
  
          channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY,null, message.getBytes());
          System.out.println(" [x] Sent '" + message + "'");
          channel.close();
          connection.close();
      }
  }
  ```

### 扇形交换机

- 消费者

  ```java
  import com.my.rabbit.MyConnectionFactory;
  import com.rabbitmq.client.Channel;
  import com.rabbitmq.client.Connection;
  import com.rabbitmq.client.QueueingConsumer;
  
  import java.io.IOException;
  import java.nio.charset.StandardCharsets;
  
  /**
   * 扇形交换机-消费者
   *
   * @author Zijian Liao
   * @since 1.0.0
   */
  public class FanoutConsumer {
  
      static String QUEUE_NAME = "hello-fanout";
      static String EXCHANGE_NAME = "my-fanout";
      static String EXCHANGE_TYPE = "fanout";
      /**
       * 扇形交换机routingKey无效
       */
      static String ROUTING_KEY = "fanout-key";
      public static void main(String[] args) throws IOException, InterruptedException {
          Connection connection = MyConnectionFactory.connection();
          Channel channel = connection.createChannel();
  
          channel.queueDeclare(QUEUE_NAME,true, false, false, null);
  
          channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
  
          channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
  
          System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
          QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
  
          channel.basicConsume(QUEUE_NAME,true,queueingConsumer);
  
          while (true){
              QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
              String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
              System.out.println(" [x] Received '" + message + "'");
          }
      }
  }
  ```

- 生产者

  ```java
  import com.my.rabbit.MyConnectionFactory;
  import com.rabbitmq.client.Channel;
  import com.rabbitmq.client.Connection;
  
  import java.io.IOException;
  import java.util.concurrent.TimeoutException;
  
  /**
   * 扇形交换机-生产者
   *
   * @author Zijian Liao
   * @since 1.0.0
   */
  public class FanoutProducer {
      /**
       * 扇形交换机routingKey无效
       */
      static String ROUTING_KEY = "everything";
      static String EXCHANGE_NAME = "my-fanout";
      public static void main(String[] args) throws IOException, TimeoutException {
          Connection connection = MyConnectionFactory.connection();
          Channel channel = connection.createChannel();
          String message = "Hello fanout!";
  
          channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY,null, message.getBytes());
          System.out.println(" [x] Sent '" + message + "'");
          channel.close();
          connection.close();
      }
  }
  ```

### 自定义消息

- 消费者

  ```java
  import com.my.rabbit.MyConnectionFactory;
  import com.rabbitmq.client.AMQP;
  import com.rabbitmq.client.Channel;
  import com.rabbitmq.client.Connection;
  import com.rabbitmq.client.QueueingConsumer;
  
  import java.io.IOException;
  import java.nio.charset.StandardCharsets;
  
  /**
   * 自定义消息属性-消费者， 这里直接把topic的demo拿过来
   *
   * @author Zijian Liao
   * @since 1.0.0
   */
  public class MessageConsumer {
  
      static String QUEUE_NAME = "hello-message1";
      static String EXCHANGE_NAME = "my-message";
      static String EXCHANGE_TYPE = "topic";
      /**
       *  * :匹配一个单词 如 topic-key.* 可以匹配 topic-key.1 topic-key.a topic-key.a-b，但不能匹配 topic-key.a.b
       *  # :匹配多个单词 如 topic-key.# 可以匹配 topic-key.1 也可以匹配 topic-key.a.b topic-key.a.b.c
       *  也可以这样玩,放到前面 #.key 匹配 1.key 2.key a.b.key 或者放到中间
       */
      static String ROUTING_KEY = "message-key.*";
      public static void main(String[] args) throws IOException, InterruptedException {
          Connection connection = MyConnectionFactory.connection();
          Channel channel = connection.createChannel();
  
          channel.queueDeclare(QUEUE_NAME,true, false, false, null);
  
          channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
  
          channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
  
          System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
          QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
  
          channel.basicConsume(QUEUE_NAME,true,queueingConsumer);
  
          while (true){
              QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
              AMQP.BasicProperties properties = delivery.getProperties();
              String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
              System.out.println(" [x] Received '" + message + "'");
              System.out.println("properties: " + properties.toString());
          }
      }
  }
  ```

- 生产者

  ```java
  import com.my.rabbit.MyConnectionFactory;
  import com.rabbitmq.client.AMQP;
  import com.rabbitmq.client.Channel;
  import com.rabbitmq.client.Connection;
  
  import java.io.IOException;
  import java.util.HashMap;
  import java.util.Map;
  import java.util.UUID;
  import java.util.concurrent.TimeoutException;
  
  /**
   * 自定义消息属性-生产者
   *
   * @author Zijian Liao
   * @since 1.0.0
   */
  public class MessageProducer {
      
      static String ROUTING_KEY = "message-key.a";
      static String EXCHANGE_NAME = "my-message";
      public static void main(String[] args) throws IOException, TimeoutException {
          Connection connection = MyConnectionFactory.connection();
          Channel channel = connection.createChannel();
          String message = "Hello topic!";
          Map<String, Object> headers = new HashMap<>(2,1);
          headers.put("name","jack");
          headers.put("age",20);
          /**
           * deliveryMode 1.不持久化 2.持久化  若队列没有持久化，消息持久化无意义，重启队列都没了
           * expiration 过期时间 单位毫秒 使用该参数后则持久化无效
           * contentEncoding 消息内容编码
           * messageId 消息id
           * correlationId 关联id
           * headers 消息头
           */
          AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
                  .deliveryMode(2)
  //                .expiration("10000")
                  .contentEncoding("UTF-8")
                  .messageId(UUID.randomUUID().toString())
                  .correlationId(UUID.randomUUID().toString())
                  .headers(headers)
                  .build();
          channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, basicProperties, message.getBytes());
          System.out.println(" [x] Sent '" + message + "'");
          channel.close();
          connection.close();
      }
  }
  ```

  