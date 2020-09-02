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