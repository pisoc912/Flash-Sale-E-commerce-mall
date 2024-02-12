package com.yifan.seckill.service;

import com.alibaba.fastjson.JSON;
import com.yifan.seckill.db.dao.OrderDao;
import com.yifan.seckill.db.dao.SeckillActivityDao;
import com.yifan.seckill.db.po.Order;
import com.yifan.seckill.db.po.SeckillActivity;
import com.yifan.seckill.mq.RocketMQService;
import com.yifan.seckill.util.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Service
public class SeckillActivityService {

    @Resource
    private RedisService redisService;

    @Resource
    private OrderDao orderDao;

    @Resource
    private SeckillActivityDao activityDao;

    @Resource
    private RocketMQService rocketMQService;

    private SnowFlake snowFlake = new SnowFlake(1,1);

    /**
     * Determine whether the product is still in stock
     * @param activityId
     * @return
     */
    public boolean seckillStockValidator(long activityId){
        String key = "stock:"+activityId;
        return redisService.stockDeductValidation(key);
    }

    public Order createOrder(long seckillActivityId,long userId)throws Exception{
        SeckillActivity seckillActivity = activityDao.querySeckillActivityById(seckillActivityId);
        Order order = new Order();
        // generator orderId using snowflake
        order.setOrderNo(String.valueOf(snowFlake.nextId()));
        order.setSeckillActivityId(seckillActivity.getId());
        order.setUserId(userId);
        order.setOrderAmount(seckillActivity.getSeckillPrice().longValue());
        // send create order message
        rocketMQService.sendMessage("seckill_order", JSON.toJSONString(order));
        return order;
    }

    public void payOrderProcess(String orderNo) {
        Order order = orderDao.queryOrder(orderNo);
        boolean deductStockResult = activityDao.deductStock(order.getSeckillActivityId());

        if(deductStockResult){
            order.setPayTime(new Date());
            // 0 No stock
            // 1 Created wait for pay
            // 2 Paid
            order.setOrderStatus(2);
            orderDao.updateOrder(order);
        }
    }
}
