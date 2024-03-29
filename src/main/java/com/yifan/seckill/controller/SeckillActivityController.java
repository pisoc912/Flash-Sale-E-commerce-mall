package com.yifan.seckill.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.yifan.seckill.db.dao.OrderDao;
import com.yifan.seckill.db.dao.SeckillActivityDao;
import com.yifan.seckill.db.dao.SeckillCommodityDao;
import com.yifan.seckill.db.po.Order;
import com.yifan.seckill.db.po.SeckillActivity;
import com.yifan.seckill.db.po.SeckillCommodity;
import com.yifan.seckill.service.RedisService;
import com.yifan.seckill.service.SeckillActivityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class SeckillActivityController {

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Autowired
    private SeckillCommodityDao seckillCommodityDao;

    @Resource
    private OrderDao orderDao;
    @Resource
    private SeckillActivityService seckillActivityService;

    @Resource
    private RedisService redisService;

    @RequestMapping("/seckills")
    public String activityList(Map<String, Object> resultMap) throws BlockException {
        try(Entry entry = SphU.entry("seckills")){
            List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitysByStatus(1);
            resultMap.put("seckillActivities", seckillActivities);
            return "seckill_activity";
        } catch (BlockException e){
            log.error("Query flash sale activity list current limit",e.toString());
            return "wait";
        }


    }
    /**
     * Flash sale product details
     * @param resultMap
     * @param seckillActivityId
     * @return
     */
    @RequestMapping("/item/{seckillActivityId}")
    public String itemPage(Map<String,Object> resultMap,@PathVariable long seckillActivityId){
        SeckillActivity seckillActivity;
        SeckillCommodity seckillCommodity;

        String seckillActivityInfo = redisService.getValue("seckillActivity:"+seckillActivityId);
        if(StringUtils.isNotEmpty(seckillActivityInfo)){
            log.info("Redis cache data:"+seckillActivityInfo);
            seckillActivity = JSON.parseObject(seckillActivityInfo, SeckillActivity.class);
        } else {
            seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        }

        String seckillCommodityInfo = redisService.getValue("seckillCommodity:"+seckillActivity.getCommodityId());
        if(StringUtils.isNotEmpty(seckillCommodityInfo)){
            log.info("Redis cache data: "+seckillCommodityInfo);
            seckillCommodity = JSON.parseObject(seckillActivityInfo, SeckillCommodity.class);
        } else {
            seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        }

        resultMap.put("seckillActivity",seckillActivity);
        resultMap.put("seckillCommodity",seckillCommodity);
        resultMap.put("seckillPrice",seckillActivity.getSeckillPrice());
        resultMap.put("oldPrice",seckillActivity.getOldPrice());
        resultMap.put("commodityId",seckillActivity.getCommodityId());
        resultMap.put("commodityName",seckillCommodity.getCommodityName());
        resultMap.put("commodityDesc",seckillCommodity.getCommodityDesc());
        return "seckill_item";
    }

    //    @ResponseBody
    @RequestMapping("/addSeckillActivityAction")
    public String addSeckillActivityAction(
            @RequestParam("name") String name,
            @RequestParam("commodityId") long commodityId,
            @RequestParam("seckillPrice") BigDecimal seckillPrice,
            @RequestParam("oldPrice") BigDecimal oldPrice,
            @RequestParam("seckillNumber") long seckillNumber,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            Map<String, Object> resultMap) throws ParseException {
        startTime = startTime.substring(0, 10) + startTime.substring(11);
        endTime = endTime.substring(0, 10) + endTime.substring(11);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddhh:mm");
        SeckillActivity seckillActivity = new SeckillActivity();
        seckillActivity.setName(name);
        seckillActivity.setCommodityId(commodityId);
        seckillActivity.setSeckillPrice(seckillPrice);
        seckillActivity.setOldPrice(oldPrice);
        seckillActivity.setTotalStock(seckillNumber);
        seckillActivity.setAvailableStock(new Integer("" + seckillNumber));
        seckillActivity.setLockStock(0L);
        seckillActivity.setActivityStatus(1);
        seckillActivity.setStartTime(format.parse(startTime));
        seckillActivity.setEndTime(format.parse(endTime));
        seckillActivityDao.insertSeckillActivity(seckillActivity);
        resultMap.put("seckillActivity", seckillActivity);
        return "add_success";
    }


    @RequestMapping("/addSeckillActivity")
    public String addSeckillActivity() {
        return "add_activity";
    }

    /**
     * create order
     * @param userId
     * @param seckillActivityId
     * @return
     */
    @RequestMapping("/seckill/buy/{userId}/{seckillActivityId}")
    public ModelAndView seckillCommodity(@PathVariable long userId,@PathVariable long seckillActivityId){
        boolean stockValidateResult = false;
        ModelAndView modelAndView= new ModelAndView();
        try{
            /*
             * Determine whether it is on the restricted purchase list
             */
            if(redisService.isInLimitMember(seckillActivityId,userId)){
                modelAndView.addObject("resultInfo","Sorry, You are on the restricted purchase list.");
                modelAndView.setViewName("seckill_result");
                return modelAndView;
            }
            /*
             * Determine if you can purchase it for a limited time
             */
            stockValidateResult = seckillActivityService.seckillStockValidator(seckillActivityId);
            if(stockValidateResult){
                Order order = seckillActivityService.createOrder(seckillActivityId,userId);
                modelAndView.addObject("resultInfo","Flash sale successful, order created, Order ID:" + order.getOrderNo());
                modelAndView.addObject("orderNo",order.getOrderNo());
                // Add user in restricted purchase list
                redisService.addLimitMember(seckillActivityId,userId);
            } else {
                modelAndView.addObject("resultInfo","Sorry, Insufficient product inventory.");
            }
        }catch (Exception e){
            log.error("Flash sale activity is abnormal",e.toString());
            modelAndView.addObject("resultInfo","Flash sale failed");
        }
        modelAndView.setViewName("seckill_result");
        return modelAndView;
    }

    @RequestMapping("/seckill/orderQuery/{orderNo}")
    public ModelAndView orderQuery(@PathVariable String orderNo){
        log.info("Order Query:"+orderNo);
        Order order = orderDao.queryOrder(orderNo);
        ModelAndView modelAndView = new ModelAndView();

        if(order != null) {
            modelAndView.setViewName("order");
            modelAndView.addObject("order",order);
            SeckillActivity seckillActivity=seckillActivityDao.querySeckillActivityById(order.getSeckillActivityId());
            modelAndView.addObject("seckillActivity",seckillActivity);
        }else{
            modelAndView.setViewName("order_wait");
        }
        return modelAndView;
    }

    @RequestMapping("/seckill/payOrder/{orderNo}")
    public String payOrder(@PathVariable String orderNo) throws Exception {
        seckillActivityService.payOrderProcess(orderNo);
        return "redirect:/seckill/orderQuery/" +orderNo;
    }

    /**
     * Get system time
     * @return
     */
    @ResponseBody
    @RequestMapping("/seckill/getSystemTime")
    public String getSystemTime(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        return date;
    }

}