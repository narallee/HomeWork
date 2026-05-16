package com.collab.controller;

import com.collab.common.Result;
import com.collab.model.dto.UserInfoRequest;
import com.collab.model.dto.WxLoginRequest;
import com.collab.model.entity.User;
import com.collab.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 微信登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody WxLoginRequest request) {
        Map<String, Object> data = userService.wxLogin(request.getCode());
        return Result.success(data, "登录成功");
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<Void> updateUserInfo(HttpServletRequest request, @RequestBody UserInfoRequest body) {
        String userId = (String) request.getAttribute("userId");
        userService.updateUserInfo(userId, body.getNickName(), body.getAvatarUrl());
        return Result.success("更新成功");
    }
}
