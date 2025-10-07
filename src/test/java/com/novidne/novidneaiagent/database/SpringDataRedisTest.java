package com.novidne.novidneaiagent.database;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class SpringDataRedisTest {
    /**
     * redis模板
     */
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     *
     */
    @Test
    void testString(){
        redisTemplate.opsForValue().set("name","lisi");
        Object name = redisTemplate.opsForValue().get("name");
        System.out.println(name);
    }
}
