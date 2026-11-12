package com.example.zxds.mis.api.feign;

import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchOrderByPageForm;
import com.example.zxds.mis.api.controller.form.SearchOrderContentForm;
import com.example.zxds.mis.api.controller.form.SearchOrderStatusForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("zxds-odr")
public interface OdrServiceApi {

    @PostMapping("/order/page")
    R searchOrderByPage(@RequestBody SearchOrderByPageForm form);

    @GetMapping("/order/content")
    R searchOrderContent(@SpringQueryMap SearchOrderContentForm form);

    @GetMapping("/order/status")
    R searchOrderStatus(@SpringQueryMap SearchOrderStatusForm form);
}
