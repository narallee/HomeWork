package com.collab.model.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Record {
    private String id;
    private String ledgerId;
    private String userId;
    private BigDecimal amount;
    private String type; // income / expense
    private String categoryId;
    private String description;
    private LocalDate recordDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联字段
    private String nickName;
    private String avatarUrl;
    private String categoryName;
}
