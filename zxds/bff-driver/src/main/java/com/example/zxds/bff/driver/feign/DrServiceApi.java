package com.example.zxds.bff.driver.feign;

import com.example.zxds.bff.driver.controller.form.*;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign客户端，调用司机子服务的web接口
 */
@FeignClient("zxds-dr")
public interface DrServiceApi {

    /**
     * 发送请求调用司机子服务的web接口，完成新司机的注册。
     */
    @PostMapping("/driver")
    R registerNewDriver(@RequestBody RegisterNewDriverForm form);

    @PutMapping("/driver")
    R updateDriverAuth(@RequestBody UpdateDriverAuthForm form);

    @PostMapping("/driver/face-model")
    R createDriverFaceModel(@RequestBody CreateDriverFaceModelForm form);

    @PostMapping("/driver/token")
    R login(@RequestBody LoginForm form);

    @PostMapping("/driver/base-info")
    R searchDriverBaseInfo(@RequestBody SearchDriverBaseInfoForm form);

    @PostMapping("/settings/driver")
    R searchDriverSettings(@RequestBody SearchDriverSettingsForm form);

    @PostMapping("/driver/auth")
    R searchDriverAuth(@RequestBody SearchDriverAuthForm form);
}
