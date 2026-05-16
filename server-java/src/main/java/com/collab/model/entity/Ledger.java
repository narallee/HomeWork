package com.collab.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Ledger {
    private String id;
    private String name;
    private String description;
    private String ownerId;
    private String inviteCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 非数据库字段，用于查询结果
    private String role;
    private Integer memberCount;
    private java.math.BigDecimal totalAmount;
}
