package com.example.zxds.bff.driver.feign;

import com.example.zxds.bff.driver.controller.form.SearchCustomerInfoInOrderForm;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "zxds-cst")
public interface CstServiceApi {

    @GetMapping("/customer/info")
    R searchCustomerInfoInOrder(@SpringQueryMap SearchCustomerInfoInOrderForm form);
}