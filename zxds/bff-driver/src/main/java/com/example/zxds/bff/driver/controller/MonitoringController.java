package com.example.zxds.bff.driver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.util.StrUtil;
import com.example.zxds.bff.driver.feign.NebulaServiceApi;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/monitoring")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "MonitoringController", description = "订单监控服务的Web接口")
public class MonitoringController {
    private final NebulaServiceApi nebulaServiceApi;

    @PostMapping(value = "/uploadRecordFile")
    @Operation(summary = "上传代驾录音文件")
    @SaCheckLogin
    public R uploadRecordFile(@RequestPart("file") MultipartFile file,
                              @RequestParam("name") String name,
                              @RequestParam(value = "text", required = false) String text) {
        // 1.校验
        if(file.isEmpty()){
            throw new ZxdsException("上传文件不能为空");
        }
        if(file.getSize() > 50 * 1024 * 1024){
            throw new ZxdsException("录音文件不能超过50MB");
        }
        if(StrUtil.isBlank(name)){
            throw new ZxdsException("文件名不能为空");
        }

        // 2. 校验文件名必须是mp3
        String originalFilename = file.getOriginalFilename();
        if(StrUtil.isBlank(originalFilename) || !originalFilename.toLowerCase().endsWith(".mp3")){
            throw new ZxdsException("仅支持MP3格式的文件");
        }

        // 3. 调用下游的 Feign
        R result = nebulaServiceApi.uploadRecordFile(file, name, text);
        // result本身是空的或者返回的业务code码不是200
        if(result == null || !Integer.valueOf(200).equals(result.get("code"))) {
            log.error("调用下游Feign上传录音失败，name={},result={}", name, result);
            throw new ZxdsException("上传录音失败");
        }

        return R.ok();
    }
}

















