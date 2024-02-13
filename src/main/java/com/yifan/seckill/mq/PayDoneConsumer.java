package com.yifan.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.yifan.seckill.db.dao.SeckillActivityDao;
import com.yifan.seckill.db.po.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * Payment completion message processing
 * Deduct inventory
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "pay_done",consumerGroup = "pay_done_group")
public class PayDoneConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private SeckillActivityDao seckillActivityDao;
    @Override
    public void onMessage(MessageExt messageExt) {
        // 1.Parse the create order request message
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Received create order request:"+message);
        Order order = JSON.parseObject(message, Order.class);
        // 2.Deduct inventory
        seckillActivityDao.deductStock(order.getSeckillActivityId());
    }
}
