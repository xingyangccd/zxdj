package com.example.zxds.nebula.controller.form;

import com.example.zxds.nebula.controller.dto.InsertOrderGpsDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "添加订单GPS记录的表单")
public class InsertOrderGpsForm {
    @NotEmpty(message = "list不能为空")
    @Schema(description = "GPS数据")
    private List<@Valid InsertOrderGpsDTO> list;
}