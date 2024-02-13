package com.yifan.seckill.service;

import com.yifan.seckill.db.dao.SeckillActivityDao;
import com.yifan.seckill.db.dao.SeckillCommodityDao;
import com.yifan.seckill.db.po.SeckillActivity;
import com.yifan.seckill.db.po.SeckillCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ActivityItemPageService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Autowired
    private SeckillCommodityDao seckillCommodityDao;

    public void createActivityHtml(long seckillActivityId){
        PrintWriter writer = null;
        try{
            SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
            SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());

            Map<String,Object> resultMap = new HashMap<>();
            resultMap.put("seckillActivity",seckillActivity);
            resultMap.put("seckillCommodity",seckillCommodity);
            resultMap.put("seckillPrice",seckillActivity.getSeckillPrice());
            resultMap.put("oldPrice",seckillActivity.getOldPrice());
            resultMap.put("commodityId",seckillActivity.getCommodityId());
            resultMap.put("commodityName",seckillCommodity.getCommodityName());
            resultMap.put("commodityDesc",seckillCommodity.getCommodityDesc());

            // Create thymeleaf context
            Context context = new Context();

            // Put data into context
            context.setVariables(resultMap);

            // create output stream
            File file = new File("src/main/resources/templates/"+"seckill_item_"+seckillActivityId + ".html");
            writer = new PrintWriter(file);

            templateEngine.process("seckill_item",context,writer);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (Exception e){
            log.error(e.toString());
            log.error("Page static failed: "+ seckillActivityId);
        }finally {
            if(writer != null){
                writer.close();
            }
        }
    }
}
