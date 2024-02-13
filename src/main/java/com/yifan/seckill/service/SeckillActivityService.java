package com.yifan.seckill.service;

import com.alibaba.fastjson.JSON;
import com.yifan.seckill.db.dao.OrderDao;
import com.yifan.seckill.db.dao.SeckillActivityDao;
import com.yifan.seckill.db.dao.SeckillCommodityDao;
import com.yifan.seckill.db.po.Order;
import com.yifan.seckill.db.po.SeckillActivity;
import com.yifan.seckill.db.po.SeckillCommodity;
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
    private SeckillCommodityDao seckillCommodityDao;

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

        /*
         * 1. Create Order
         */
        SeckillActivity seckillActivity = activityDao.querySeckillActivityById(seckillActivityId);
        Order order = new Order();
        // generator orderId using snowflake
        order.setOrderNo(String.valueOf(snowFlake.nextId()));
        order.setSeckillActivityId(seckillActivity.getId());
        order.setUserId(userId);
        order.setOrderAmount(seckillActivity.getSeckillPrice().longValue());

        /*
         * 2. Send Create Order Message
         */
        rocketMQService.sendMessage("seckill_order", JSON.toJSONString(order));
        /*
         * 3. Send Order Payment Status Check Message
         * delay time level = 3 => 10s
         */
        rocketMQService.sendDelayMessage("pay_check",JSON.toJSONString(order),3);
        return order;
    }

    public void pushSeckillInfoToRedis(long seckillActivityId){
        SeckillActivity seckillActivity = activityDao.querySeckillActivityById(seckillActivityId);
        redisService.setValue("seckillActivity:"+seckillActivityId,JSON.toJSONString(seckillActivity));

        SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        redisService.setValue("seckillCommodity:" + seckillActivity.getCommodityId(),JSON.toJSONString(seckillCommodity));
    }

    public void payOrderProcess(String orderNo) throws Exception {
        log.info("Complete payment order, Order No:" + orderNo);
        Order order = orderDao.queryOrder(orderNo);
        /**
         * 1. Determine whether the order exists
         * 2. Determine whether the order status is unpaid
         */
        if(order == null) {
            log.error("The order corresponding to the order number does not exist"+orderNo);
            return;
        }else if(order.getOrderStatus()!=1){
            log.error("Order status is invalid"+orderNo);
            return;
        }
        /**
         * 3.Order payment completed
         */
        order.setPayTime(new Date());
        order.setOrderStatus(2);
        orderDao.updateOrder(order);
        /**
         * Send Order Pay Successful Message
         */
        rocketMQService.sendMessage("pay_done",JSON.toJSONString(order));
    }

}
