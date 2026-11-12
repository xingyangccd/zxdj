package com.example.zxds.bff.driver.feign;

import com.example.zxds.bff.driver.config.MultipartSupportConfig;
import com.example.zxds.bff.driver.controller.form.CalculateOrderMileageForm;
import com.example.zxds.bff.driver.controller.form.InsertOrderGpsForm;
import com.example.zxds.bff.driver.controller.form.InsertOrderMonitoringForm;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// 指定Feign客户端使用自定义的配置类：MultipartSupportConfig
// 核心作用是：启用 SpringFormEncoder 编码器，用来支持 MultipartFile 文件上传。
@FeignClient(value = "zxds-nebula", configuration = MultipartSupportConfig.class)
public interface NebulaServiceApi {

    // consumes作用：告诉 Feign 用 multipart/form-data 格式编码请求，从而正确的把文件和文本字段打包好发出去。
    @PostMapping(value = "/monitoring/uploadRecordFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    R uploadRecordFile(@RequestPart("file") MultipartFile file,
                       @RequestParam("name") String name,
                       @RequestParam(value = "text", required = false) String text);

    @PostMapping("/monitoring/orders")
    R insertOrderMonitoring(@RequestBody InsertOrderMonitoringForm form);

    @PostMapping("/order/gps")
    R insertOrderGps(@RequestBody InsertOrderGpsForm form);

    @GetMapping("/order/gps/mileage")
    R calculateOrderMileage(@SpringQueryMap CalculateOrderMileageForm form);
}
