package com;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class SeckillApplicationTests {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DefaultRedisScript<Long> script;

    @Test
    void contextLoads() {
    }

    @Test
    public void testLock03() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String value = UUID.randomUUID().toString();
        //给锁添加一个过期时间，防止应用在运行过程中抛出异常导致锁无法及时得到释放
        Boolean isLock = valueOperations.setIfAbsent("k1",value,5, TimeUnit.SECONDS);
        //没人占位
        if (isLock){
            valueOperations.set("name","xxxx");
            String name = (String) valueOperations.get("name");
            System.out.println(name);
            System.out.println(valueOperations.get("k1"));
            //释放锁
            Boolean result = (Boolean) redisTemplate.execute(script,
                    Collections.singletonList("k1"), value);
            System.out.println(result);
        }else {
            //有人占位，停止/暂缓 操作
            System.out.println("有线程在使用，请稍后");
        }
    }

    }
