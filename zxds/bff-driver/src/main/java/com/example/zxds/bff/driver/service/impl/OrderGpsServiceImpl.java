package com.example.zxds.bff.driver.service.impl;

import cn.hutool.core.map.MapUtil;
import com.example.zxds.bff.driver.controller.form.InsertOrderGpsForm;
import com.example.zxds.bff.driver.feign.NebulaServiceApi;
import com.example.zxds.bff.driver.service.OrderGpsService;
import com.example.zxds.common.util.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderGpsServiceImpl implements OrderGpsService {

    private final NebulaServiceApi nebulaServiceApi;

    @Override
    public int insertOrderGps(InsertOrderGpsForm form) {
        R r = nebulaServiceApi.insertOrderGps(form);
        return MapUtil.getInt(r, "rows");
    }
}
