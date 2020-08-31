package quick.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import quick.QuickApplication;

import javax.annotation.Resource;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuickApplication.class)
public class ZsetTest {

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Test
    public void rank(){
        String rankKey = "rank";
        redisTemplate.opsForZSet().add(rankKey, "user1", 10);
        redisTemplate.opsForZSet().add(rankKey, "user2", 30);
        redisTemplate.opsForZSet().add(rankKey, "user3", 5);
        redisTemplate.opsForZSet().add(rankKey, "user4", 20);
        //从大到小
        System.out.println(redisTemplate.opsForZSet().reverseRank(rankKey, "user2"));
        //从小到大
        System.out.println(redisTemplate.opsForZSet().rank(rankKey, "user2"));
    }
}
