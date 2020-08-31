package quick.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.test.context.junit4.SpringRunner;
import quick.QuickApplication;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuickApplication.class)
public class HashTest {

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Test
    public void msetTest(){
        String hashKey = "user";
        Map<String,Object> user = new HashMap<>(2,1);
        user.put(hashKey + ":name","jack");
        user.put(hashKey + ":balance", 1000);
        redisTemplate.opsForHash().putAll(hashKey, user);
        List<Object> objects = redisTemplate.opsForHash().multiGet(hashKey, Arrays.asList(hashKey + ":name", hashKey + ":balance"));
        objects.forEach(System.out::println);
        ScanOptions.ScanOptionsBuilder scanOptionsBuilder = ScanOptions.scanOptions();
        scanOptionsBuilder.count(0);
        Cursor<Map.Entry<Object, Object>> scan = redisTemplate.opsForHash().scan(hashKey, scanOptionsBuilder.build());
        System.out.println(scan.next().getKey() + ": " +scan.next().getValue());
    }
}
