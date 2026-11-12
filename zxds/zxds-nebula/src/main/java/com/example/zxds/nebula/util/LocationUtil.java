package com.example.zxds.nebula.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LocationUtil {

    // 地球平均半径，单位：公里
    private static final double EARTH_RADIUS = 6378.137;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 计算两点间距离（纯数学计算，高性能）
     *
     * @return double 距离 单位：公里（未做四舍五入，方便后续累加）
     */
    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);

        // 使用半正矢公式（Haversine formula）计算
        double s = 2 * Math.asin(Math.sqrt(Math.sin(a / 2) * Math.sin(a / 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.sin(b / 2) * Math.sin(b / 2)));
        s = s * EARTH_RADIUS;

        return s;
    }

    /**
     * 格式化距离（供业务层在累加完总距离后调用）
     *
     * @param distance 总距离（公里）
     * @return 保留三位小数的距离（精确到米）
     */
    public static double formatDistance(double distance) {
        return BigDecimal.valueOf(distance).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }
}