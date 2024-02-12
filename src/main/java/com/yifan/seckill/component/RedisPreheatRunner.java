package com.yifan.seckill.component;

import com.yifan.seckill.db.dao.SeckillActivityDao;
import com.yifan.seckill.db.po.SeckillActivity;
import com.yifan.seckill.service.RedisService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RedisPreheatRunner implements ApplicationRunner {

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private RedisService redisService;

    /**
     * When starting the project, store product inventory in redis
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitysByStatus(1);
        for(SeckillActivity seckillActivity:seckillActivities){
            redisService.setValue("stock:"+seckillActivity.getId(),
                    (long)seckillActivity.getAvailableStock());
            }
        }
}
