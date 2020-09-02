package quick.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import quick.QuickApplication;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 热点缓存重建
 * <p>
 * 开发人员使用“缓存+过期时间”的策略既可以加速数据读写， 又保证数据的定期更新， 这种模式基本能够满足绝大部分需求。 但是有两个问题如果同时出现， 可能就会对应用造成致命的危害：
 * 当前key是一个热点key（例如一个热门的娱乐新闻），并发量非常大。
 * 重建缓存不能在短时间完成， 可能是一个复杂计算， 例如复杂的SQL、 多次IO、 多个依赖等。
 * 在缓存失效的瞬间， 有大量线程来重建缓存， 造成后端负载加大， 甚至可能会让应用崩溃。
 * 要解决这个问题主要就是要避免大量线程同时重建缓存。
 * 我们可以利用互斥锁（Redission）来解决，此方法只允许一个线程重建缓存， 其他线程等待重建缓存的线程执行完， 重新从缓存获取数据即可
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuickApplication.class)
public class CacheBuild {

    @Resource
    public RedisTemplate<String, String> redisTemplate;

    @Test
    public void build() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        //模拟大量请求
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                doBuild();

            },"thread-"+i).start();
        }
        countDownLatch.countDown();
        Thread.sleep(5000);
    }

    public void doBuild(){
        String lock = "lock";
        String build = redisTemplate.opsForValue().get("build");
        //第一步
        if(build == null){
            //第二步
            if (redisTemplate.opsForValue().setIfAbsent(lock, lock, 5, TimeUnit.SECONDS)) {
                System.out.println(Thread.currentThread().getName() + " 获得了锁");
                try{
                    build = redisTemplate.opsForValue().get("build");
                    //使用双重判断，防止走到第二步时刚好锁被释放，导致又重新设置
                    if(build==null){
                        System.err.println(Thread.currentThread().getName() + " 进行了设值");
                        redisTemplate.opsForValue().set("build","123");
                    }
                }finally {
                    redisTemplate.delete(lock);
                }
            }else {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                doBuild();
            }
        }else {
            return;
        }
    }
}
