package com.example.zxds.mis.api.feign;

import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.CalculateDriveLineForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "zxds-mps")
public interface MpsServiceApi {

    @GetMapping("/map/drive-lines")
    R calculateDriveLine(@SpringQueryMap CalculateDriveLineForm form);
}