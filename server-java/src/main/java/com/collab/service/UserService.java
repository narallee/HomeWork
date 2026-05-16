package com.collab.service;

import com.collab.common.BusinessException;
import com.collab.config.WxConfig;
import com.collab.mapper.UserMapper;
import com.collab.model.entity.User;
import com.collab.security.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final WxConfig wxConfig;
    private final ObjectMapper objectMapper;

    public UserService(UserMapper userMapper, JwtUtil jwtUtil, WxConfig wxConfig) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.wxConfig = wxConfig;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 微信登录
     */
    public Map<String, Object> wxLogin(String code) {
        // 调用微信接口获取openid和session_key
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wxConfig.getAppId(), wxConfig.getSecret(), code);

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
                throw BusinessException.badRequest("微信登录失败: " + jsonNode.get("errmsg").asText());
            }

            String openid = jsonNode.get("openid").asText();
            String sessionKey = jsonNode.has("session_key") ? jsonNode.get("session_key").asText() : "";

            // 查询用户是否存在
            User user = userMapper.findByOpenid(openid);

            if (user == null) {
                // 新用户注册
                user = new User();
                user.setId(UUID.randomUUID().toString());
                user.setOpenid(openid);
                user.setSessionKey(sessionKey);
                userMapper.insert(user);
            } else {
                // 更新session_key
                userMapper.updateSessionKey(user.getId(), sessionKey);
            }

            // 生成JWT
            String token = jwtUtil.generateToken(user.getId(), openid);

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", user.getId());
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "微信登录处理失败");
        }
    }

    /**
     * 获取用户信息
     */
    public User getUserInfo(String userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return user;
    }

    /**
     * 更新用户信息
     */
    public void updateUserInfo(String userId, String nickName, String avatarUrl) {
        userMapper.updateUserInfo(userId, nickName, avatarUrl);
    }
}
