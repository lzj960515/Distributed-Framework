package quick.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import quick.QuickApplication;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * list test
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuickApplication.class)
public class ListTest {

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Test
    public void stack(){
        //一端入，一端出
        String stackKey = "stack";
        redisTemplate.opsForList().leftPush(stackKey,1);
        redisTemplate.opsForList().leftPush(stackKey,2);
        redisTemplate.opsForList().leftPush(stackKey,3);
        redisTemplate.opsForList().leftPush(stackKey,4);
        System.out.println(redisTemplate.opsForList().leftPop(stackKey));
        System.out.println(redisTemplate.opsForList().leftPop(stackKey));
        System.out.println(redisTemplate.opsForList().leftPop(stackKey));
        System.out.println(redisTemplate.opsForList().leftPop(stackKey));
    }

    @Test
    public void queue(){
        //一端入，另一端出
        String queueKey = "queue";
        redisTemplate.opsForList().leftPush(queueKey,1);
        redisTemplate.opsForList().leftPush(queueKey,2);
        redisTemplate.opsForList().leftPush(queueKey,3);
        redisTemplate.opsForList().leftPush(queueKey,4);
        System.out.println(redisTemplate.opsForList().rightPop(queueKey));
        System.out.println(redisTemplate.opsForList().rightPop(queueKey));
        System.out.println(redisTemplate.opsForList().rightPop(queueKey));
        System.out.println(redisTemplate.opsForList().rightPop(queueKey));
    }

    @Test
    public void blockingQueue(){
        //无数据则等待
        String blockKey = "blockQueue";
        redisTemplate.opsForList().leftPush(blockKey,1);
        System.out.println(redisTemplate.opsForList().rightPop(blockKey, 5, TimeUnit.SECONDS));
        System.out.println(redisTemplate.opsForList().rightPop(blockKey, 5, TimeUnit.SECONDS));
    }
}
