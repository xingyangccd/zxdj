package com.example.zxds.mps.service;

import java.util.List;
import java.util.Map;

public interface DriverLocationService {

    /**
     * 更新司机的GEO缓存以及在线缓存。
     */
    void updateLocationCache(Map<String, Object> param);

    /**
     * 删除司机的GEO缓存以及在线缓存。
     */
    void removeLocationCache(long driverId);

    /**
     * 查找关于这个订单适合的司机列表
     */
    List<Map<String, Object>> searchBefittingDriverAboutOrder(double startPlaceLatitude,
                                                              double startPlaceLongitude,
                                                              double endPlaceLatitude,
                                                              double endPlaceLongitude,
                                                              double mileage);

    /**
     * 将司机的实时位置保存到redis当中
     */
    void updateOrderLocationCache(Map<String, Object> param);

    /**
     * 查询司机的实时位置
     */
    Map<String, Object> searchOrderLocationCache(long orderId);

}
