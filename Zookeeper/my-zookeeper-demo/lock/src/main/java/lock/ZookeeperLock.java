package lock;

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
    private static final Map<Lock, Thread> nodeMap = new HashMap<>();

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
            nodeMap.put(lockNode, Thread.currentThread());
            System.out.println(Thread.currentThread().getName() + "park");
            LockSupport.parkNanos(timeout);
            System.out.println(Thread.currentThread().getName() + "unpark");
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
                    // 事件处理 与心跳 在同一个线程，如果Debug时占用太多时间，将导致本节点被删除，从而影响锁逻辑。
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
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
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


            }, "Thread-" + i).start();
        }
        countDownLatch.countDown();
    }
}

