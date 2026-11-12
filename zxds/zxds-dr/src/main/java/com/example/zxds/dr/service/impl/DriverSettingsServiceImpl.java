package com.example.zxds.dr.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.zxds.dr.db.dao.DriverSettingsDao;
import com.example.zxds.dr.service.DriverSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriverSettingsServiceImpl implements DriverSettingsService {

    private final DriverSettingsDao driverSettingsDao;

    @Override
    public Map<String, Object> searchDriverSettings(long driverId) {
        // 查询司机的设置
        String settings = driverSettingsDao.searchDriverSettings(driverId);
        if(StrUtil.isBlank(settings)){
            return new HashMap<>();
        }

        // 将json字符串转换成Map对象
        Map<String, Object> map = JSONUtil.parseObj(settings).toBean(Map.class);

        // listenService / autoAccept：数据库中存储的是0或1，返回给前端true/false
        map.put("listenService", MapUtil.getInt(map, "listenService") == 1);
        map.put("autoAccept", MapUtil.getInt(map, "autoAccept") == 1);

        return map;
    }
}
