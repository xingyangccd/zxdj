package com.example.zxds.mis.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchDriverByPageForm;
import com.example.zxds.mis.api.controller.form.SearchDriverComprehensiveDataForm;
import com.example.zxds.mis.api.controller.form.UpdateDriverRealAuthForm;
import com.example.zxds.mis.api.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/driver")
@Tag(name = "DriverController", description = "司机管理Web接口")
@RequiredArgsConstructor
@Slf4j
public class DriverController {
    private final DriverService driverService;

    @GetMapping("/drivers")
    @SaCheckPermission(value = {"ROOT", "DRIVER:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "分页查询司机记录")
    public R searchDriverByPage(@Valid SearchDriverByPageForm form) {
        PageUtils pageUtils = driverService.searchDriverByPage(form);
        return R.ok().put("result", pageUtils);
    }

    @PostMapping("/comprehensive-data")
    @SaCheckPermission(value = {"ROOT", "DRIVER:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "查询司机综合数据")
    public R searchDriverComprehensiveData(@RequestBody @Valid SearchDriverComprehensiveDataForm form) {
        Map<String, Object> map = driverService.searchDriverComprehensiveData(form.getRealAuth(), form.getDriverId());
        return R.ok().put("result", map);
    }

    @PutMapping("/real-auth")
    @SaCheckPermission(value = {"ROOT", "DRIVER:UPDATE"}, mode = SaMode.OR)
    @Operation(summary = "更新司机实名认证状态")
    public R updateDriverRealAuth(@RequestBody @Valid UpdateDriverRealAuthForm form) {
        int rows = driverService.updateDriverRealAuth(form);
        return R.ok().put("rows", rows);
    }
}
