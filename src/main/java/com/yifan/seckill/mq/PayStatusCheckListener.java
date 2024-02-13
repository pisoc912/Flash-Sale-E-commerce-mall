package com.yifan.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.yifan.seckill.db.dao.OrderDao;
import com.yifan.seckill.db.dao.SeckillActivityDao;
import com.yifan.seckill.db.po.Order;
import com.yifan.seckill.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
@Slf4j
@Component
@RocketMQMessageListener(topic = "pay_check",consumerGroup = "pay_check_group")
public class PayStatusCheckListener implements RocketMQListener<MessageExt> {

    @Resource
    private OrderDao orderDao;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private RedisService redisService;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Received the order pay check message: "+message);
        Order order = JSON.parseObject(message, Order.class);
        // 1. Check Order
        Order orderInfo = orderDao.queryOrder(order.getOrderNo());
        // 2. Check order have paid
        if(orderInfo.getOrderStatus() != 2){
            // 3. Unpaid, close order
            log.info("Not paid & close order No:"+orderInfo.getOrderNo());
            orderInfo.setOrderStatus(99);
            orderDao.updateOrder(orderInfo);
            // 4. Revert stock
            seckillActivityDao.revertStock(order.getSeckillActivityId());
            // Revert stock in redis
            redisService.revertStock("stock:"+order.getSeckillActivityId());
            // 5.Remove user from purchase restriction list
            redisService.removeLimitMember(order.getSeckillActivityId(),order.getUserId());
        }

    }
}
