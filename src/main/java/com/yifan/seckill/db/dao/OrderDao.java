package com.yifan.seckill.db.dao;

import com.yifan.seckill.db.po.Order;

public interface OrderDao {

    void insertOrder(Order order);
    Order queryOrder(String orderNo);
    void updateOrder(Order order);

}
