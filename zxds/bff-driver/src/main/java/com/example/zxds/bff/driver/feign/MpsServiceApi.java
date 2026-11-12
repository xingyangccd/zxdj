package com.example.zxds.bff.driver.feign;

import com.example.zxds.bff.driver.controller.form.RemoveLocationCacheForm;
import com.example.zxds.bff.driver.controller.form.UpdateLocationCacheForm;
import com.example.zxds.bff.driver.controller.form.UpdateOrderLocationCacheForm;
import com.example.zxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("zxds-mps")
public interface MpsServiceApi {
    @PutMapping("/driver/location")
    R updateLocationCache(@RequestBody UpdateLocationCacheForm form);

    @DeleteMapping("/driver/location")
    R removeLocationCache(@RequestBody RemoveLocationCacheForm form);

    @PutMapping("/driver/location/order-location")
    R updateOrderLocationCache(@RequestBody UpdateOrderLocationCacheForm form);
}
