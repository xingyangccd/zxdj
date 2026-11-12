package com.example.zxds.nebula.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.example.zxds.common.util.SqlEscapeUtils;
import com.example.zxds.nebula.controller.dto.InsertOrderGpsDTO;
import com.example.zxds.nebula.db.dao.OrderGpsDao;
import com.example.zxds.nebula.db.pojo.OrderGpsEntity;
import com.example.zxds.nebula.service.OrderGpsService;
import com.example.zxds.nebula.util.LocationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderGpsServiceImpl implements OrderGpsService {

    private final OrderGpsDao orderGpsDao;

    // 说明：HBase + Phoenix + MyBatis 没有提供这种批处理的方式，只能一个一个插入。
    @Override
    public int insertOrderGps(List<InsertOrderGpsDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return 0;
        }
        int rows = 0;
        for (InsertOrderGpsDTO dto : list) {
            // 将dto转换成entity对象（Dao方法需要entity）
            OrderGpsEntity entity = BeanUtil.copyProperties(dto, OrderGpsEntity.class);
            // 防止SQL注入。
            entity.setLatitude(SqlEscapeUtils.escape(entity.getLatitude()));
            entity.setLongitude(SqlEscapeUtils.escape(entity.getLongitude()));
            entity.setSpeed(SqlEscapeUtils.escape(entity.getSpeed()));

            rows += orderGpsDao.insert(entity);
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> searchOrderGps(long orderId) {
        return orderGpsDao.searchOrderGps(orderId);
    }

    @Override
    public Map<String, Object> searchOrderLastGps(long orderId) {
        return orderGpsDao.searchOrderLastGps(orderId);
    }

    @Override
    public String calculateOrderMileage(long orderId) {
        // 这个订单所有的GPS点。
        List<Map<String, Object>> list = orderGpsDao.searchOrderAllGps(orderId);
        if(list == null || list.size() < 2){
            // 没有任何行进轨迹，或者只有一个GPS点，里程 0
            return "0.000";
        }

        double totalMileage = 0.0;

        for (int i = 0; i < list.size() - 1; i++) {
            Map<String, Object> map1 = list.get(i);
            Map<String, Object> map2 = list.get(i + 1);

            Double latitude1 = MapUtil.getDouble(map1, "latitude");
            Double longitude1 = MapUtil.getDouble(map1, "longitude");
            Double latitude2 = MapUtil.getDouble(map2, "latitude");
            Double longitude2 = MapUtil.getDouble(map2, "longitude");

            if(latitude1 == null || longitude1 == null || latitude2 == null || longitude2 == null){
                continue;
            }

            double distance = LocationUtil.getDistance(latitude1, longitude1, latitude2, longitude2);

            // 过滤 GPS 漂移（距离太近忽略）
            if(distance < 0.005) { // 当计算两个GPS点之间的距离小于5的时候，我认为原地没动
                continue;
            }

            totalMileage += distance;
        }

        return String.valueOf(LocationUtil.formatDistance(totalMileage));
    }
}
















