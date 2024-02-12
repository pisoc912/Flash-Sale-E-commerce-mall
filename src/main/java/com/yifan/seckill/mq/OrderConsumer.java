package com.yifan.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.yifan.seckill.db.dao.OrderDao;
import com.yifan.seckill.db.dao.SeckillActivityDao;
import com.yifan.seckill.db.po.Order;
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
@RocketMQMessageListener(topic = "seckill_order", consumerGroup = "seckill_order_group")
public class OrderConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private OrderDao orderDao;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        // 1.Parse the request message to create an order
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Receive create order request: " + message);
        Order order = JSON.parseObject(message, Order.class);

        // 2.Reduce stock
        boolean lockStockResult =  seckillActivityDao.lockStock(order.getSeckillActivityId());
        if(lockStockResult){
            // Lock Successful
            // 1 = Created.
            order.setOrderStatus(1);
        } else {
            // 0 = No stock
            order.setOrderStatus(0);
        }

        // 3. Insert order
        orderDao.insertOrder(order);

    }
}
