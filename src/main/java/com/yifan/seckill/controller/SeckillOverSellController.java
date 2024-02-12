package com.yifan.seckill.controller;

import com.yifan.seckill.service.SeckillActivityService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class SeckillOverSellController {

//    @Resource
//    private SeckillOverSellService seckillOverSellService;

    @Resource
    private SeckillActivityService seckillActivityService;
//    @ResponseBody
//    @RequestMapping("/seckill/{seckillActivityId}")
//    public String seckill(@PathVariable long seckillActivityId){
//        return seckillOverSellService.processSeckill(seckillActivityId);
//    }

    /**
     * Use lua script to handle snap-up requests
     * @param seckillActivityId
     * @return
     */
    @ResponseBody
    @RequestMapping("/seckill/{seckillActivityId}")
    public String seckillCommodity(@PathVariable long seckillActivityId){
        boolean stockValidatorResult= seckillActivityService.seckillStockValidator(seckillActivityId);
        return stockValidatorResult ? "Successful Purchase!": "Product sold out";
    }
}
