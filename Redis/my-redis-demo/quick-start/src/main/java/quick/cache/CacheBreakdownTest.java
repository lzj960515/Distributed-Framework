package quick.cache;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import quick.QuickApplication;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * 缓存击穿测试
 * <p>
 * 缓存穿透是指查询一个根本不存在的数据， 缓存层和存储层都不会命中， 通常出于容错的考虑， 如果从存储层查不到数据则不写入缓存层。
 * 缓存穿透将导致不存在的数据每次请求都要到存储层去查询， 失去了缓存保护后端存储的意义。
 * 造成缓存穿透的基本原因有两个：
 * 第一， 自身业务代码或者数据出现问题。
 * 第二， 一些恶意攻击、 爬虫等造成大量空命中。
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuickApplication.class)
public class CacheBreakdownTest {

    @Resource
    public RedisTemplate<String, Object> redisTemplate;

    //通过缓存空对象解决
    @Test
    public void storageNull() {
        String key = "a";
        for (int i = 0; i < 100; i++) {
            String finalKey = key + i;
            Object result = redisTemplate.opsForValue().get(finalKey);
            if(result != null){
                return ;
            }
            //模拟数据库取对象
            result = get(finalKey);
            if(result == null){
                //如果取出来是空的，则设置过期事件
                redisTemplate.opsForValue().set(finalKey, "", 5, TimeUnit.MINUTES);
            }else {
                redisTemplate.opsForValue().set(finalKey, result);
            }
        }
    }

    Object get(String key){
        return null;
    }

    //初始化布隆过滤器
    //1000：期望存入的数据个数，0.001：期望的误差率
    BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.forName("utf-8")), 1000, 0.001);
    //使用布隆过滤器
    @Test
    public void bloom(){
        String key = "b";

        for (int i = 0; i < 100; i++) {
            String finalKey = key + i;
            bloomFilter.put(finalKey);
        }
        for (int i = 0; i < 100; i++) {
            String finalKey = key + i;
            boolean exist = bloomFilter.mightContain(finalKey);
            if(!exist){
                return;
            }
            Object result = redisTemplate.opsForValue().get(finalKey);
            if(result != null){
                return ;
            }
            //模拟数据库取对象
            result = get(finalKey);
            if(result == null){
                //如果取出来是空的，则设置过期事件
                redisTemplate.opsForValue().set(finalKey, "", 5, TimeUnit.MINUTES);
            }else {
                redisTemplate.opsForValue().set(finalKey, result);
            }
        }

    }
}
