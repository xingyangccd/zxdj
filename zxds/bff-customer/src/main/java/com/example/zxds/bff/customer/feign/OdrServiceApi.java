package com.example.zxds.bff.customer.feign;

import com.example.zxds.bff.customer.controller.form.*;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

@FeignClient("zxds-odr")
public interface OdrServiceApi {
    @PostMapping("/order")
    R insertOrder(@RequestBody InsertOrderForm form);
    @GetMapping("/order/status")
    R searchOrderStatus(@SpringQueryMap SearchOrderStatusForm form);
    @DeleteMapping("/order/unaccepted")
    R deleteUnAcceptOrder(@RequestBody DeleteUnAcceptOrderForm form);
    @GetMapping("/order/customer/current")
    R hasCustomerCurrentOrder(@SpringQueryMap HasCustomerCurrentOrderForm form);
    @GetMapping("/order/move-info")
    R searchOrderForMoveById(@SpringQueryMap SearchOrderForMoveByIdForm form);
    @PutMapping("/order/confirm-arrive")
    R confirmArriveStartPlace(@RequestBody ConfirmArriveStartPlaceForm form);
    @GetMapping("/order")
    R searchOrderById(@SpringQueryMap SearchOrderByIdForm form);
    @GetMapping("/order/payable")
    R validCanPayOrder(@SpringQueryMap ValidCanPayOrderForm form);
    @PutMapping("/bill/payment")
    R updateBillPayment(@RequestBody UpdateBillPaymentForm form);
    @PutMapping("/order/prepay-id")
    R updateOrderPrepayId(@RequestBody UpdateOrderPrepayIdForm form);
    @PostMapping("/order/payment-status-sync")
    R updateOrderAboutPayment(UpdateOrderAboutPaymentForm form);
}
