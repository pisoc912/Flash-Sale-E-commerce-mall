package com.yifan.seckill.service;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.Collections;

@Service
public class RedisService {

    @Resource
    private JedisPool jedisPool;

    public RedisService setValue(String key,Long value){
        Jedis client = jedisPool.getResource();
        client.set(key,value.toString());
        client.close();
        return this;
    }
    public String getValue(String key){
        Jedis client = jedisPool.getResource();
        String value= client.get(key);
        client.close();
        return value;
    }

    /**
     * Inventory judgment and deduction in cache
     * @param key
     * @return
     */
    public boolean stockDeductValidation(String key){
        try(Jedis client = jedisPool.getResource()){
            String script = "if redis.call('exists',KEYS[1]) == 1 then\n" +
                    "    local stock = tonumber(redis.call('get',KEYS[1]))\n" +
                    "    if(stock <= 0)then\n" +
                    "        return -1\n" +
                    "    end;\n" +
                    "\n" +
                    "    redis.call('decr',KEYS[1]);\n" +
                    "    return stock - 1;\n" +
                    "end;\n" +
                    "\n" +
                    "return -1;";
            Long stock = (Long) client.eval(script, Collections.singletonList(key),Collections.emptyList());

            if(stock<0){
                System.out.println("Inventory shortage");
                return false;
            }
            System.out.println("Successful purchase！");
            return true;
        }catch(Throwable throwable){
            System.out.println("Inventory deduction failed" + throwable);
            return false;
        }
    }

}
