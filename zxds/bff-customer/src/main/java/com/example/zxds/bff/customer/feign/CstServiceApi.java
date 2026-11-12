package com.example.zxds.bff.customer.feign;

import com.example.zxds.bff.customer.controller.form.*;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "zxds-cst")
public interface CstServiceApi {

    @PostMapping("/customer")
    R registerNewCustomer(@RequestBody RegisterNewCustomerForm form);

    @PostMapping("/customer/token")
    R login(LoginForm form);

    @PostMapping("/customer/car")
    R insertCustomerCar(@RequestBody InsertCustomerCarForm form);

    @PostMapping("/customer/car/search")
    R searchCustomerCarList(@RequestBody SearchCustomerCarListForm form);

    @DeleteMapping("/customer/car")
    R deleteCustomerCarById(@RequestBody DeleteCustomerCarByIdForm form);

    @GetMapping("/customer/open-id")
    R searchCustomerOpenId(@SpringQueryMap SearchCustomerOpenIdForm form);

}