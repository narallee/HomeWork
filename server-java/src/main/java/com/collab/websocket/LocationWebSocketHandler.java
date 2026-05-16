package com.collab.websocket;

import com.collab.security.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@EnableScheduling
public class LocationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(LocationWebSocketHandler.class);

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    // sessionId -> WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // sessionId -> userId
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();
    // userId -> sessionId
    private final Map<String, String> userSessionMap = new ConcurrentHashMap<>();
    // sessionId -> ledgerId (当前加入的房间)
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();
    // ledgerId -> { userId -> locationData }
    private final Map<String, Map<String, LocationData>> roomLocations = new ConcurrentHashMap<>();

    public LocationWebSocketHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.debug("WebSocket连接建立: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            String type = jsonNode.get("type").asText();

            switch (type) {
                case "auth" -> handleAuth(session, jsonNode);
                case "join_room" -> handleJoinRoom(session, jsonNode);
                case "update_location" -> handleUpdateLocation(session, jsonNode);
                case "leave_room" -> handleLeaveRoom(session);
                default -> sendMessage(session, createErrorMessage("未知消息类型"));
            }
        } catch (Exception e) {
            log.error("WebSocket消息处理错误: {}", e.getMessage());
            sendMessage(session, createErrorMessage("消息格式错误"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        handleDisconnect(session);
        log.debug("WebSocket连接断开: {}", session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket传输错误: {}", exception.getMessage());
        handleDisconnect(session);
    }

    private void handleAuth(WebSocketSession session, JsonNode jsonNode) throws IOException {
        String token = jsonNode.get("token").asText();
        try {
            if (!jwtUtil.isTokenValid(token)) {
                sendMessage(session, createMessage("auth_failed", "message", "认证失败"));
                session.close();
                return;
            }
            String userId = jwtUtil.getUserIdFromToken(token);
            sessionUserMap.put(session.getId(), userId);
            userSessionMap.put(userId, session.getId());

            ObjectNode response = objectMapper.createObjectNode();
            response.put("type", "auth_success");
            response.put("userId", userId);
            sendMessage(session, response.toString());
        } catch (Exception e) {
            sendMessage(session, createMessage("auth_failed", "message", "认证失败"));
            session.close();
        }
    }

    private void handleJoinRoom(WebSocketSession session, JsonNode jsonNode) throws IOException {
        String userId = sessionUserMap.get(session.getId());
        if (userId == null) {
            sendMessage(session, createErrorMessage("请先认证"));
            return;
        }

        String ledgerId = jsonNode.get("ledgerId").asText();
        sessionRoomMap.put(session.getId(), ledgerId);
        roomLocations.computeIfAbsent(ledgerId, k -> new ConcurrentHashMap<>());

        // 发送当前房间内其他人的位置
        Map<String, LocationData> roomLocs = roomLocations.get(ledgerId);
        List<Map<String, Object>> locations = new ArrayList<>();
        roomLocs.forEach((uid, loc) -> {
            if (!uid.equals(userId)) {
                Map<String, Object> locMap = new HashMap<>();
                locMap.put("userId", uid);
                locMap.put("lat", loc.lat);
                locMap.put("lng", loc.lng);
                locMap.put("nickName", loc.nickName);
                locMap.put("avatarUrl", loc.avatarUrl);
                locMap.put("timestamp", loc.timestamp);
                locations.add(locMap);
            }
        });

        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "room_locations");
        response.set("locations", objectMapper.valueToTree(locations));
        sendMessage(session, response.toString());
    }

    private void handleUpdateLocation(WebSocketSession session, JsonNode jsonNode) {
        String userId = sessionUserMap.get(session.getId());
        String ledgerId = sessionRoomMap.get(session.getId());

        if (userId == null || ledgerId == null) {
            try {
                sendMessage(session, createErrorMessage("请先加入房间"));
            } catch (IOException ignored) {}
            return;
        }

        LocationData locationData = new LocationData();
        locationData.lat = jsonNode.get("lat").asDouble();
        locationData.lng = jsonNode.get("lng").asDouble();
        locationData.nickName = jsonNode.has("nickName") ? jsonNode.get("nickName").asText() : "未知";
        locationData.avatarUrl = jsonNode.has("avatarUrl") ? jsonNode.get("avatarUrl").asText() : "";
        locationData.timestamp = System.currentTimeMillis();

        Map<String, LocationData> roomLocs = roomLocations.get(ledgerId);
        if (roomLocs != null) {
            roomLocs.put(userId, locationData);
        }

        // 广播给同房间其他用户
        ObjectNode broadcastMsg = objectMapper.createObjectNode();
        broadcastMsg.put("type", "location_update");
        broadcastMsg.put("userId", userId);
        broadcastMsg.put("lat", locationData.lat);
        broadcastMsg.put("lng", locationData.lng);
        broadcastMsg.put("nickName", locationData.nickName);
        broadcastMsg.put("avatarUrl", locationData.avatarUrl);
        broadcastMsg.put("timestamp", locationData.timestamp);

        broadcastToRoom(ledgerId, userId, broadcastMsg.toString());
    }

    private void handleLeaveRoom(WebSocketSession session) {
        String userId = sessionUserMap.get(session.getId());
        String ledgerId = sessionRoomMap.get(session.getId());

        if (userId != null && ledgerId != null) {
            Map<String, LocationData> roomLocs = roomLocations.get(ledgerId);
            if (roomLocs != null) {
                roomLocs.remove(userId);
            }

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("type", "user_left");
            msg.put("userId", userId);
            broadcastToRoom(ledgerId, userId, msg.toString());

            sessionRoomMap.remove(session.getId());
        }
    }

    private void handleDisconnect(WebSocketSession session) {
        handleLeaveRoom(session);
        String userId = sessionUserMap.remove(session.getId());
        if (userId != null) {
            userSessionMap.remove(userId);
        }
        sessions.remove(session.getId());
    }

    private void broadcastToRoom(String ledgerId, String excludeUserId, String message) {
        Map<String, LocationData> roomLocs = roomLocations.get(ledgerId);
        if (roomLocs == null) return;

        roomLocs.keySet().forEach(uid -> {
            if (!uid.equals(excludeUserId)) {
                String targetSessionId = userSessionMap.get(uid);
                if (targetSessionId != null) {
                    WebSocketSession targetSession = sessions.get(targetSessionId);
                    if (targetSession != null && targetSession.isOpen()) {
                        try {
                            sendMessage(targetSession, message);
                        } catch (IOException e) {
                            log.error("广播消息失败: {}", e.getMessage());
                        }
                    }
                }
            }
        });
    }

    private void sendMessage(WebSocketSession session, String message) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    private String createErrorMessage(String errorMsg) {
        return "{\"type\":\"error\",\"message\":\"" + errorMsg + "\"}";
    }

    private String createMessage(String type, String key, String value) {
        return "{\"type\":\"" + type + "\",\"" + key + "\":\"" + value + "\"}";
    }

    /**
     * 定期清理过期位置数据（超过5分钟未更新）
     */
    @Scheduled(fixedRate = 60000)
    public void cleanExpiredLocations() {
        long now = System.currentTimeMillis();
        long timeout = 5 * 60 * 1000; // 5分钟

        roomLocations.forEach((ledgerId, roomLocs) -> {
            Iterator<Map.Entry<String, LocationData>> iterator = roomLocs.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, LocationData> entry = iterator.next();
                if (now - entry.getValue().timestamp > timeout) {
                    iterator.remove();
                    ObjectNode msg = objectMapper.createObjectNode();
                    msg.put("type", "user_left");
                    msg.put("userId", entry.getKey());
                    broadcastToRoom(ledgerId, entry.getKey(), msg.toString());
                }
            }
            if (roomLocs.isEmpty()) {
                roomLocations.remove(ledgerId);
            }
        });
    }

    // 内部位置数据类
    private static class LocationData {
        double lat;
        double lng;
        String nickName;
        String avatarUrl;
        long timestamp;
    }
}
