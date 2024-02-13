package com.yifan.seckill;

import com.yifan.seckill.service.RedisService;
import com.yifan.seckill.service.SeckillActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.UUID;

@SpringBootTest
public class RedisDemoTest {

    @Resource
    private RedisService redisService;

    @Resource
    private SeckillActivityService seckillActivityService;

    @Test
    public void stockTest(){
        String value = redisService.setValue("stock:19",10L).getValue("stock:19");
        System.out.println(value);
    }

    @Test
    public void pushSeckillInfoToRedisTest(){
        seckillActivityService.pushSeckillInfoToRedis(19);
    }

    /**
     * Test the results of lock acquisition under high concurrency
     */
    @Test
    public void testConcurrentAddLock(){
        for(int i = 0; i < 10; i++){
            String requestId = UUID.randomUUID().toString();
            System.out.println(redisService.tryGetDistributedLock("A",requestId,1000));
        }
    }

    @Test
    public void testConcurrent(){
        for(int i = 0; i < 10; i++){
            String requestId = UUID.randomUUID().toString();
            System.out.println(redisService.tryGetDistributedLock("A",requestId,1000));
            redisService.releaseDistributedLock("A",requestId);
        }
    }
}
