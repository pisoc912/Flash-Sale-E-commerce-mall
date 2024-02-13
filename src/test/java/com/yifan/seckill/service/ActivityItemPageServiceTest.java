package com.yifan.seckill.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ActivityItemPageServiceTest {
    @Autowired
    private ActivityItemPageService activityItemPageService;

    @Test
    public void createHtmlTest(){
        activityItemPageService.createActivityHtml(19);
    }

}