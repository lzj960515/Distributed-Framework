## 分布式集群管理

使用Zookeeper实现分布式集群管理，功能如下：

- 查看线上服务节点的资源使用情况
- 服务离线通知
- 服务资源超出阈值告警

### 架构设计：

![](monitor.png)

### 具体实现：

- 服务端收集服务状态信息发送给Zookeeper
- 监控管理中心监听Zookeeper monitor节点下的子结点状态，做事件响应

### 代码：

#### 服务端：使用agent技术监测服务端信息

- pom

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <artifactId>my-zookeeper-demo</artifactId>
        <groupId>com.my</groupId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>agent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    <inceptionYear>2020-Now</inceptionYear>

    <properties>
        <java.version>1.8</java.version>
        <maven.compiler.source>${java.version}</maven.compiler.source>
        <maven.compiler.target>${java.version}</maven.compiler.target>
    </properties>

    <dependencies>
        <!-- https://mvnrepository.com/artifact/org.apache.zookeeper/zookeeper -->
        <dependency>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>3.5.8</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>com.101tec</groupId>
            <artifactId>zkclient</artifactId>
            <version>0.11</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.12</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.9.9.3</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>2.2</version>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Project-name>monitor-collector</Project-name>
                            <Project-version>${project.version}</Project-version>
                            <Premain-Class>agent.MonitorAgent</Premain-Class>
                            <Can-Redefine-Classes>true</Can-Redefine-Classes>
                            <Can-Retransform-Classes>true</Can-Retransform-Classes>
                        </manifestEntries>
                    </archive>
                    <skip>true</skip>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer
                                        implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>com.cbt.agent.Agent</mainClass>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

dependency-reduced-pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <parent>
    <artifactId>my-zookeeper-demo</artifactId>
    <groupId>com.my</groupId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>
  <modelVersion>4.0.0</modelVersion>
  <artifactId>agent</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <inceptionYear>2020-Now</inceptionYear>
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-jar-plugin</artifactId>
        <version>2.2</version>
        <configuration>
          <archive>
            <manifestEntries>
              <Project-name>monitor-collector</Project-name>
              <Project-version>${project.version}</Project-version>
              <Premain-Class>agent.MonitorAgent</Premain-Class>
              <Can-Redefine-Classes>true</Can-Redefine-Classes>
              <Can-Retransform-Classes>true</Can-Retransform-Classes>
            </manifestEntries>
          </archive>
          <skip>true</skip>
        </configuration>
      </plugin>
      <plugin>
        <artifactId>maven-shade-plugin</artifactId>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>shade</goal>
            </goals>
            <configuration>
              <transformers>
                <transformer>
                  <mainClass>com.cbt.agent.Agent</mainClass>
                </transformer>
              </transformers>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
  <properties>
    <java.version>1.8</java.version>
    <maven.compiler.source>${java.version}</maven.compiler.source>
    <maven.compiler.target>${java.version}</maven.compiler.target>
  </properties>
</project>

```



- domain

```java
package agent.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 操作系统信息
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode
public class OSInfo {
    public String ip;
    public Double cpu;
    public long usedMemorySize;
    public long usableMemorySize;
    public String pid;
    public long lastUpdateTime;

}

```

```java
package agent.domain;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

/**
 * 获取CPU相关信息
 */
public class CPUMonitorCalc {

    private static CPUMonitorCalc instance = new CPUMonitorCalc();

    private OperatingSystemMXBean osMxBean;
    private ThreadMXBean threadBean;
    private long preTime = System.nanoTime();
    private long preUsedTime = 0;

    private CPUMonitorCalc() {
        osMxBean = ManagementFactory.getOperatingSystemMXBean();
        threadBean = ManagementFactory.getThreadMXBean();
    }

    public static CPUMonitorCalc getInstance() {
        return instance;
    }

    public double getProcessCpu() {
        long totalTime = 0;
        for (long id : threadBean.getAllThreadIds()) {
            totalTime += threadBean.getThreadCpuTime(id);
        }
        long curtime = System.nanoTime();
        long usedTime = totalTime - preUsedTime;
        long totalPassedTime = curtime - preTime;
        preTime = curtime;
        preUsedTime = totalTime;
        return (((double) usedTime) / totalPassedTime / osMxBean.getAvailableProcessors()) * 100;
    }
}
```

- agent

```java
package agent;

import agent.domain.CPUMonitorCalc;
import agent.domain.OSInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.I0Itec.zkclient.ZkClient;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * agent
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class MonitorAgent {

    private static final String CLUSTER = "172.20.140.111:2181,172.20.140.220:2181,172.20.140.28:2181";
    private static final String ROOT_PATH = "/my-manager";
    private static final String SERVER_PATH = ROOT_PATH + "/server";


    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("搜集服务信息插件启动中....");
        ZkClient zkClient = new ZkClient(CLUSTER);
        if (!zkClient.exists(ROOT_PATH)) {
            zkClient.createPersistent(ROOT_PATH);
        }
        //创建临时节点
        String nodePath = zkClient.createEphemeralSequential(SERVER_PATH, getOsInfo());
        //上报信息
        Thread thread = new Thread(()->{
            while(true){
                String osInfo = getOsInfo();
                zkClient.writeData(nodePath, osInfo);
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"zk-monitor");

        thread.setDaemon(true);
        thread.start();
        System.out.println("搜集服务信息插件已启动");
    }

    private static String getOsInfo() {
        OSInfo bean = new OSInfo();
        bean.lastUpdateTime = System.currentTimeMillis();
        bean.ip = getLocalIp();
        bean.cpu = CPUMonitorCalc.getInstance().getProcessCpu();
        MemoryUsage memoryUsag = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        bean.usedMemorySize = memoryUsag.getUsed() / 1024 / 1024;
        bean.usableMemorySize = memoryUsag.getMax() / 1024 / 1024;
        bean.pid = ManagementFactory.getRuntimeMXBean().getName();
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getLocalIp() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return addr.getHostAddress();
    }

}
```

> 打包后运行服务端时，只要加上vm参数 -javaagent:D:\agent-1.0.0-SNAPSHOT.jar 即可挂载上监控插件

#### 监控中心

```java
@Controller
public class MonitorController implements InitializingBean {

    private static final String CLUSTER = "172.20.140.111:2181,172.20.140.220:2181,172.20.140.28:2181";
    private static final String ROOT_PATH = "/my-manager";
    private static final String SERVER_PATH = ROOT_PATH + "/server";
    ZkClient zkClient;

    @RequestMapping("/list")
    public String list(Model model){
        List<OSInfo> items = new ArrayList<>();
        List<String> children = zkClient.getChildren(ROOT_PATH);
        for (String child : children) {
            String path = ROOT_PATH + "/" + child;
            System.out.println(path);
            OSInfo osInfo = convert(zkClient.readData(path));
            System.out.println(osInfo);
            items.add(osInfo);
        }
        model.addAttribute("items", items);
        return "list";
    }

    ObjectMapper mapper = new ObjectMapper();
    private OSInfo convert(String json) {
        try {
            return mapper.readValue(json, OSInfo.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        zkClient = new ZkClient(CLUSTER);
    }
}
```

## 分布式注册中心

> 详见dubbo篇



## 分布式锁

使用Zookeeper实现一把分布式锁，具体功能

- 同时只有一个服务能获取到锁
- 未获取到服务的线程进入阻塞
- 服务解锁后通知其他服务唤醒阻塞线程

### 具体实现：

- 各个服务获取锁时在`lock`结点下创建临时节点
- 获取到`lock`结点下所有子结点
- 判断自己是不是最前的结点
- 是则获取到锁，否则监听前一个结点，并阻塞自己
- 服务解锁后，删除自己这个结点，Zookeeper通知下一个监听结点
- 结点重新获取`lock`结点下所有子结点判断自己是不是最前的结点（检查一次，也可以不检查），唤醒阻塞线程，获取到了锁
- 重复以上逻辑

### 代码：

```java
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

public class ZookeeperLock {
    private static final String CLUSTER = "172.20.140.111:2181,172.20.140.220:2181,172.20.140.28:2181";
    private ZkClient zkClient;
    private static final String ROOT_PATH = "/my-lock";
    private static final Map<Lock,Thread> nodeMap = new HashMap<>();

    public ZookeeperLock() {
        zkClient = new ZkClient(CLUSTER);
        if (!zkClient.exists(ROOT_PATH)) {
            zkClient.createPersistent(ROOT_PATH);
        }
    }
    // 获取锁
    public Lock lock(String lockId, long timeout) {
        // 创建临时节点
        Lock lockNode = createLockNode(lockId);
        lockNode = tryActiveLock(lockNode);// 尝试激活锁
        if (!lockNode.isActive()) {
            nodeMap.put(lockNode,Thread.currentThread());
            System.out.println(Thread.currentThread().getName()+"park");
            LockSupport.parkNanos(timeout);
            System.out.println(Thread.currentThread().getName()+"unpark");
        }
        if (!lockNode.isActive()) {
            throw new RuntimeException(" lock  timeout");
        }
        return lockNode;
    }

    // 释放锁
    public void unlock(Lock lock) {
        if (lock.isActive()) {
            zkClient.delete(lock.getPath());
        }
    }

    // 尝试激活锁
    private Lock tryActiveLock(Lock lockNode) {

        // 获取根节点下面所有的子节点
        List<String> list = zkClient.getChildren(ROOT_PATH)
                .stream()
                .sorted()
                .map(p -> ROOT_PATH + "/" + p)
                .collect(Collectors.toList());      // 判断当前是否为最小节点

        String firstNodePath = list.get(0);
        // 最小节点是不是当前节点
        if (firstNodePath.equals(lockNode.getPath())) {
            lockNode.setActive(true);
        } else {
            String upNodePath = list.get(list.indexOf(lockNode.getPath()) - 1);
            zkClient.subscribeDataChanges(upNodePath, new IZkDataListener() {
                @Override
                public void handleDataChange(String dataPath, Object data) throws Exception {

                }

                @Override
                public void handleDataDeleted(String dataPath) throws Exception {
                    System.out.println("节点删除:" + dataPath);
                    Lock lock = tryActiveLock(lockNode);
                    if (lock.isActive()) {
                        LockSupport.unpark(nodeMap.get(lockNode)); // 释放了
                    }
                    zkClient.unsubscribeDataChanges(upNodePath, this);
                }
            });
        }
        return lockNode;
    }


    public Lock createLockNode(String lockId) {
        String nodePath = zkClient.createEphemeralSequential(ROOT_PATH + "/" + lockId, "w");
        return new Lock(lockId, nodePath);
    }


    public static class Lock {
        private String lockId;
        private String path;
        private boolean active;
        public Lock(String lockId, String path) {
            this.lockId = lockId;
            this.path = path;
        }
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isActive() {
            return active;
        }
        public void setActive(boolean active) {
            this.active = active;
        }
    }

    public static void main(String[] args) {
        ZookeeperLock lock = new ZookeeperLock();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        for(int i =0 ;i<10;i++){
            new Thread(()->{
                try {
                    countDownLatch.await();
                    System.out.println(Thread.currentThread().getName() + "准备获取锁");
                    Lock lockNode = lock.lock("lockKey", TimeUnit.SECONDS.toNanos(10));
                    System.out.println(Thread.currentThread().getName() + "获取到锁");
                    Thread.sleep(5000);
                    lock.unlock(lockNode);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            },"Thread-"+i).start();
        }
        countDownLatch.countDown();
    }
}
```



