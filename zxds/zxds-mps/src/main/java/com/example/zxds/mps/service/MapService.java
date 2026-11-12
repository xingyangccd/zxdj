package com.example.zxds.mps.service;

import java.util.Map;

public interface MapService {

    /**
     * 计算预估里程和预估时间
     * @param mode 出行方式
     * @param startPlaceLatitude 起点Latitude
     * @param startPlaceLongitude 起点Longitude
     * @param endPlaceLatitude 终点Latitude
     * @param endPlaceLongitude 终点Longitude
     * @return 预估的里程（KM），预估时长（x小时x分）
     */
    Map<String, Object> estimateOrderMileageAndMinute(String mode,
                                                      String startPlaceLatitude,
                                                      String startPlaceLongitude,
                                                      String endPlaceLatitude,
                                                      String endPlaceLongitude);

    /**
     * 计算行驶路线
     * @param startPlaceLatitude 起点Latitude
     * @param startPlaceLongitude 起点Longitude
     * @param endPlaceLatitude 终点Latitude
     * @param endPlaceLongitude 中Longitude
     * @return 行驶方案
     */
    Map<String, Object> calculateDriveLine(String startPlaceLatitude,
                                           String startPlaceLongitude,
                                           String endPlaceLatitude,
                                           String endPlaceLongitude);
}
