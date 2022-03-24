package quick.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import quick.QuickApplication;

import javax.annotation.Resource;
import java.util.List;

/**
 * set test
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuickApplication.class)
public class SetTest {

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Test
    public void lottery(){
        String lotteryKey = "lottery";
        redisTemplate.opsForSet().add(lotteryKey,"user1");
        redisTemplate.opsForSet().add(lotteryKey,"user2");
        redisTemplate.opsForSet().add(lotteryKey,"user3");
        redisTemplate.opsForSet().add(lotteryKey,"user4");
        //随机取出count个元素但不移除,可能重复
        List<Object> objects = redisTemplate.opsForSet().randomMembers(lotteryKey, 2);
        objects.forEach(System.out::println);
        System.out.println(redisTemplate.opsForSet().size(lotteryKey));

        //随机取出count个元素但不移除,不重复
        redisTemplate.opsForSet().distinctRandomMembers(lotteryKey,2).forEach(System.out::println);
        System.out.println(redisTemplate.opsForSet().size(lotteryKey));

        //随机取出count个元素并移除
        redisTemplate.opsForSet().pop(lotteryKey, 2).forEach(System.out::println);
        System.out.println(redisTemplate.opsForSet().size(lotteryKey));
    }
}
