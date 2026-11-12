package com.example.zxds.odr.db.dao;


import com.example.zxds.odr.db.pojo.OrderProfitsharingEntity;

import java.util.List;
import java.util.Map;

public interface OrderProfitsharingDao {
    int insert(OrderProfitsharingEntity entity);
    Map<String, Object> searchDriverIncome(String uuid);
    int updateProfitsharingStatus(long profitsharingId);
    List<Map<String, Object>> searchProcessingRecords(int minutes);
}




