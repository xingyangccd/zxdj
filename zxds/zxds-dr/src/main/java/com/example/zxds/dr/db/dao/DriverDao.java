package com.example.zxds.dr.db.dao;


import java.util.List;
import java.util.Map;

public interface DriverDao {
    long hasDriver(Map<String, Object> param);
    int registerNewDriver(Map<String, Object> param);
    int updateDriverAuth(Map<String, Object> param);
    Map<String,Object> searchDriverNameAndSex(long driverId);
    int updateDriverArchive(long driverId);
    Map<String,Object> login(String openId);
    Map<String,Object> searchDriverBaseInfo(long driverId);
    List<Map<String, Object>> searchDriverByPage(Map<String, Object> param);
    long searchDriverCount(Map<String, Object> param);
    Map<String, Object> searchDriverAuth(long driverId);
    Map<String, Object> searchDriverRealSummary(long driverId);
    int updateDriverRealAuth(Map<String, Object> param);
    Map<String, Object> searchDriverBriefInfo(long driverId);
    String searchDriverOpenId(long driverId);
}




