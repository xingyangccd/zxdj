package com.example.zxds.mps.controller;

import com.example.zxds.common.util.R;
import com.example.zxds.mps.controller.form.CalculateDriveLineForm;
import com.example.zxds.mps.controller.form.EstimateOrderMileageAndMinuteForm;
import com.example.zxds.mps.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/map")
@Tag(name = "MapController", description = "地图WEB接口")
@RequiredArgsConstructor
@Slf4j
public class MapController {
    private final MapService mapService;

    @PostMapping("/estimate")
    @Operation(summary = "预估代驾里程和代驾时长")
    public R estimateOrderMileageAndMinute(@RequestBody @Valid EstimateOrderMileageAndMinuteForm form) {
        Map<String, Object> map = mapService.estimateOrderMileageAndMinute(
                form.getMode(),
                form.getStartPlaceLatitude(),
                form.getStartPlaceLongitude(),
                form.getEndPlaceLatitude(),
                form.getEndPlaceLongitude()
        );
        return R.ok().put("result", map);
    }

    @GetMapping("/drive-lines")
    @Operation(summary = "计算行驶线路")
    public R calculateDriveLine(@Valid CalculateDriveLineForm form) {
        Map<String, Object> map = mapService.calculateDriveLine(
                form.getStartPlaceLatitude(),
                form.getStartPlaceLongitude(),
                form.getEndPlaceLatitude(),
                form.getEndPlaceLongitude()
        );
        return R.ok().put("result", map);
    }
}
