[官网文档地址](https://zookeeper.apache.org/doc/r3.5.8/zookeeperProgrammers.html )

## Zookeeper环境搭建与使用

### 下载安装包并启动

```shell
#下载3.5.8版本，并解压
wget https://mirrors.tuna.tsinghua.edu.cn/apache/zookeeper/zookeeper-3.5.8/apache-zookeeper-3.5.8-bin.tar.gz
tar -zxf apache-zookeeper-3.5.8-bin.tar.gz
cd apache-zookeeper-3.5.8-bin
#zookeeper默认使用conf/zoo.cfg文件作为配置文件
cp conf/zoo_sample.cfg conf/zoo.cfg
#启动 bin/zkServer.sh [--config <conf-dir>] {start|start-foreground|stop|restart|status|print-cmd}
bin/zkServer.sh start
```

### 配置文件说明

```properties
# The number of milliseconds of each tick
# Zookeeper时间配置中的基本单位 (毫秒)
tickTime=2000
# The number of ticks that the initial 
# synchronization phase can take
# follower初始化同步阶段连接到leader的最大时间，值为tickTime的倍数，即 initLimit*tickTime
initLimit=10
# The number of ticks that can pass between 
# sending a request and getting an acknowledgement
# follower发送请求到接收到响应的最大等待时间
syncLimit=5
# the directory where the snapshot is stored.
# do not use /tmp for storage, /tmp here is just 
# example sakes.
# 存储数据的目录，不要使用/tmp目录
dataDir=/tmp/zookeeper
# the port at which the clients will connect
# 服务端口号
clientPort=2181
# the maximum number of client connections.
# increase this if you need to handle more clients
#maxClientCnxns=60
#
# Be sure to read the maintenance section of the 
# administrator guide before turning on autopurge.
#
# http://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_maintenance
#
# The number of snapshots to retain in dataDir
# 保存的数据快照数量，之外的将会被清除
autopurge.snapRetainCount=3
# Purge task interval in hours
# Set to "0" to disable auto purge feature
# 自动触发清除任务时间间隔，小时为单位，0则为不启用
autopurge.purgeInterval=1
```

### 基本命令使用

- 连接到服务端

  ```shell
  # bin/zkCli.sh <-server localhost:2181> 不写默认连接到本机的2181端口
  bin/zkCli.sh
  ```

- 创建结点

  ```shell
  #create [-s] [-e] [-c] [-t ttl] path [data] [acl]
  create -s -e /earth 123
  ```

  - -s : 序号结点

  - -e : 临时结点

  - -c : 容器结点，当删除了容器结点下的所有子结点，若一段时间内，未对容器结点执行任务操作，容器结点也将被删除。

  - -t :  有效期结点，单位毫秒，若在ttl有效期内未对结点做任何操作，并且结点下无子结点，那么该结点过会将会被删除，`create -t 5000 /ttlnode` 该结点默认禁止创建，若想创建该结点，必须在启动时设置` zookeeper.extendedTypesEnabled=true`，可通过修改zkEnv.sh文件更改

    ```shell
    # default heap for zookeeper server
    ZK_SERVER_HEAP="${ZK_SERVER_HEAP:-1000}"
    export SERVER_JVMFLAGS="-Xmx${ZK_SERVER_HEAP}m $SERVER_JVMFLAGS -Dzookeeper.extendedTypesEnabled=true"
    ```

  - data : 数据

  - acl : 权限

- 列出结点

  ```shell
  #ls [-s] [-w] [-R] path -s 列出结点状态 -w 列出结点并添加监听 -R 递归列出结点和结点下的所有子结点
  ls -R /
  ```
  
  - -s :  列出结点状态
  - -w : 添加监听
  - -R : 递归列出结点和结点下的所有子结点

- 给结点设值

  ```shell
  # set [-s] [-v version] path data
  set /earth 123
  ```

  - -s : 显示结点状态
  - -v:  指定版本号，与状态中的dataVersion相同才能设值成功。这意味着并发场景下添加此参数时只会有一个客户端能够设置成功，类似乐观锁

  > 设置成功后dataVersion将会+1，所以当一个客户端设值成功后，版本便发生了改变，其他客户端自然设值失败

- 获取值

  ```shell
  # get [-s] [-w] path
  -s 状态 -w 添加监听
  ```

- 删除结点

  ```shell
  # 若结点下有子结点，则无法删除
  delete [-v version] path
  -v 版本号，效果同设值
  ```

- 删除结点和结点下的所有子结点

  ```shell
  deleteall path
  ```

- 查看结点状态

  ```shell
  stat [-w] path
  -w 添加监听
  ```

## Zookeeper 结点介绍

zookeeper 中结点叫znode存储结构上跟文件系统类似，以树级结构进行存储。不同之外在于znode没有目录的概念，不能执行类似cd之类的命令。znode结构包含如下：

* **path**:唯一路径 
* **childNode**：子结点
* **stat**:状态属性
* **type**:结点类型

### 结点类型

| 类型                  | 描述                           |
| :-------------------- | :----------------------------- |
| PERSISTENT            | 持久结点                       |
| PERSISTENT_SEQUENTIAL | 持久序号结点                   |
| EPHEMERAL             | 临时结点(不可在拥有子结点)     |
| EPHEMERAL_SEQUENTIAL  | 临时序号结点(不可在拥有子结点) |

1. PERSISTENT（持久结点）

持久化保存的结点，也是默认创建的

```shell
#默认创建的就是持久结点
create /test
```

1. PERSISTENT_SEQUENTIAL(持久序号结点)

创建时zookeeper 会在路径上加上序号作为后缀，。非常适合用于分布式锁、分布式选举等场景。创建时添加 -s 参数即可。

```shell
#创建序号结点
create -s /test
#返回创建的实际路径
Created /test0000000001
create -s /test
#返回创建的实际路径2
Created /test0000000002
```

1. EPHEMERAL（临时结点）

临时结点会在客户端会话断开后自动删除。适用于心跳，服务发现等场景。创建时添加参数-e 即可。

```shell
#创建临时结点， 断开会话 在连接将会自动删除
create -e /temp
```

1. EPHEMERAL_SEQUENTIAL（临时序号结点）

与持久序号结点类似，不同之处在于EPHEMERAL_SEQUENTIAL是临时的会在会话断开后删除。创建时添加 -e -s 

```shell
create -e -s /temp/seq
```

### **结点属性**

### **结点属性**

```shell
# 查看结点属性
stat /test
```

其属性说明如下表：

```properties
#创建结点的事务ID
cZxid = 0x385
#创建时间
ctime = Tue Sep 24 17:26:28 CST 2019
#修改结点的事务ID
mZxid = 0x385
#最后修改时间
mtime = Tue Sep 24 17:26:28 CST 2019
# 子结点变更的事务ID
pZxid = 0x385
#这表示对此znode的子结点进行的更改次数（不包括子结点）
cversion = 0
# 数据版本，变更次数
dataVersion = 0
#权限版本，变更次数
aclVersion = 0
#临时结点所属会话ID
ephemeralOwner = 0x0
#数据长度
dataLength = 17
#子结点数(不包括子子结点)
numChildren = 0
```

### 结点的监听：

客户添加 -w 参数可实时监听结点与子结点的变化，并且实时收到通知。非常适用保障分布式情况下的数据一至性。其使用方式如下：

| 命令                 | 描述                                 |
| :------------------- | :----------------------------------- |
| ls -w path           | 监听子结点的变化（增，删）           |
| get -w path          | 监听结点数据的变化                   |
| stat -w path         | 监听结点属性的变化                   |
| printwatches on\|off | 触发监听后，是否打印监听事件(默认on) |

### **acl权限设置**

ACL全称为Access Control List（访问控制列表），用于控制资源的访问权限。ZooKeeper使用ACL来控制对其znode的防问。基于`scheme:id:permission`的方式进行权限控制。scheme表示授权模式、id模式对应值、permission即具体的增删改权限位。

**scheme:认证模型**

| 方案   | 描述                                                         |
| :----- | :----------------------------------------------------------- |
| world  | 开放模式，world表示全世界都可以访问（这是默认设置）          |
| ip     | ip模式，限定客户端IP防问                                     |
| auth   | 用户密码认证模式，只有在会话中添加了认证才可以防问           |
| digest | 与auth类似，区别在于auth用明文密码，而digest 用sha-1+base64加密后的密码。在实际使用中digest 更常见。 |

**permission权限位**

| 权限位 | 权限   | 描述                             |
| :----- | :----- | :------------------------------- |
| c      | CREATE | 可以创建子结点                   |
| d      | DELETE | 可以删除子结点（仅下一级结点）   |
| r      | READ   | 可以读取结点数据及显示子结点列表 |
| w      | WRITE  | 可以设置结点数据                 |
| a      | ADMIN  | 可以设置结点访问控制列表权限     |

**acl 相关命令：**

| 命令    | 使用方式                | 描述         |
| :------ | :---------------------- | :----------- |
| getAcl  | getAcl <path>           | 读取ACL权限  |
| setAcl  | setAcl <path> <acl>     | 设置ACL权限  |
| addauth | addauth <scheme> <auth> | 添加认证用户 |

**world权限****示例**

**world权限****示例**
语法： setAcl <path> world:anyone:<权限位>
注：world模式中anyone是唯一的值,表示所有人

1. 查看默认节点权限：

```shell
#创建一个节点
create -e /testAcl
#查看节点权限
getAcl /testAcl
#返回的默认权限表示 ，所有人拥有所有权限。
'world,'anyone: cdrwa
```

1. 修改默认权限为 读写

```shell
#设置为rw权限 
setAcl /testAcl world:anyone:rw
# 可以正常读
get /testAcl
# 无法正常创建子节点
create -e /testAcl/t "hi"
# 返回没有权限的异常
Authentication is not valid : /testAcl/t
```

**IP权限示例：**
语法： setAcl <path> ip:<ip地址|地址段>:<权限位>

**auth模式示例:**
语法： 

1. setAcl <path> auth:<用户名>:<密码>:<权限位>
2. addauth digest <用户名>:<密码>

**digest 权限示例：**
语法： 

1. setAcl <path> digest :<用户名>:<密钥>:<权限位>
2. addauth digest <用户名>:<密码>

注1：密钥 通过sha1与base64组合加密码生成，可通过以下命令生成

```shell
echo -n <用户名>:<密码> | openssl dgst -binary -sha1 | openssl base64
```

注2：为节点设置digest 权限后，访问前必须执行addauth，当前会话才可以防问。

1. 设置digest 权限

```shell
#先 sha1 加密，然后base64加密
echo -n test:123456 | openssl dgst -binary -sha1 | openssl base64
#返回密钥
2Rz3ZtRZEs5RILjmwuXW/wT13Tk=
#设置digest权限
setAcl /test digest:test:2Rz3ZtRZEs5RILjmwuXW/wT13Tk=:cdrw
```

1. 查看节点将显示没有权限

```shell
#查看节点
get /test
#显示没有权限访问
org.apache.zookeeper.KeeperException$NoAuthException: KeeperErrorCode = NoAuth for /test
```

1. 给当前会话添加认证后在次查看

```shell
#给当前会话添加权限帐户
addauth digest test:123456
#在次查看
get /test
#获得返回结果
123456
```

ACL的特殊说明：
权限仅对当前节点有效，不会让子节点继承。如限制了IP防问A节点，但不妨碍该IP防问A的子节点 /A/B。