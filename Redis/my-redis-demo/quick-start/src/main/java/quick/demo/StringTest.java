package quick.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;
import quick.QuickApplication;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 操作String
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuickApplication.class)
public class StringTest {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void set() {
        stringRedisTemplate.opsForValue().set("name", "lucy");
    }

    @Test
    public void lock() {
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch stop = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    //模拟多个服务同时抢锁
                    start.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent("lock", "lock", 30, TimeUnit.SECONDS);
                if (aBoolean) {
                    System.out.println(Thread.currentThread().getName() + "加锁成功");
                } else {
                    System.out.println(Thread.currentThread().getName() + "加锁失败");
                }
                stop.countDown();
            }, "thread-" + i).start();
        }
        start.countDown();
        try {
            stop.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void generateSequence() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String orderId = "orderId";
        stringRedisTemplate.opsForValue().set(orderId, "0");

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch stop = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    //模拟多个服务同时生成id
                    start.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Long increment = stringRedisTemplate.opsForValue().increment(orderId, 1000);
                long sequenceStartIndex = increment - 1000;
                System.out.println(Thread.currentThread().getName() + "生成的序列号区间为：[" + sequenceStartIndex + ", " + increment + "]");
                stop.countDown();
            }, "thread-" + i).start();
        }
        start.countDown();
        try {
            stop.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }


}
