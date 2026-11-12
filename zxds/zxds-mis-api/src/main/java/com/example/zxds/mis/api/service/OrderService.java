package com.example.zxds.mis.api.service;

import com.example.zxds.common.util.PageUtils;
import com.example.zxds.mis.api.controller.form.SearchOrderByPageForm;
import com.example.zxds.mis.api.controller.form.SearchOrderLastGpsForm;

import java.util.Map;

public interface OrderService {

    PageUtils searchOrderByPage(SearchOrderByPageForm form);

    /**
     * 查询订单的综合信息。
     */
    Map<String, Object> searchOrderComprehensiveInfo(long orderId);

    Map<String, Object> searchOrderLastGps(SearchOrderLastGpsForm form);
}
