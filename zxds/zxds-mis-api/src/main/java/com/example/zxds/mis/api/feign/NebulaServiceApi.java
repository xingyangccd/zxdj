package com.example.zxds.mis.api.feign;

import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchOrderGpsForm;
import com.example.zxds.mis.api.controller.form.SearchOrderLastGpsForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "zxds-nebula")
public interface NebulaServiceApi {
    @GetMapping("/order/gps/order-gps")
    R searchOrderGps(@SpringQueryMap SearchOrderGpsForm form);

    @GetMapping("/order/gps/order-gps/last")
    R searchOrderLastGps(@SpringQueryMap SearchOrderLastGpsForm form);
}