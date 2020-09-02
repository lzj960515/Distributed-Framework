package quick.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import quick.QuickApplication;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 缓存失效
 * 由于大批量缓存在同一时间失效可能导致大量请求同时穿透缓存直达数据库，可能会造成数据库瞬间压力过大甚至挂掉，
 * 对于这种情况我们在批量增加缓存时最好将这一批数据的缓存过期时间设置为一个时间段内的不同时间。
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuickApplication.class)
public class CacheFailure {

    @Resource
    public RedisTemplate<String, Object> redisTemplate;

    @Test
    public void solverCacheFailure() {
        String key = "product";
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            String finalKey = key + i;
            //将数据缓存成随机300~600的过期时间
            redisTemplate.opsForValue().set(finalKey,finalKey, random.nextInt(300) + 300, TimeUnit.SECONDS);
        }
    }
}
