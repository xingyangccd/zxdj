package com.example.zxds.bff.driver.service;

import com.example.zxds.bff.driver.controller.form.*;

import java.util.Map;

public interface DriverService {

    /**
     * 注册新司机
     */
    String registerNewDriver(RegisterNewDriverForm form);

    /**
     * 更新司机实名信息
     */
    int updateDriverAuth(UpdateDriverAuthForm form);

    /**
     * 采集司机人脸
     */
    String createDriverFaceModel(CreateDriverFaceModelForm form);

    /**
     * 司机登录
     */
    Map<String,Object> login(LoginForm form);

    /**
     * 根据司机ID查询司机基本信息
     */
    Map<String, Object> searchDriverBaseInfo(SearchDriverBaseInfoForm form);

    /**
     * 根据driverId查询司机当天的营业数据
     */
    Map<String, Object> searchWorkbenchData(long driverId);

    /**
     * 根据司机ID查询司机的实名认证信息
     */
    Map<String, Object> searchDriverAuth(SearchDriverAuthForm form);
}
