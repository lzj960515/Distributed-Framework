package quick.pipeline;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.context.junit4.SpringRunner;
import quick.QuickApplication;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 管道测试
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuickApplication.class)
public class PipelineTest {

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Test
    public void pipelineTest(){
        String incrKey = "product";
        String userKey = "user";
        List<Object> objects = redisTemplate.executePipelined(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.stringCommands().incr(incrKey.getBytes());
                connection.stringCommands().set(userKey.getBytes(),"lomio".getBytes());
                return null;
            }
        });

        System.out.println(objects);
    }

    @Test
    public void luaTest(){
        String script = "redis.call('set',KEYS[1],ARGV[1]) " +
                " return redis.call('get', KEYS[1]) ";
        RedisScript<String> of = RedisScript.of(script, String.class);
        String yjr = redisTemplate.execute(of, Arrays.asList("yjr"), "666");

        System.out.println(yjr);
    }
}
