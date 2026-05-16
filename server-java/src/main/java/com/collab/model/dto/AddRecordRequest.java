package com.collab.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddRecordRequest {
    @NotBlank(message = "账本ID不能为空")
    private String ledgerId;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    @NotBlank(message = "类型不能为空")
    private String type; // income / expense

    private String categoryId;
    private String description;
    private String date; // yyyy-MM-dd
}
