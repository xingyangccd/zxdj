package com.example.zxds.mis.api.feign;

import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchCustomerBriefInfoForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "zxds-cst")
public interface CstServiceApi {
    @GetMapping("/customer/brief-info")
    R searchCustomerBriefInfo(@SpringQueryMap SearchCustomerBriefInfoForm form);
}