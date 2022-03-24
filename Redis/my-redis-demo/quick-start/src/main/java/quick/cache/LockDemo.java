package quick.cache;

import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Zijian Liao
 * @since
 */
public class LockDemo {

    private static final int COUNT = 100;
    public static void main(String[] args) throws Exception {
        long beginTime = System.nanoTime();
        for (int i = 0; i < COUNT; i++) {
            System.currentTimeMillis();
        }

        long elapsedTime = System.nanoTime() - beginTime;
        System.out.println("100 System.currentTimeMillis() serial calls: " + elapsedTime + " ns");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(COUNT);
        for (int i = 0; i < COUNT; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    //System.currentTimeMillis();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        beginTime = System.nanoTime();
        startLatch.countDown();
        endLatch.await();
        elapsedTime = System.nanoTime() - beginTime;
        System.out.println("100 System.currentTimeMillis() parallel calls: " + elapsedTime + " ns");
    }

    private static final Map<String, String> MAP = new ConcurrentHashMap<>(2);

    private static final Lock LOCK = new ReentrantLock();
    private static volatile AtomicInteger var = new AtomicInteger();
    private static volatile int var2 = 0;
    @Test
    public void build() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CountDownLatch end = new CountDownLatch(2000);
        //模拟大量请求
        for (int i = 0; i < 2000; i++) {
            new Thread(() -> {
                try {
                    countDownLatch.await();
                    //var2++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    end.countDown();
                }
                //doBuild();

            }, "thread-" + i).start();
        }
        System.out.println(var2);
        TimeUnit.SECONDS.sleep(5);
       // countDownLatch.countDown();
        end.await();
        System.out.println(var2);
       // Thread.sleep(5000);
    }

    public void doBuild() {
        if (MAP.get("build") != null) {
            return;
        }
        // 尝试获取锁
        boolean lock = LOCK.tryLock();
        if (lock) {
            System.out.println(Thread.currentThread().getName() + " 获得了锁");
            if (MAP.get("build") !=null) {
                return;
            }
            try {
                System.err.println(Thread.currentThread().getName() + " 进行了设值");
                try {
                    // 模拟设置堵塞
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MAP.put("build", "123");
            } finally {
                // 解锁
                LOCK.unlock();
            }
        } else {
            try {
                // 让度时间片
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 重试
            doBuild();
        }
    }
}
