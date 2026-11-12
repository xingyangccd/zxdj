package com.example.zxds.bff.driver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import com.example.zxds.bff.driver.controller.form.*;
import com.example.zxds.bff.driver.service.DriverLocationService;
import com.example.zxds.bff.driver.service.DriverService;
import com.example.zxds.bff.driver.service.NewOrderMessageService;
import com.example.zxds.common.util.R;
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
@Tag(name = "DriverController", description = "BFF-司机模块web接口")
public class DriverController {
    private final DriverService driverService;
    private final DriverLocationService driverLocationService;
    private final NewOrderMessageService newOrderMessageService;

    @PostMapping
    @Operation(summary = "新司机注册")
    public R registerNewDriver(@RequestBody @Valid RegisterNewDriverForm form) {

        // 注册司机
        String driverId = driverService.registerNewDriver(form);

        // 程序能够到这里一定是注册成功了。
        // satoken颁发令牌
        StpUtil.login(driverId);

        // 获取token令牌
        String token = StpUtil.getTokenInfo().getTokenValue();

        // 返回给前端小程序
        return R.ok().put("token", token);
    }

    @PutMapping
    @Operation(summary = "更新实名认证信息")
    @SaCheckLogin
    public R updateDriverAuth(@RequestBody @Valid UpdateDriverAuthForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        int rows = driverService.updateDriverAuth(form);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/face-model")
    @Operation(summary = "创建司机人脸模型档案")
    @SaCheckLogin
    public R createDriverFaceModel(@RequestBody @Valid CreateDriverFaceModelForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        String result = driverService.createDriverFaceModel(form);
        return R.ok().put("result", result);
    }

    @PostMapping("/token")
    @Operation(summary = "司机登录")
    public R login(@RequestBody @Valid LoginForm form) {
        Map<String, Object> map = driverService.login(form);
        if(map == null){
            return R.ok();
        }

        long driverId = MapUtil.getLong(map, "id");
        byte realAuth = Byte.parseByte(MapUtil.getStr(map, "realAuth"));
        boolean archive = MapUtil.getBool(map, "archive");

        // satoken 登录，生成token
        StpUtil.login(driverId);
        String token = StpUtil.getTokenInfo().getTokenValue();

        return R.ok().put("token", token).put("realAuth", realAuth).put("archive", archive);
    }

    @PostMapping("/logout")
    @Operation(summary = "司机退出登录")
    @SaCheckLogin
    public R logout() {
        StpUtil.logout();
        return R.ok();
    }

    @PostMapping("/base-info")
    @Operation(summary = "查询司机基本信息")
    @SaCheckLogin
    public R searchDriverBaseInfo() {
        long driverId = StpUtil.getLoginIdAsLong();
        SearchDriverBaseInfoForm form = new SearchDriverBaseInfoForm();
        form.setDriverId(driverId);
        Map<String, Object> map = driverService.searchDriverBaseInfo(form);
        return R.ok().put("result", map);
    }

    @PostMapping("/workbench-data")
    @Operation(summary = "查询司机工作台数据")
    @SaCheckLogin
    public R searchWorkbenchData() {
        long driverId = StpUtil.getLoginIdAsLong();
        Map<String, Object> map = driverService.searchWorkbenchData(driverId);
        return R.ok().put("result", map);
    }

    @GetMapping("/auth")
    @Operation(summary = "查询司机认证信息")
    @SaCheckLogin
    public R searchDriverAuth() {
        long driverId = StpUtil.getLoginIdAsLong();
        SearchDriverAuthForm form = new SearchDriverAuthForm();
        form.setDriverId(driverId);
        Map<String, Object> map = driverService.searchDriverAuth(form);
        return R.ok().put("result", map);
    }


    @PostMapping("/work/start")
    @Operation(summary = "开始接单")
    @SaCheckLogin
    public R startWork(){
        long driverId = StpUtil.getLoginIdAsLong();
        // 清空GEO缓存、清空上线缓存、清空消息队列
        clearDriverCacheAndQueue(driverId);
        return R.ok();
    }

    @PostMapping("/work/stop")
    @Operation(summary = "停止接单")
    @SaCheckLogin
    public R stopWork(){
        long driverId = StpUtil.getLoginIdAsLong();
        clearDriverCacheAndQueue(driverId);
        return R.ok();
    }

    // 清空司机上线缓存，GEO缓存，清空消息队列
    private void clearDriverCacheAndQueue(long driverId) {
        // 删除司机上线缓存和GEO缓存
        RemoveLocationCacheForm removeForm = new RemoveLocationCacheForm();
        removeForm.setDriverId(driverId);
        driverLocationService.removeLocationCache(removeForm);

        // 清空司机的消息队列。
        ClearNewOrderQueueForm clearForm = new ClearNewOrderQueueForm();
        clearForm.setUserId(driverId);
        newOrderMessageService.clearNewOrderQueue(clearForm);
    }


}
