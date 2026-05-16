# 微信协作记账小程序 - 部署指南

## 项目概述

本项目是一个多人协作记账 + 实时位置共享的微信小程序，包含：
- **前端**：微信小程序（原生开发）
- **后端**：Java 17 + Spring Boot 3.2 + MyBatis + WebSocket
- **数据库**：MySQL 8.0+

---

## 目录结构

```
wechat-collab-app/
├── server-java/                     # Java后端服务
│   ├── pom.xml                      # Maven依赖配置
│   ├── sql/
│   │   └── init.sql                 # 数据库初始化脚本
│   └── src/main/
│       ├── java/com/collab/
│       │   ├── CollabApplication.java     # 启动类
│       │   ├── common/                    # 通用类（Result, Exception等）
│       │   ├── config/                    # 配置类
│       │   │   ├── WebConfig.java         # CORS + JWT过滤器注册
│       │   │   ├── WebSocketConfig.java   # WebSocket配置
│       │   │   └── WxConfig.java          # 微信配置属性
│       │   ├── controller/                # API控制器
│       │   │   ├── UserController.java
│       │   │   ├── LedgerController.java
│       │   │   ├── RecordController.java
│       │   │   ├── CategoryController.java
│       │   │   └── HealthController.java
│       │   ├── mapper/                    # MyBatis Mapper接口
│       │   │   ├── UserMapper.java
│       │   │   ├── LedgerMapper.java
│       │   │   ├── RecordMapper.java
│       │   │   └── CategoryMapper.java
│       │   ├── model/                     # 数据模型
│       │   │   ├── entity/                # 实体类
│       │   │   └── dto/                   # 请求DTO
│       │   ├── security/                  # 安全相关
│       │   │   ├── JwtUtil.java           # JWT工具类
│       │   │   └── JwtAuthFilter.java     # JWT认证过滤器
│       │   ├── service/                   # 业务逻辑层
│       │   │   ├── UserService.java
│       │   │   ├── LedgerService.java
│       │   │   ├── RecordService.java
│       │   │   └── CategoryService.java
│       │   └── websocket/                 # WebSocket
│       │       └── LocationWebSocketHandler.java
│       └── resources/
│           ├── application.yml            # Spring Boot配置
│           └── mapper/                    # MyBatis XML映射文件
│               ├── LedgerMapper.xml
│               ├── RecordMapper.xml
│               └── CategoryMapper.xml
├── miniprogram/                     # 微信小程序前端（不变）
└── DEPLOY.md                        # 本文档
```

---

## 一、后端部署（Java Spring Boot）

### 1.1 环境要求

- **JDK 17+**（推荐 OpenJDK 17 或 21）
- **Maven 3.8+**
- **MySQL 8.0+**
- 具有公网IP的服务器
- SSL证书（微信小程序要求HTTPS）

### 1.2 安装步骤

```bash
# 1. 进入后端目录
cd server-java

# 2. 使用Maven编译打包
mvn clean package -DskipTests

# 3. 生成的可执行jar包位于
# target/wechat-collab-server-1.0.0.jar
```

### 1.3 配置说明

项目使用 `application.yml` 作为配置文件，支持通过环境变量覆盖配置。

#### 方式一：设置环境变量（推荐生产环境）

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_USER=root
export DB_PASSWORD=你的数据库密码
export DB_NAME=wechat_collab
export WX_APPID=你的小程序AppID
export WX_SECRET=你的小程序AppSecret
export JWT_SECRET=一个随机的强密码字符串至少32位
export JWT_EXPIRES_IN=604800000
```

#### 方式二：启动时通过命令行参数指定

```bash
java -jar target/wechat-collab-server-1.0.0.jar \
  --spring.datasource.url=jdbc:mysql://localhost:3306/wechat_collab \
  --spring.datasource.username=root \
  --spring.datasource.password=你的密码 \
  --wx.app-id=你的AppID \
  --wx.secret=你的Secret \
  --jwt.secret=你的JWT密钥
```

#### 方式三：自定义配置文件

创建 `application-prod.yml` 放在jar包同目录下，启动时指定profile：

```bash
java -jar target/wechat-collab-server-1.0.0.jar --spring.profiles.active=prod
```

`application-prod.yml` 示例：

```yaml
server:
  port: 3000

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wechat_collab?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root
    password: 你的数据库密码

wx:
  app-id: 你的小程序AppID
  secret: 你的小程序AppSecret

jwt:
  secret: 一个随机的强密码字符串至少32位
  expiration: 604800000
```

### 1.4 初始化数据库

```bash
# 登录MySQL并执行初始化脚本
mysql -u root -p < sql/init.sql
```

数据库将创建以下表：
- `users` - 用户表
- `ledgers` - 账本表
- `ledger_members` - 账本成员关系表
- `categories` - 分类表（含默认分类数据）
- `records` - 账目记录表

### 1.5 启动服务

```bash
# 开发模式
cd server-java
mvn spring-boot:run

# 生产模式
java -jar target/wechat-collab-server-1.0.0.jar
```

启动成功后，控制台会输出：

```
Started CollabApplication in x.xxx seconds
```

验证服务是否正常：

```bash
curl http://localhost:3000/health
# 应返回: {"code":0,"message":"success","data":{"status":"ok","timestamp":"..."}}
```

### 1.6 生产部署建议

#### 使用 systemd 管理服务

创建 `/etc/systemd/system/wechat-collab.service`：

```ini
[Unit]
Description=WeChat Collab Server
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/wechat-collab
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar wechat-collab-server-1.0.0.jar --spring.profiles.active=prod
Restart=always
RestartSec=10

Environment=DB_HOST=localhost
Environment=DB_PORT=3306
Environment=DB_USER=root
Environment=DB_PASSWORD=你的密码
Environment=DB_NAME=wechat_collab
Environment=WX_APPID=你的AppID
Environment=WX_SECRET=你的Secret
Environment=JWT_SECRET=你的JWT密钥

[Install]
WantedBy=multi-user.target
```

```bash
# 启动服务
sudo systemctl start wechat-collab
sudo systemctl enable wechat-collab

# 查看日志
sudo journalctl -u wechat-collab -f
```

#### 使用 Docker 部署（可选）

`Dockerfile`：

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/wechat-collab-server-1.0.0.jar app.jar
EXPOSE 3000
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
```

```bash
# 构建镜像
docker build -t wechat-collab-server .

# 运行容器
docker run -d --name wechat-collab \
  -p 3000:3000 \
  -e DB_HOST=host.docker.internal \
  -e DB_PASSWORD=你的密码 \
  -e WX_APPID=你的AppID \
  -e WX_SECRET=你的Secret \
  -e JWT_SECRET=你的JWT密钥 \
  wechat-collab-server
```

#### Nginx反向代理配置

```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;

    ssl_certificate     /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    # HTTP API
    location /api {
        proxy_pass http://127.0.0.1:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket
    location /ws {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 86400;
    }

    # 健康检查
    location /health {
        proxy_pass http://127.0.0.1:3000;
    }
}

# HTTP重定向到HTTPS
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$host$request_uri;
}
```

---

## 二、前端小程序部署

### 2.1 准备工作

1. 注册微信小程序账号：https://mp.weixin.qq.com/
2. 获取 AppID 和 AppSecret
3. 下载并安装微信开发者工具

### 2.2 配置修改

1. **修改 AppID**

   编辑 `miniprogram/project.config.json`：
   ```json
   {
     "appid": "你的真实AppID"
   }
   ```

2. **修改后端地址**

   编辑 `miniprogram/app.js`：
   ```javascript
   globalData: {
     baseUrl: 'https://your-domain.com',  // 你的后端API地址
     wsUrl: 'wss://your-domain.com/ws',   // WebSocket地址
   }
   ```

### 2.3 开发调试

1. 打开微信开发者工具
2. 导入 `miniprogram` 目录
3. 在开发者工具中预览和调试

### 2.4 发布上线

1. 在微信开发者工具中点击"上传"
2. 登录微信公众平台 -> 版本管理
3. 提交审核
4. 审核通过后发布

### 2.5 微信公众平台配置

在微信公众平台 -> 开发管理 -> 开发设置中：

1. **服务器域名配置**：
   - request合法域名：`https://your-domain.com`
   - socket合法域名：`wss://your-domain.com`

2. **隐私协议设置**：
   - 配置位置信息使用说明
   - 配置用户信息使用说明

---

## 三、数据库设计

### 3.1 表结构概览

| 表名 | 说明 |
|------|------|
| users | 用户表 |
| ledgers | 账本表 |
| ledger_members | 账本成员关系表 |
| categories | 分类表 |
| records | 账目记录表 |

### 3.2 ER关系

```
users (1) ─── (N) ledger_members (N) ─── (1) ledgers
users (1) ─── (N) records (N) ─── (1) ledgers
records (N) ─── (1) categories
```

### 3.3 连接池配置

Java后端使用HikariCP连接池（Spring Boot默认），相关配置在 `application.yml` 中：

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10      # 最大连接数
      minimum-idle: 5            # 最小空闲连接
      idle-timeout: 300000       # 空闲超时(ms)
      connection-timeout: 20000  # 连接超时(ms)
```

---

## 四、API接口文档

### 4.1 用户模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/user/login | 微信登录 | 否 |
| GET | /api/user/info | 获取用户信息 | 是 |
| PUT | /api/user/info | 更新用户信息 | 是 |

### 4.2 账本模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/ledgers | 创建账本 | 是 |
| GET | /api/ledgers | 获取账本列表 | 是 |
| GET | /api/ledgers/{id} | 获取账本详情 | 是 |
| POST | /api/ledgers/{id}/invite | 邀请成员 | 是 |
| POST | /api/ledgers/{id}/invite-code | 生成邀请码 | 是 |
| POST | /api/ledgers/join | 通过邀请码加入 | 是 |

### 4.3 记录模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/records | 添加记录 | 是 |
| GET | /api/records/ledger/{id} | 获取记录列表 | 是 |
| PUT | /api/records/{id} | 更新记录 | 是 |
| DELETE | /api/records/{id} | 删除记录 | 是 |
| GET | /api/records/statistics/{id} | 获取统计 | 是 |

### 4.4 分类模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/categories | 获取分类列表 | 否 |
| POST | /api/categories | 添加分类 | 是 |

### 4.5 WebSocket协议

连接地址：`wss://your-domain.com/ws`

消息类型：

```json
// 认证
{ "type": "auth", "token": "jwt_token" }

// 加入房间
{ "type": "join_room", "ledgerId": "账本ID" }

// 更新位置
{ "type": "update_location", "lat": 39.9, "lng": 116.4, "nickName": "用户名" }

// 离开房间
{ "type": "leave_room" }
```

服务端推送消息：

```json
// 认证成功
{ "type": "auth_success", "userId": "用户ID" }

// 房间内其他人位置
{ "type": "room_locations", "locations": [...] }

// 位置更新
{ "type": "location_update", "userId": "...", "lat": 39.9, "lng": 116.4 }

// 用户离开
{ "type": "user_left", "userId": "..." }
```

---

## 五、Node.js 到 Java 迁移说明

| 原 Node.js | Java Spring Boot 对应 |
|---|---|
| `server/src/app.js` | `CollabApplication.java` + `WebConfig.java` |
| `server/src/config/index.js` | `application.yml` + `WxConfig.java` |
| `server/src/models/db.js` | Spring `DataSource` + HikariCP (自动配置) |
| `server/src/middleware/auth.js` | `JwtAuthFilter.java` |
| `server/src/controllers/*` | `controller/` + `service/` 包 |
| `server/src/routes/*` | `@RequestMapping` 注解在Controller类上 |
| `.env` 环境变量 | `application.yml` + 系统环境变量 |
| `ws` WebSocket库 | Spring WebSocket (`TextWebSocketHandler`) |
| `jsonwebtoken` 库 | `jjwt` 库 (io.jsonwebtoken) |
| `mysql2` 驱动 | `mysql-connector-j` + MyBatis |
| PM2 进程管理 | systemd / Docker |

---

## 六、常见问题

### Q1: 微信登录失败？
- 确认 `wx.app-id` 和 `wx.secret` 配置正确
- 确认后端服务器能访问微信API（api.weixin.qq.com）
- 检查是否有网络防火墙阻挡出站请求

### Q2: WebSocket连接失败？
- 确认 Nginx 配置了 WebSocket 代理（Upgrade头）
- 确认域名已在微信公众平台配置为 socket 合法域名
- 确认 SSL 证书有效
- 检查 `proxy_read_timeout` 是否足够长

### Q3: 位置获取失败？
- 确认已在 app.json 中声明位置权限
- 确认用户已授权位置信息
- 确认在微信公众平台配置了隐私协议

### Q4: 数据库连接失败？
- 确认 MySQL 服务已启动
- 确认 `application.yml` 或环境变量中数据库连接信息正确
- 确认数据库用户有权访问指定数据库
- 检查HikariCP日志是否有连接池耗尽问题

### Q5: 编译报错 "找不到lombok"？
- 确认IDE安装了Lombok插件
- 确认Maven依赖下载完整：`mvn dependency:resolve`

### Q6: JWT认证失败？
- 确认 `jwt.secret` 配置不为空且长度>=32字符
- 确认前端传递的 `Authorization: Bearer <token>` 格式正确

---

## 七、安全建议

1. **生产环境必须使用 HTTPS**
2. **JWT_SECRET 使用强随机字符串**（至少32位）
3. **数据库密码不要使用弱密码**
4. **定期备份数据库**
5. **配置防火墙，只开放必要端口**（443, 80）
6. **不要将配置文件（含敏感信息）提交到版本控制**
7. **设置适当的 CORS 策略**
8. **对用户输入进行验证**（已通过 `@Valid` 注解实现）
9. **JVM内存建议**：生产环境设置 `-Xms256m -Xmx512m` 或更高
10. **日志管理**：使用 logback 配置日志轮转，避免磁盘写满
