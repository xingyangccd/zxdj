package com.example.zxds.snm.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "根据消息ID删除消息的表单")
public class DeleteMessageRefByIdForm {
    @NotBlank(message = "id不能为空")
    @Schema(description = "Ref消息的ID")
    private String id;
}