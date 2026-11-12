package com.example.zxds.mis.api.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.example.zxds.common.util.CosUtil;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchDriverByPageForm;
import com.example.zxds.mis.api.controller.form.SearchDriverRealSummaryForm;
import com.example.zxds.mis.api.controller.form.UpdateDriverRealAuthForm;
import com.example.zxds.mis.api.feign.DrServiceApi;
import com.example.zxds.mis.api.service.DriverService;
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
    private final CosUtil cosUtil;

    @Override
    public PageUtils searchDriverByPage(SearchDriverByPageForm form) {
        R r = drServiceApi.searchDriverByPage(form);

        // 不要这样写代码：这里可能会发生 ClassCastException
        //PageUtils pageUtils = (PageUtils) r.get("result");

        // 通过这种方式安全的返回了 PageUtils 对象。
        Map<String,Object> map = (Map<String, Object>)r.get("result");
        return BeanUtil.toBean(map, PageUtils.class);
    }

    @Override
    public Map<String, Object> searchDriverComprehensiveData(byte realAuth, Long driverId) {
        Map<String, Object> map = new HashMap<>();
        if(realAuth != 2 && realAuth != 3){
            return map;
        }
        // 1. 查询司机的实名信息
        SearchDriverRealSummaryForm form = new SearchDriverRealSummaryForm();
        form.setDriverId(driverId);
        R r = drServiceApi.searchDriverRealSummary(form);
        Map<String, Object> summaryMap = (Map<String, Object>) r.get("result");

        // 2. 把证件路径换成私有文件的临时访问URL
        String[] keys = {"idcardFront","idcardBack","idcardHolding","drcardFront","drcardBack","drcardHolding"};
        for (String key : keys) {
            // 存储桶中的路径
            String path = MapUtil.getStr(summaryMap, key);
            // 生成临时 路径
            summaryMap.put(key, StrUtil.isNotBlank(path) ? cosUtil.getPrivateFileUrl(path) : "");
        }
        map.put("summaryMap", summaryMap);

        // TODO 后续还有更多的信息需要补充

        return map;
    }

    @Override
    public int updateDriverRealAuth(UpdateDriverRealAuthForm form) {
        R r = drServiceApi.updateDriverRealAuth(form);
        int rows = MapUtil.getInt(r, "rows");

        // TODO: 调用消息子系统，给司机发送实名认证结果通知

        return rows;
    }
}
