package com.collab.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    private String id;
    private String name;
    private String type; // income / expense
    private String icon;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
