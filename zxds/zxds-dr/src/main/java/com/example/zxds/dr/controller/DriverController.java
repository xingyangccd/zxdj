package com.example.zxds.dr.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.common.util.R;
import com.example.zxds.dr.controller.form.*;
import com.example.zxds.dr.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/driver")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "DriverController", description = "司机模块web接口")
public class DriverController {
    private final DriverService driverService;

    @PostMapping
    @Operation(summary = "新司机注册")
    public R registerNewDriver(@RequestBody @Valid RegisterNewDriverForm form){
        Map<String, Object> params = BeanUtil.beanToMap(form);
        String driverId = driverService.registerNewDriver(params);
        return R.ok().put("driverId", driverId);
    }

    @PutMapping
    @Operation(summary = "更新时机实名认证信息")
    public R updateDriverAuth(@RequestBody @Valid UpdateDriverAuthForm form){
        Map<String, Object> param = BeanUtil.beanToMap(form);
        int rows = driverService.updateDriverAuth(param);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/face-model")
    @Operation(summary = "创建司机人脸模型归档")
    public R createDriverFaceModel(@RequestBody @Valid CreateDriverFaceModelForm form) {
        String result = driverService.createDriverFaceModel(form.getDriverId(), form.getPhoto());
        return R.ok().put("result", result);
    }

    @PostMapping("/token")
    @Operation(summary = "司机登录")
    public R login(@RequestBody @Valid LoginForm form) {
        Map<String, Object> map = driverService.login(form.getCode());
        return R.ok().put("result", map);
    }

    @PostMapping("/base-info")
    @Operation(summary = "查询司机基本信息")
    public R searchDriverBaseInfo(@RequestBody @Valid SearchDriverBaseInfoForm form) {
        Map<String, Object> result = driverService.searchDriverBaseInfo(form.getDriverId());
        return R.ok().put("result", result);
    }

    @GetMapping("/drivers")
    @Operation(summary = "分页查询司机记录")
    public R searchDriverByPage(@Valid SearchDriverByPageForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        Integer pageIndex = form.getPageIndex();
        Integer pageSize = form.getPageSize();
        Integer startIndex = (pageIndex - 1) * pageSize;
        param.put("startIndex", startIndex);
        PageUtils pageUtils = driverService.searchDriverByPage(param);
        return R.ok().put("result", pageUtils);
    }

    @PostMapping("/auth")
    @Operation(summary = "查询司机认证信息")
    public R searchDriverAuth(@RequestBody @Valid SearchDriverAuthForm form) {
        Map<String, Object> result = driverService.searchDriverAuth(form.getDriverId());
        return R.ok().put("result", result);
    }

    @PostMapping("/real-summary")
    @Operation(summary = "查询司机实名信息摘要")
    public R searchDriverRealSummary(@RequestBody @Valid SearchDriverRealSummaryForm form) {
        Map<String, Object> map = driverService.searchDriverRealSummary(form.getDriverId());
        return R.ok().put("result", map);
    }

    @PutMapping("/real-auth")
    @Operation(summary = "更新时机实名认证状态")
    public R updateDriverRealAuth(@RequestBody @Valid UpdateDriverRealAuthForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        int rows = driverService.updateDriverRealAuth(param);
        return R.ok().put("rows", rows);
    }

    @GetMapping("/brief-info")
    @Operation(summary = "查询司机简明信息")
    public R searchDriverBriefInfo(@Valid SearchDriverBriefInfoForm form) {
        Map<String, Object> map = driverService.searchDriverBriefInfo(form.getDriverId());
        return R.ok().put("result", map);
    }

    @GetMapping("/open-id")
    @Operation(summary = "查询司机的OpenId")
    public R searchDriverOpenId(@Valid SearchDriverOpenIdForm form) {
        String openId = driverService.searchDriverOpenId(form.getDriverId());
        return R.ok().put("result", openId);
    }

}
