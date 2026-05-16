package com.collab.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLedgerRequest {
    @NotBlank(message = "账本名称不能为空")
    private String name;
    private String description;
}
