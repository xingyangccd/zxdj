package com.example.zxds.mis.api.feign;

import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchDriverBriefInfoForm;
import com.example.zxds.mis.api.controller.form.SearchDriverByPageForm;
import com.example.zxds.mis.api.controller.form.SearchDriverRealSummaryForm;
import com.example.zxds.mis.api.controller.form.UpdateDriverRealAuthForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("zxds-dr")
public interface DrServiceApi {

    // @SpringQueryMap 作用：如果是GET请求，需要通过查询参数提交数据，需要添加这个注解。
    @GetMapping("/driver/drivers")
    R searchDriverByPage(@SpringQueryMap SearchDriverByPageForm form);

    @PostMapping("/driver/real-summary")
    R searchDriverRealSummary(@RequestBody SearchDriverRealSummaryForm form);

    @PutMapping("/driver/real-auth")
    R updateDriverRealAuth(@RequestBody UpdateDriverRealAuthForm form);

    @GetMapping("/driver/brief-info")
    R searchDriverBriefInfo(@SpringQueryMap SearchDriverBriefInfoForm form);

}
