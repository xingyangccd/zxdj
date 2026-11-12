package com.example.zxds.mps.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.example.zxds.mps.service.DriverLocationService;
import com.example.zxds.mps.util.CoordinateTransform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriverLocationServiceImpl implements DriverLocationService {

    private final StringRedisTemplate stringRedisTemplate;

    // 1.将司机的GPS定位更新到GEO缓存当中。
    // 2.更新司机的在线缓存。
    @Override
    public void updateLocationCache(Map<String, Object> param) {
        // 司机的ID
        long driverId = MapUtil.getLong(param, "driverId");
        // 司机的当前GPS定位
        String latitude = MapUtil.getStr(param, "latitude");
        String longitude = MapUtil.getStr(param, "longitude");
        // 司机的接单范围（KM）【司机的当前位置 → 代驾起点的距离】
        int rangeDistance = MapUtil.getInt(param, "rangeDistance");
        // 订单的里程范围（KM）【起点 → 终点】
        int orderDistance = MapUtil.getInt(param, "orderDistance");

        // 封装点对象（这个点就代表司机当前的位置）
        Point point = new Point(Convert.toDouble(longitude), Convert.toDouble(latitude));

        // "driver_location" GEO缓存的key（创建了GEO缓存。）
        stringRedisTemplate.opsForGeo().add("driver_location", point, String.valueOf(driverId));

        String orientateLongitude = MapUtil.getStr(param, "orientateLongitude");
        String orientateLatitude = MapUtil.getStr(param, "orientateLatitude");
        // 拼接
        String orientation = "none"; // none代表这个司机没有开启定向接单。
        if(StrUtil.isNotBlank(orientateLongitude) && StrUtil.isNotBlank(orientateLatitude)) {
            orientation = orientateLatitude + "," + orientateLongitude; // 代表这个司机开启了定向接单。
        }

        // 创建司机的上线缓存。
        String temp = rangeDistance + "#" + orderDistance + "#" + orientation;
        stringRedisTemplate.opsForValue().set("driver_online#" + driverId, temp, 60, TimeUnit.SECONDS);
    }

    // 1.删除司机的GEO缓存。
    // 2.删除司机的在线缓存。
    @Override
    public void removeLocationCache(long driverId) {
        // 删除司机的GEO缓存
        stringRedisTemplate.opsForGeo().remove("driver_location", String.valueOf(driverId));
        // 删除司机的上线缓存。
        stringRedisTemplate.delete("driver_online#" + driverId);
    }

    @Override
    public List<Map<String, Object>> searchBefittingDriverAboutOrder(double startPlaceLatitude,
                                                                     double startPlaceLongitude,
                                                                     double endPlaceLatitude,
                                                                     double endPlaceLongitude,
                                                                     double mileage) {
        // 1. 以订单起点为中心画一个半径5公里的圆。
        Point point = new Point(startPlaceLongitude, startPlaceLatitude);
        Distance distance = new Distance(5, RedisGeoCommands.DistanceUnit.KILOMETERS);
        Circle circle = new Circle(point, distance);

        // 2. 构建GEO查询参数：带距离、带坐标、按照距离升序
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance() // 查询结果中带有距离
                .includeCoordinates() // 查询结果带有坐标
                .sortAscending(); // 查询结果按照距离升序排列。

        // 3. 执行GEO查询，获取到起点5公里之内的所有司机。
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().radius("driver_location", circle, args);

        // 定义一个司机列表，最终返回
        List<Map<String, Object>> list = new ArrayList<>();
        if(results == null){
            return list;
        }

        // 逐个筛选司机。
        for(GeoResult<RedisGeoCommands.GeoLocation<String>> result : results){
            // 司机ID
            String driverId = result.getContent().getName();
            // 司机到代驾起点的距离
            double dist = result.getDistance().getValue();

            // 司机上线缓存的key
            String onlineKey = "driver_online#" + driverId;
            if(!stringRedisTemplate.hasKey(onlineKey)){
                // 司机如果不在线
                continue;
            }

            // 程序执行到这里说明该司机仍然是在线的。
            String value = stringRedisTemplate.opsForValue().get(onlineKey);
            if(value == null) {
                // 查询的一瞬间刚好key过期
                continue;
            }

            // 程序走到这里司机也可能下线了，但是对后续的业务没有影响。只不过给这个司机推送订单消息的时候，推送失败了。司机收不到。无所谓。
            // 程序执行到这里能够保证value是有值的。（NPE）
            String[] temp = value.split("#");
            int rangeDistance = Integer.parseInt(temp[0]); // 司机要求离他的距离
            int orderDistance = Integer.parseInt(temp[1]); // 司机要求的订单的距离
            String orientation = temp[2]; // 要求定向接单的坐标。

            // 判断司机与订单起点的距离，是否在司机允许的接单范围内
            boolean distanceOK = dist <= rangeDistance;

            // 判断订单里程是否符合司机的要求
            // orderDistance == 0 表示司机不挑单/不限。其他值表示直接该里程之间的订单。
            boolean mileageOk = false;
            if(orderDistance == 0) { // orderDistance是司机设置的条件：0（不挑单） 5 10 15 30
                mileageOk = true;
            }else if(orderDistance == 5 && mileage > 0 && mileage <= 5){
                mileageOk = true;
            }else if(orderDistance == 10 && mileage > 5 && mileage <= 10){
                mileageOk = true;
            }else if(orderDistance == 15 && mileage > 10 && mileage <= 15){
                mileageOk = true;
            }else if(orderDistance == 30 && mileage > 15 && mileage <= 30){
                mileageOk = true;
            }

            // 判断是否满足定向接单。
            boolean orientationOk = false;
            if(!"none".equals(orientation)){
                // 说明司机设置了定向接单。
                // 定向接单坐标找到。
                double orientationLatitude = Double.parseDouble(orientation.split(",")[0]);
                double orientationLongitude = Double.parseDouble(orientation.split(",")[1]);

                // 把定向点GPS和订单终点GPS都从火星坐标(GCJ02)转换成GPS坐标（WGS84）
                // 腾讯地图返回的是GCJ02加密便宜之后的坐标，直接算距离会有几百米的误差，所以要先转换成WGS84标准。
                // 司机要求的定向点
                double[] location = CoordinateTransform.transformGCJ02ToWGS84(orientationLongitude, orientationLatitude);
                GlobalCoordinates point1 = new GlobalCoordinates(location[1], location[0]);

                // 代驾的终点
                location = CoordinateTransform.transformGCJ02ToWGS84(endPlaceLongitude, endPlaceLatitude);
                GlobalCoordinates point2 = new GlobalCoordinates(location[1], location[0]);

                // 计算两个GPS点的距离。
                GeodeticCurve geoCurve = new GeodeticCalculator().calculateGeodeticCurve(Ellipsoid.WGS84, point1, point2);

                // 司机设置了定向接单，并且定向点和代驾的终点距离在3公里以内，则顺路。
                if(geoCurve.getEllipsoidalDistance() <= 3000){
                    orientationOk = true;
                }

            }else{
                // 司机没有设置定向接单要求。
                orientationOk = true;
            }

            // 三个条件都满足，加入待通知的列表当中。
            if(distanceOK && mileageOk && orientationOk) {
                // 除了将司机的id加入通知列表之外，为什么还要添加一个距离呢？
                // 主要使用在语音播报订单上：距您 x 公里有一单....
                Map<String, Object> map = Map.of("driverId", driverId, "distance", dist);
                list.add(map);
            }

        }

        // 最终返回所有符合条件的司机，这些司机都可以参与接单/抢单。
        return list;
    }

    @Override
    public void updateOrderLocationCache(Map<String, Object> param) {
        Long orderId = MapUtil.getLong(param, "orderId");
        String latitude = MapUtil.getStr(param, "latitude");
        String longitude = MapUtil.getStr(param, "longitude");
        String location = latitude+","+longitude;
        stringRedisTemplate.opsForValue().set("order_location#" + orderId, location, 10, TimeUnit.MINUTES);
    }

    @Override
    public Map<String, Object> searchOrderLocationCache(long orderId) {
        String location = stringRedisTemplate.opsForValue().get("order_location#" + orderId);
        if(location != null){
            String[] temp = location.split(",");
            return Map.of("latitude", temp[0], "longitude", temp[1]);
        }
        return null;
    }
}















