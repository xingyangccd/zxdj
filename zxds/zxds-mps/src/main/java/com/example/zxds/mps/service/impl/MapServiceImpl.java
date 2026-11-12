package com.example.zxds.mps.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.mps.service.MapService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MapServiceImpl implements MapService {

    // 预估里程的API地址
    private static final String distanceUrl = "https://apis.map.qq.com/ws/distance/v1/matrix";

    @Value("${tencent.map.key}")
    private String key;

    @Override
    public Map<String, Object> estimateOrderMileageAndMinute(String mode, String startPlaceLatitude, String startPlaceLongitude, String endPlaceLatitude, String endPlaceLongitude) {
        // 起点坐标/终点坐标
        String from = startPlaceLatitude + "," + startPlaceLongitude;
        String to = endPlaceLatitude + "," + endPlaceLongitude;

        // 发送请求（建议使用资源自动管理：try-with-resource语法）
        String body;
        try (HttpResponse response = HttpRequest.post(distanceUrl)
                .form("mode", mode)
                .form("from", from)
                .form("to", to)
                .form("key", key)
                .execute()){
            body = response.body();
        }

        // 解析body，获取结果
        JSONObject json = JSONUtil.parseObj(body);
        // 获取status
        int status = json.getInt("status");
        if(status != 0){
            String message = json.getStr("message");
            log.error("腾讯地图预估里程失败，status={},message={},from={},to={}", status, message, from, to);
            throw new ZxdsException("预估里程异常：" + message);
        }

        // 按照官方的API文档解析数据即可。
        JSONArray rows = json.getJSONObject("result").getJSONArray("rows");
        JSONObject element = rows.getJSONObject(0).getJSONArray("elements").getJSONObject(0);

        // 米为单位
        int distance = element.getInt("distance");
        int duration = element.getInt("duration");

        // 米转换为公里KM，保留两位小数：采用四舍五入
        // 2 表示 保留两位小数
        // RoundingMode.HALF_UP 表示采用四舍五入
        // toPlainString()：转换成普通字符串
        String mileage = BigDecimal.valueOf(distance).divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP).toPlainString();

        // 秒转换成分钟，向上取整
        int minute = BigDecimal.valueOf(duration).divide(BigDecimal.valueOf(60), 0, RoundingMode.CEILING).intValue();

        Map<String, Object> map = new HashMap<>();
        map.put("mileage", mileage);
        map.put("minute", minute);

        return map;
    }


    private static final String directionUrl = "https://apis.map.qq.com/ws/direction/v1/driving/";

    @Override
    public Map<String, Object> calculateDriveLine(String startPlaceLatitude, String startPlaceLongitude, String endPlaceLatitude, String endPlaceLongitude) {

        // 1. 使用urlWithForm拼接并自动做URL编码，避免手动拼接查询参数出错。
        Map<String, Object> params = new HashMap<>();
        params.put("from", startPlaceLatitude + "," + startPlaceLongitude);
        params.put("to", endPlaceLatitude + "," + endPlaceLongitude);
        params.put("key", key);

        // 获取最终的URL。
        // 最后的false表示：会对参数做URL编码【参数值可能含有中文】，但不是对整个URL编码  url?name=value&name=value
        String url = HttpUtil.urlWithForm(directionUrl, params, StandardCharsets.UTF_8, false);

        // 2. try-with-resource语法
        try (HttpResponse response = HttpRequest.get(url).execute()){
            JSONObject json = JSONUtil.parseObj(response.body());
            int status = json.getInt("status");
            if(status != 0){
                throw new ZxdsException("腾讯地图路线规划失败，status=" + status + ", message=" + json.getStr("message"));
            }
            JSONObject result = json.getJSONObject("result");
            return result.toBean(Map.class);
        }
    }
}

















