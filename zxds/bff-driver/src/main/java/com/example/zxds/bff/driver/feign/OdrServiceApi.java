package com.example.zxds.bff.driver.feign;

import com.example.zxds.bff.driver.controller.form.*;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("zxds-odr")
public interface OdrServiceApi {

    @PostMapping("/order/driver-today-stat")
    R searchDriverTodayBusinessData(@RequestBody SearchDriverTodayBusinessDataForm form);

    @PutMapping("/order/driver")
    R acceptNewOrder(@RequestBody AcceptNewOrderForm form);

    @GetMapping("/order/driver/execute-order")
    R searchDriverExecuteOrder(@SpringQueryMap SearchDriverExecuteOrderForm form);

    @GetMapping("/order/driver/current")
    R searchDriverCurrentOrder(@SpringQueryMap SearchDriverCurrentOrderForm form);

    @GetMapping("/order/move-info")
    R searchOrderForMoveById(@SpringQueryMap SearchOrderForMoveByIdForm form);

    @PutMapping("/order/arrive")
    R arriveStartPlace(@RequestBody ArriveStartPlaceForm form);

    @PutMapping("/order/start-driving")
    R startDriving(@RequestBody StartDrivingForm form);

    @PutMapping("/order/status")
    R updateOrderStatus(@RequestBody UpdateOrderStatusForm form);

    @GetMapping("/order/valid-driver")
    R validDriverOwnOrder(@SpringQueryMap ValidDriverOwnOrderForm form);

    @GetMapping("/order/settlement-data")
    R searchSettlementNeedData(@SpringQueryMap SearchSettlementNeedDataForm form);

    @PutMapping("/bill")
    R updateBillFee(@RequestBody UpdateBillFeeForm form);

    @GetMapping("/bill/driver-order-bills/review")
    R searchReviewDriverOrderBill(@SpringQueryMap SearchReviewDriverOrderBillForm form);

    @GetMapping("/order/status")
    R searchOrderStatus(@SpringQueryMap SearchOrderStatusForm form);

    @PostMapping("/order/payment-status-sync")
    R updateOrderAboutPayment(@RequestBody UpdateOrderAboutPaymentForm form);
}
