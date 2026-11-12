package com.example.zxds.nebula.controller;

import cn.hutool.core.util.StrUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.R;
import com.example.zxds.nebula.controller.form.InsertOrderMonitoringForm;
import com.example.zxds.nebula.service.MonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/monitoring")
@Tag(name = "MonitoringController", description = "监控服务的Web接口")
@Slf4j
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    @PostMapping(value = "/uploadRecordFile")
    @Operation(summary = "上传代驾录音文件")
    public R uploadRecordFile(@RequestPart("file") MultipartFile file,
                              @RequestParam("name") String name,
                              @RequestParam(value = "text", required = false) String text) {
        // 1. 基础校验
        if (file.isEmpty()) {
            throw new ZxdsException("录音文件不能为空");
        }
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new ZxdsException("录音文件不能超过 50MB");
        }
        if (StrUtil.isBlank(name)) {
            throw new ZxdsException("文件名不能为空");
        }

        // 2. 后缀校验（兼容大小写）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp3")) {
            throw new ZxdsException("仅支持 MP3 格式的录音文件");
        }

        // 3. 调用 Service
        monitoringService.monitoring(file, name, text);
        return R.ok();
    }

    @PostMapping("/orders")
    @Operation(summary = "添加订单监控摘要记录")
    public R insertOrderMonitoring(@RequestBody @Valid InsertOrderMonitoringForm form) {
        int rows = monitoringService.insertOrderMonitoring(form.getOrderId());
        return R.ok().put("rows", rows);
    }

}