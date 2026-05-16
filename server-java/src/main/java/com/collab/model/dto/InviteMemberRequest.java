package com.collab.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteMemberRequest {
    @NotBlank(message = "目标用户ID不能为空")
    private String targetUserId;
}
