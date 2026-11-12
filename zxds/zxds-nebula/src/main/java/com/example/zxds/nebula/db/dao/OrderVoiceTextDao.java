package com.example.zxds.nebula.db.dao;

import com.example.zxds.nebula.db.pojo.OrderVoiceTextEntity;

import java.util.Map;

public interface OrderVoiceTextDao {

    int insert(OrderVoiceTextEntity entity);

    Long searchIdByUuid(String uuid);

    int updateCheckResult(Map<String, Object> param);

}
