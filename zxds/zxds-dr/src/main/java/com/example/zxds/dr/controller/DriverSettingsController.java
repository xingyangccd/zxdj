package com.example.zxds.dr.controller;

import com.example.zxds.common.util.R;
import com.example.zxds.dr.controller.form.SearchDriverSettingsForm;
import com.example.zxds.dr.service.DriverSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settings")
@Slf4j
@Tag(name = "DriverSettingsController", description = "司机设置模块web接口")
public class DriverSettingsController {
    private final DriverSettingsService driverSettingsService;

    @PostMapping("/driver")
    @Operation(summary = "查询司机的设置")
    public R searchDriverSettings(@RequestBody @Valid SearchDriverSettingsForm form) {
        Map<String, Object> map = driverSettingsService.searchDriverSettings(form.getDriverId());
        return R.ok().put("result", map);
    }
}
