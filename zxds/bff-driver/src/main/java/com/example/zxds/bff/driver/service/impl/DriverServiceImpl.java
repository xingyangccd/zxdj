package com.example.zxds.bff.driver.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.example.zxds.bff.driver.controller.form.*;
import com.example.zxds.bff.driver.feign.DrServiceApi;
import com.example.zxds.bff.driver.feign.OdrServiceApi;
import com.example.zxds.bff.driver.service.DriverService;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.CosUtil;
import com.example.zxds.common.util.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DrServiceApi drServiceApi;
    private final OdrServiceApi odrServiceApi;
    private final CosUtil cosUtil;

    // 正常来说，这里是控制分布式事务的，但不需要添加@GlobalTransactional，因为这里的服务调用是单一的。
    // 直接使用底层的子服务的本地事务即可。
    @Override
    public String registerNewDriver(RegisterNewDriverForm form) {
        R r = drServiceApi.registerNewDriver(form);
        Object driverId = r.get("driverId");
        if (driverId == null) {
            throw new ZxdsException("司机注册失败");
        }
        return String.valueOf(driverId);
    }

    @Override
    public int updateDriverAuth(UpdateDriverAuthForm form) {
        R r = drServiceApi.updateDriverAuth(form);
        return Convert.toInt(r.get("rows"));
    }

    @Override
    public String createDriverFaceModel(CreateDriverFaceModelForm form) {
        R r = drServiceApi.createDriverFaceModel(form);
        return MapUtil.getStr(r, "result");
    }

    @Override
    public Map<String, Object> login(LoginForm form) {
        R r = drServiceApi.login(form);
        return (Map<String, Object>) r.get("result");
    }

    // 思考：service方法的返回值为什么不能直接是R？？？？
    @Override
    public Map<String, Object> searchDriverBaseInfo(SearchDriverBaseInfoForm form) {
        R r = drServiceApi.searchDriverBaseInfo(form);
        return (Map<String, Object>) r.get("result");
    }

    @Override
    public Map<String, Object> searchWorkbenchData(long driverId) {
        // 1.查询今日营业数据
        SearchDriverTodayBusinessDataForm businessForm = new SearchDriverTodayBusinessDataForm();
        businessForm.setDriverId(driverId);
        R businessR = odrServiceApi.searchDriverTodayBusinessData(businessForm);
        Map<String, Object> business = (Map<String, Object>) businessR.get("result");

        // 2.查询司机设置
        SearchDriverSettingsForm settingsForm = new SearchDriverSettingsForm();
        settingsForm.setDriverId(driverId);
        R settingsR = drServiceApi.searchDriverSettings(settingsForm);
        Map<String, Object> settings = (Map<String, Object>) settingsR.get("result");

        // 3.组装Map集合并返回
        Map<String, Object> result = new HashMap<>();
        result.put("business", business);
        result.put("settings", settings);

        return result;
    }

    // 将下游返回的司机实名认证信息中的所有图片生成5分钟之内有效的外网访问地址。
    @Override
    public Map<String, Object> searchDriverAuth(SearchDriverAuthForm form) {
        R r = drServiceApi.searchDriverAuth(form);
        Map<String, Object> map = (Map<String, Object>)r.get("result");

        // 向map集合中添加 6 张外网能够访问的临时URL。
        map.put("idcardFrontUrl", tempUrl(map, "idcardFront"));
        map.put("idcardBackUrl", tempUrl(map, "idcardBack"));
        map.put("idcardHoldingUrl", tempUrl(map, "idcardHolding"));
        map.put("drcardFrontUrl", tempUrl(map, "drcardFront"));
        map.put("drcardBackUrl", tempUrl(map, "drcardBack"));
        map.put("drcardHoldingUrl", tempUrl(map, "drcardHolding"));

        return map;
    }

    private String tempUrl(Map<String, Object> map, String key){
        // path是存储桶中的路径。
        String path = MapUtil.getStr(map, key);
        // 获取一个5分钟有效的临时URL
        return StrUtil.isNotBlank(path) ? cosUtil.getPrivateFileUrl(path) : "";
    }

}
