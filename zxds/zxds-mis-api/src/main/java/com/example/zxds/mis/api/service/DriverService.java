package com.example.zxds.mis.api.service;

import com.example.zxds.common.util.PageUtils;
import com.example.zxds.mis.api.controller.form.SearchDriverByPageForm;
import com.example.zxds.mis.api.controller.form.UpdateDriverRealAuthForm;

import java.util.Map;

public interface DriverService {
    PageUtils searchDriverByPage(SearchDriverByPageForm form);

    // 参数realAuth的作用：如果realAuth=1表示司机根本没有提交任何资料，不需要进行审核。
    Map<String, Object> searchDriverComprehensiveData(byte realAuth, Long driverId);

    int updateDriverRealAuth(UpdateDriverRealAuthForm form);
}
