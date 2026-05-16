package com.collab.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private String id;
    private String openid;
    private String sessionKey;
    private String nickName;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
