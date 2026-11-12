package com.example.zxds.bff.customer.feign;

import com.example.zxds.bff.customer.controller.form.EstimateOrderMileageAndMinuteForm;
import com.example.zxds.bff.customer.controller.form.SearchBefittingDriverAboutOrderForm;
import com.example.zxds.bff.customer.controller.form.SearchOrderLocationCacheForm;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("zxds-mps")
public interface MpsServiceApi {

    @PostMapping("/map/estimate")
    R estimateOrderMileageAndMinute(@RequestBody EstimateOrderMileageAndMinuteForm form);

    @PostMapping("/driver/location/search")
    R searchBefittingDriverAboutOrder(@RequestBody SearchBefittingDriverAboutOrderForm form);

    @GetMapping("/driver/location/order-location")
    R searchOrderLocationCache(@SpringQueryMap SearchOrderLocationCacheForm form);
}
