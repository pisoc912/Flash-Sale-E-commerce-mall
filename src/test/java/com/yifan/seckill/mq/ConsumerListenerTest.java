package com.yifan.seckill.mq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Date;
@SpringBootTest
class ConsumerListenerTest {

    @Resource
    private RocketMQService rocketMQService;

    @Test
    void sendMQTest()throws Exception {
        rocketMQService.sendMessage("test","Hello World!" + new Date().toString());
    }
}