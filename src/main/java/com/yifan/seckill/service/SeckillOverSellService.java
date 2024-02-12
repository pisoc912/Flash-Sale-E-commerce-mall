package com.yifan.seckill.service;

import com.yifan.seckill.db.dao.SeckillActivityDao;
import com.yifan.seckill.db.po.SeckillActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillOverSellService {
    @Autowired
    private SeckillActivityDao seckillActivityDao;

    public String processSeckill(long activityId){
        SeckillActivity activity = seckillActivityDao.querySeckillActivityById(activityId);
        int availabkeStock = activity.getAvailableStock();
        String result;

        if(availabkeStock > 0){
            result =  "Congregation, Purchase successfully!";
            availabkeStock -= 1;
            activity.setAvailableStock(availabkeStock);
            seckillActivityDao.updateSeckillActivity(activity);
        } else {
            result = "Sorry, purchase failed, product stock is out!";
        }

        return result;
    }


}
