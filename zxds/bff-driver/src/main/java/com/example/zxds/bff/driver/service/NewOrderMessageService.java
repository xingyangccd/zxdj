package com.example.zxds.bff.driver.service;

import com.example.zxds.bff.driver.controller.form.ClearNewOrderQueueForm;
import com.example.zxds.bff.driver.controller.form.ReceiveNewOrderMessageForm;

import java.util.List;
import java.util.Map;

public interface NewOrderMessageService {
    /**
     * 清空消息队列
     */
    void clearNewOrderQueue(ClearNewOrderQueueForm form);

    /**
     * 司机接收属于自己的订单消息
     */
    List<Map<String, Object>> receiveNewOrderMessage(ReceiveNewOrderMessageForm form);
}
