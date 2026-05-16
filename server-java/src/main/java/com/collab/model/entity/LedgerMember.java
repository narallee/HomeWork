package com.collab.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LedgerMember {
    private String ledgerId;
    private String userId;
    private String role; // owner, admin, member
    private LocalDateTime joinedAt;

    // 关联字段
    private String nickName;
    private String avatarUrl;
}
