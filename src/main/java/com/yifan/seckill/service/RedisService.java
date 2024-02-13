package com.yifan.seckill.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.Collections;
@Slf4j
@Service
public class RedisService {

    @Resource
    private JedisPool jedisPool;

    /*
    Set value as long type
     */
    public RedisService setValue(String key,Long value){
        Jedis client = jedisPool.getResource();
        client.set(key,value.toString());
        client.close();
        return this;
    }

    /*
    Set value as string type
     */
    public void setValue(String key, String value) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.set(key, value);
        jedisClient.close();
    }

    /*
    Get value
     */
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

    /**
     * Rollback of unpaid orders after timeout
     * @param key
     */
    public void revertStock(String key) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.incr(key);
        jedisClient.close();
    }

    /**
     * Determine whether it is on the restricted purchase list
     * @param seckillActivityId
     * @param userId
     * @return
     */
    public boolean isInLimitMember(long seckillActivityId, long userId) {
        Jedis jedisClient = jedisPool.getResource();
        boolean sismember = jedisClient.sismember("seckillActivity_users:" + seckillActivityId,String.valueOf(userId));
        log.info("userId:{} activityId:{} In restricted purchase list:{}", userId,seckillActivityId,sismember);
        return sismember;
    }

    /**
     * Add in restricted purchase list
     * @param seckillActivityId
     * @param userId
     */
    public void addLimitMember(long seckillActivityId, long userId) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.sadd("seckillActivity_users:"+seckillActivityId,String.valueOf(userId));
        jedisClient.close();
    }

    /**
     * Remove Limit Member
     * @param seckillActivityId
     * @param userId
     */
    public void removeLimitMember(Long seckillActivityId, Long userId) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.srem("seckillActivity_users:"+seckillActivityId,String.valueOf(userId));
        jedisClient.close();
    }

    /**
     * Get Distributed Lock
     * @param lockKey
     * @param requestId
     * @param expireTime
     * @return
     * nxxx-NX: not exists
     * nxxx-XX: is exists
     * expx-EX: seconds
     * exps-PX: milliseconds
     */
    public boolean tryGetDistributedLock(String lockKey,String requestId, int expireTime){
        Jedis jedisClient = jedisPool.getResource();
        String result = jedisClient.set(lockKey,requestId,"NX","PX",expireTime);
        jedisClient.close();
        if("OK".equals(result)){
            return true;
        }
        return false;
    }

    /**
     * Releasee Distributed Lock
     * @param lockKey
     * @param requestId
     * @return whether release success
     */
    public boolean releaseDistributedLock(String lockKey, String requestId){
        Jedis jedisClient = jedisPool.getResource();
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Long result = (Long)jedisClient.eval(script,Collections.singletonList(lockKey),Collections.singletonList(requestId));
        jedisClient.close();
        if(result == 1L){
            return true;
        }
        return false;
    }
}
