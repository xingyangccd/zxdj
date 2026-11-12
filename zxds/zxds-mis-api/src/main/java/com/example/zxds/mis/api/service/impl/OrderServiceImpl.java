package com.example.zxds.mis.api.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.*;
import com.example.zxds.mis.api.feign.*;
import com.example.zxds.mis.api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OdrServiceApi odrServiceApi;
    private final CstServiceApi cstServiceApi;
    private final DrServiceApi drServiceApi;
    private final RuleServiceApi ruleServiceApi;
    private final MpsServiceApi mpsServiceApi;
    private final NebulaServiceApi nebulaServiceApi;

    @Override
    public PageUtils searchOrderByPage(SearchOrderByPageForm form) {
        R r = odrServiceApi.searchOrderByPage(form);
        Map<String, Object> map = (Map<String, Object>) r.get("result");
        return BeanUtil.toBean(map, PageUtils.class);
    }

    @Override
    public Map<String, Object> searchOrderComprehensiveInfo(long orderId) {
        Map<String, Object> resultMap = new HashMap<>();

        // 1.查询订单的核心内容。
        SearchOrderContentForm contentForm = new SearchOrderContentForm();
        contentForm.setOrderId(orderId);
        R r = odrServiceApi.searchOrderContent(contentForm);
        if(!r.containsKey("result") || r.get("result") == null) {
            throw new ZxdsException("不存在订单记录");
        }
        Map<String, Object> content = (Map<String, Object>)r.get("result");
        resultMap.put("content", content);

        // 2.查询客户信息
        SearchCustomerBriefInfoForm customerForm = new SearchCustomerBriefInfoForm();
        customerForm.setCustomerId(MapUtil.getLong(content, "customerId"));
        r = cstServiceApi.searchCustomerBriefInfo(customerForm);
        resultMap.put("customerInfo", r.get("result"));

        // 3.查询司机信息
        if(content.containsKey("driverId")){
            SearchDriverBriefInfoForm driverForm = new SearchDriverBriefInfoForm();
            driverForm.setDriverId(MapUtil.getLong(content, "driverId"));
            r = drServiceApi.searchDriverBriefInfo(driverForm);
            resultMap.put("driverInfo", r.get("result"));
        }

        // 4.查询费用规则
        if(content.containsKey("chargeRuleId")){
            SearchChargeRuleByIdForm chargeForm = new SearchChargeRuleByIdForm();
            chargeForm.setRuleId(MapUtil.getLong(content, "chargeRuleId"));
            r = ruleServiceApi.searchChargeRuleById(chargeForm);
            resultMap.put("chargeRule", r.get("result"));
        }

        // 5.查询取消规则
        if(content.containsKey("cancelRuleId")){
            SearchCancelRuleByIdForm cancelForm = new SearchCancelRuleByIdForm();
            cancelForm.setRuleId(MapUtil.getLong(content, "cancelRuleId"));
            r = ruleServiceApi.searchCancelRuleById(cancelForm);
            resultMap.put("cancelRule", r.get("result"));
        }

        // 6.查询分账规则
        if(content.containsKey("profitSharingRuleId")){
            SearchProfitSharingRuleByIdForm profitForm = new SearchProfitSharingRuleByIdForm();
            profitForm.setRuleId(MapUtil.getLong(content, "profitSharingRuleId"));
            r = ruleServiceApi.searchProfitSharingRuleById(profitForm);
            resultMap.put("profitSharingRule", r.get("result"));
        }

        // 7.查询GPS，计算最佳线路
        Map<String, Object> startLoc = (Map<String, Object>)content.get("startPlaceLocation");
        Map<String, Object> endLoc = (Map<String, Object>)content.get("endPlaceLocation");
        CalculateDriveLineForm lineForm = new CalculateDriveLineForm();
        lineForm.setStartPlaceLatitude(MapUtil.getStr(startLoc, "latitude"));
        lineForm.setStartPlaceLongitude(MapUtil.getStr(startLoc, "longitude"));
        lineForm.setEndPlaceLatitude(MapUtil.getStr(endLoc, "latitude"));
        lineForm.setEndPlaceLongitude(MapUtil.getStr(endLoc, "longitude"));
        r = mpsServiceApi.calculateDriveLine(lineForm);
        resultMap.put("driveLine", r.get("result"));

        // 8.根据订单的状态查询GPS定位记录（订单进行中，订单已完成）
        int status = MapUtil.getInt(content, "status");
        if(status >= 5 && status <= 8){
            // 代驾结束了
            SearchOrderGpsForm gpsForm = new SearchOrderGpsForm();
            gpsForm.setOrderId(orderId);
            r = nebulaServiceApi.searchOrderGps(gpsForm);
            resultMap.put("orderGps", r.get("result"));
        }else if(status == 4){
            // 正在代驾
            SearchOrderLastGpsForm lastGpsForm = new SearchOrderLastGpsForm();
            lastGpsForm.setOrderId(orderId);
            r = nebulaServiceApi.searchOrderLastGps(lastGpsForm);
            resultMap.put("lastGps", r.get("result"));
        }

        return resultMap;
    }

    @Override
    public Map<String, Object> searchOrderLastGps(SearchOrderLastGpsForm form) {
        // 1.查询订单状态
        SearchOrderStatusForm statusForm = new SearchOrderStatusForm();
        statusForm.setOrderId(form.getOrderId());
        R r = odrServiceApi.searchOrderStatus(statusForm);

        if(r == null || !r.containsKey("result") || r.get("result") == null){
            throw new ZxdsException("没有对应的订单记录");
        }

        int status = MapUtil.getInt(r, "result");

        // 2.状态是4：代驾进行中，这个时候才需要去获取最新的GPS定位
        if(status == 4){
            r = nebulaServiceApi.searchOrderLastGps(form);
            if(r != null && r.containsKey("result")){
                return (Map<String, Object>) r.get("result");
            }
        }
        return null;
    }
}

















