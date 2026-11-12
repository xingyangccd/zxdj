package com.example.zxds.bff.driver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.CosUtil;
import com.example.zxds.common.util.OcrUtil;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/ocr")
@RequiredArgsConstructor
@Tag(name = "OcrController", description = "OCR识别web接口")
public class OcrController {
    // OCR识别
    private final OcrUtil ocrUtil;
    // 上传到COS + 数据万象
    private final CosUtil cosUtil;

    // 通用接口：可以识别身份证，驾驶证等...
    @PostMapping
    @Operation(summary = "识别并上传文件")
    //@SaCheckLogin // 测试的时候不添加了，太麻烦，测试完记得放开。
    public R recognizeAndUpload(@RequestParam("file") MultipartFile file,
                                @RequestParam("module") String module,
                                @RequestParam("ocrType") String ocrType) {
        // 判断文件是否为空
        if(file.isEmpty()){
            throw new ZxdsException("文件不能为空");
        }

        Map<String, Object> result = new HashMap<>();

        // 1. OCR识别
        if("1".equals(ocrType)){
            // 身份证正面识别
            JSONObject jsonObject = ocrUtil.recognizeIdCard(file, "front");
            Map<String, String> idCardFrontInfo = ocrUtil.extractFrontInfo(jsonObject);
            // 将身份证正面信息存储到result对象中，返回给前端小程序。
            result.put("idCardFrontInfo", idCardFrontInfo);
        }else if("2".equals(ocrType)){
            JSONObject jsonObject = ocrUtil.recognizeIdCard(file, "back");
            Map<String, String> idCardBackInfo = ocrUtil.extractBackInfo(jsonObject);
            result.put("idCardBackInfo", idCardBackInfo);
        }else if("3".equals(ocrType)){// 如果是驾驶证识别
            JSONObject jsonObject = ocrUtil.recognizeDriverLicenseMainPage(file); // 识别信息返回json对象
            Map<String, String> drcardFrontInfo  = ocrUtil.parseDriverLicenseMainPageInfo(jsonObject); // 通过json对象提取想要的信息
            result.put("drcardFrontInfo", drcardFrontInfo); // 信息存储到Map。
        }

        // 2. 将图片上传到COS以及使用数据万象接口验证图片是否违规。
        try {
            String path = null;
            if("driverAuth".equals(module)){
                // 司机实名认证模块的
                path = "/driver/auth/"; // 这个路径就是存储桶当中的路径。
            } else{
                throw new ZxdsException("module错误");
            }

            // 将文件上传到COS上
            Map<String, Object> map = cosUtil.uploadPrivateFile(file, path); // map中有path，这个path是：/driver/auth/fd1s2a3123f12sda3.jpg

            // 获取路径
            String filePath = String.valueOf(map.get("path"));

            // 将图片的路径保存到result当中
            result.put("filePath", filePath);

            // 立刻生成一个五分钟有效的临时URL，并返回给小程序。
            String tempUrl = cosUtil.getPrivateFileUrl(filePath);
            result.put("tempUrl", tempUrl);

        }catch (Exception e){
            log.error("文件上传到腾讯云失败", e);
            throw new ZxdsException("文件上传到腾讯云失败");
        }

        return R.ok(result);
    }

}



















