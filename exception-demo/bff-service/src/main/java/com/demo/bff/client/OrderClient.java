package com.demo.bff.client;

import com.demo.bff.common.R;
import com.demo.bff.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// url 一旦指定，Feign 就不走服务发现，直连这个地址，其中 order.service.url 前缀随便写。
// order.service.url 是读取的yml文件中的配置，如果配置不存在的话，使用冒号后面的默认值。
@FeignClient(name = "order-service", url = "${order.service.url:http://localhost:8081}")
public interface OrderClient {

    @GetMapping("/orders/{id}")
    R<OrderDTO> getOrder(@PathVariable Long id);

}