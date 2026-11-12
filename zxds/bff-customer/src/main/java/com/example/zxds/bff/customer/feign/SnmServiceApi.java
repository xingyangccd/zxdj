package com.example.zxds.bff.customer.feign;

import com.example.zxds.bff.customer.controller.form.ReceiveBillMessageForm;
import com.example.zxds.bff.customer.controller.form.SendNewOrderMessageForm;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("zxds-snm")
public interface SnmServiceApi {

    @PostMapping("/message/new-orders/async")
    R sendNewOrderMessageAsync(@RequestBody SendNewOrderMessageForm form);

    @GetMapping("/message/bill")
    R receiveBillMessage(@SpringQueryMap ReceiveBillMessageForm form);

}
