# Docker 安装 Mysql 主从

## 创建网络(测试使用)

由于我的是mac, 并且是个伪主从，所以需要创建一个网络环境，用于主从节点互相通信。

```shell
docker network create mysql_network;
```

## 1. 安装主节点

### 1.1 编写docker-compose文件

```yml
version: '3.1'
services:
    mysql-master:
        image: mysql:8.0.20
        restart: always
        environment:
            MYSQL_ROOT_PASSWORD: root
        command:
          --default-authentication-plugin=mysql_native_password
          --character-set-server=utf8mb4
          --collation-server=utf8mb4_general_ci
          --explicit_defaults_for_timestamp=true
          --lower_case_table_names=1
        ports:
          - 3307:3306
        volumes:
          - ./my.cnf:/etc/mysql/my.cnf
          - ./data:/var/lib/mysql
#正常情况无需该配置
networks:
    default:
        external:
            name: mysql_network
```

### 1.2 编写my.cnf文件

```
[mysqld]
server-id=1
log_bin=master-bin
log_bin-index=master-bin.index
skip-name-resolve
pid-file        = /var/run/mysqld/mysqld.pid
socket          = /var/run/mysqld/mysqld.sock
datadir         = /var/lib/mysql
secure-file-priv= NULL
# Disabling symbolic-links is recommended to prevent assorted security risks
symbolic-links=0

# Custom config should go here
!includedir /etc/mysql/conf.d/
```

### 1.3 启动

```shell
docker-compose up -d
```

## 2. 安装从节点

### 2.1 编写docker-compose文件

```yaml
version: '3.1'
services:
    mysql-slave:
        image: mysql:8.0.20
        restart: always
        environment:
            MYSQL_ROOT_PASSWORD: root
        command:
          --default-authentication-plugin=mysql_native_password
          --character-set-server=utf8mb4
          --collation-server=utf8mb4_general_ci
          --explicit_defaults_for_timestamp=true
          --lower_case_table_names=1
        ports:
          - 3307:3306
        volumes:
          - ./my.cnf:/etc/mysql/my.cnf
          - ./data:/var/lib/mysql
#正常情况无需该配置
networks:
    default:
        external:
            name: mysql_network
```

### 2.2 编写my.cnf文件

```
[mysqld]
server-id=2
#打开MySQL中继日志
relay-log-index=slave-relay-bin.index
relay-log=slave-relay-bin
#打开从服务二进制日志
log-bin=binlog
#使得更新的数据写进二进制日志中
log-slave-updates=1
# 设置3306端口
port=3306
pid-file        = /var/run/mysqld/mysqld.pid
socket          = /var/run/mysqld/mysqld.sock
datadir         = /var/lib/mysql
secure-file-priv= NULL
# Disabling symbolic-links is recommended to prevent assorted security risks
symbolic-links=0

# Custom config should go here
!includedir /etc/mysql/conf.d/
```

### 3.3 启动

```
docker-compose up -d
```

## 3. 主从配置

### 3.1 主节点配置

```shell
#进入到主节点
docker exec -it <主节点容器id> /bin/bash
#登陆mysql
mysql -uroot -p
#开启远程登录
use mysql;
update user set host='%' where user='root';
flush privileges;
GRANT REPLICATION SLAVE ON *.* TO 'root'@'%';
flush privileges;
#查看主节点同步状态：
show master status;
```

状态一般长这样：

```
+---------------+----------+--------------+------------------+-------------------+
| File          | Position | Binlog_Do_DB | Binlog_Ignore_DB | Executed_Gtid_Set |
+---------------+----------+--------------+------------------+-------------------+
| binlog.000005 |     1712 |              |                  |                   |
+---------------+----------+--------------+------------------+-------------------+
```

> File: 当前写的binlog日志的名称
>
> Position: 当前写的binlog日志的位置

### 3.2 从节点配置

```shell
#进入到主节点
docker exec -it <从节点容器id> /bin/bash
#登陆mysql
mysql -uroot -p
#开启远程登录
use mysql;
update user set host='%' where user='root';
flush privileges;
show master status;
#关键步骤！！
#设置同步主节点
CHANGE MASTER TO
MASTER_HOST='mysql-master',
MASTER_PORT=3306,
MASTER_USER='root',
MASTER_PASSWORD='root',
MASTER_LOG_FILE='master-bin.000005',
MASTER_LOG_POS=467,
GET_MASTER_PUBLIC_KEY=1;
#MASTER_HOST是主节点的容器service-name, docker网络知识
#MASTER_PORT是主节点的容器端口
#MASTER_LOG_FILE 和 MASTER_LOG_POS是上一步查看主节点状态的两个值
#开启slave
start slave;
#查看主从同步状态
show slave status;
或者用 show slave status \G; 这样查看比较简洁
```

### 3.3 查看是否配置成功

```
*************************** 1. row ***************************
               Slave_IO_State: Waiting for master to send event
                  Master_Host: mysql-master
                  Master_User: root
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: master-bin.000004
          Read_Master_Log_Pos: 1676
               Relay_Log_File: slave-relay-bin.000005
                Relay_Log_Pos: 1893
        Relay_Master_Log_File: master-bin.000004
             Slave_IO_Running: Yes
            Slave_SQL_Running: Yes
              Replicate_Do_DB: 
          Replicate_Ignore_DB: 
           Replicate_Do_Table: 
       Replicate_Ignore_Table: 
      Replicate_Wild_Do_Table: 
  Replicate_Wild_Ignore_Table: 
                   Last_Errno: 0
                   Last_Error: 
                 Skip_Counter: 0
          Exec_Master_Log_Pos: 1676
              Relay_Log_Space: 2271
              Until_Condition: None
               Until_Log_File: 
                Until_Log_Pos: 0
           Master_SSL_Allowed: No
           Master_SSL_CA_File: 
           Master_SSL_CA_Path: 
              Master_SSL_Cert: 
            Master_SSL_Cipher: 
               Master_SSL_Key: 
        Seconds_Behind_Master: 0
Master_SSL_Verify_Server_Cert: No
                Last_IO_Errno: 0
                Last_IO_Error: 
               Last_SQL_Errno: 0
               Last_SQL_Error: 
  Replicate_Ignore_Server_Ids: 
             Master_Server_Id: 1
                  Master_UUID: c681fe4e-5fde-11eb-99b5-0242ac170002
             Master_Info_File: mysql.slave_master_info
                    SQL_Delay: 0
          SQL_Remaining_Delay: NULL
      Slave_SQL_Running_State: Slave has read all relay log; waiting for more updates
           Master_Retry_Count: 86400
                  Master_Bind: 
      Last_IO_Error_Timestamp: 
     Last_SQL_Error_Timestamp: 
               Master_SSL_Crl: 
           Master_SSL_Crlpath: 
           Retrieved_Gtid_Set: 
            Executed_Gtid_Set: 
                Auto_Position: 0
         Replicate_Rewrite_DB: 
                 Channel_Name: 
           Master_TLS_Version: 
       Master_public_key_path: 
        Get_master_public_key: 1
            Network_Namespace: 
1 row in set (0.00 sec)
```

> 看到状态：Slave_IO_State: Waiting for master to send event
>
> 关键的两个值：  Slave_IO_Running: Yes, Slave_SQL_Running: Yes

## 4. 主从集群测试

略