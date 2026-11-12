package com.example.zxds.bff.driver.feign;

import com.example.zxds.bff.driver.controller.form.ClearNewOrderQueueForm;
import com.example.zxds.bff.driver.controller.form.ReceiveNewOrderMessageForm;
import com.example.zxds.bff.driver.controller.form.SendPrivateMessageForm;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "zxds-snm")
public interface SnmServiceApi {

    @DeleteMapping("/message/new-orders/queue/message")
    R clearNewOrderQueue(@RequestBody ClearNewOrderQueueForm form);

    @GetMapping("/message/new-orders/received")
    R receiveNewOrderMessage(@SpringQueryMap ReceiveNewOrderMessageForm form);

    // 同步
    @PostMapping("/message")
    R sendPrivateMessage(@RequestBody SendPrivateMessageForm form);

    // 异步
    @PostMapping("/message/async")
    R sendPrivateMessageAsync(@RequestBody SendPrivateMessageForm form);
}