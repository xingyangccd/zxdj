package com.example.zxds.dr.service;

import com.example.zxds.common.util.PageUtils;

import java.util.Map;

public interface DriverService {
    /**
     * 注册新新司机
     * @param param 参数
     * @return driverId
     */
    String registerNewDriver(Map<String, Object> param);

    /**
     * 更新司机的实名信息
     */
    int updateDriverAuth(Map<String, Object> param);

    /**
     * 创建司机人脸模型档案
     */
    String createDriverFaceModel(long driverId, String photo);

    /**
     * 司机登录
     */
    Map<String,Object> login(String code);

    /**
     * 根据司机ID查询司机基本信息
     */
    Map<String, Object> searchDriverBaseInfo(long driverId);

    /**
     * 分页查询司机的信息
     */
    PageUtils searchDriverByPage(Map<String, Object> param);

    /**
     * 通过司机ID获取司机的认证信息。
     */
    Map<String, Object> searchDriverAuth(long driverId);

    /**
     * 根据司机ID查询司机的实名信息（web端使用）
     */
    Map<String, Object> searchDriverRealSummary(long driverId);

    /**
     * 更新司机实名认证状态
     */
    int updateDriverRealAuth(Map<String, Object> param);

    /**
     * 查询司机的简要信息
     */
    Map<String, Object> searchDriverBriefInfo(long driverId);

    /**
     * 查询司机的openId
     */
    String searchDriverOpenId(long driverId);
}
