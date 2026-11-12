package com.example.zxds.bff.customer.feign;

import com.example.zxds.bff.customer.controller.form.SearchDriverBriefInfoForm;
import com.example.zxds.bff.customer.controller.form.SearchDriverOpenIdForm;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "zxds-dr")
public interface DrServiceApi {

    @GetMapping("/driver/brief-info")
    R searchDriverBriefInfo(@SpringQueryMap SearchDriverBriefInfoForm form);

    @GetMapping("/driver/open-id")
    R searchDriverOpenId(@SpringQueryMap SearchDriverOpenIdForm form);
}