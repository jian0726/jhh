# 三角洲行动游戏账号租赁平台

基于 Spring Boot 的游戏账号租赁平台后端服务，提供用户管理、商品管理、订单管理、支付集成、风控系统等完整功能。

## 🛠️ 技术栈

| 技术              | 版本     | 说明    |
| --------------- | ------ | ----- |
| Java            | 21     | 编程语言  |
| Spring Boot     | 3.2.x  | 应用框架  |
| MySQL           | 8.0+   | 数据库   |
| Spring Data JPA | 3.2.x  | ORM框架 |
| JWT             | -      | 身份认证  |
| Lombok          | 1.18.x | 代码简化  |

## 📦 功能模块

### 1. 用户模块

- 用户注册/登录
- 第三方登录（微信、QQ OAuth2.0）
- 用户信息管理
- 实名认证

### 2. 商品模块

- 商品上架/下架
- 商品搜索（MySQL全文检索）
- 商品审核流程

### 3. 订单模块

- 订单创建与管理
- 订单状态流转
- 纠纷处理系统

### 4. 支付模块

- **易宝支付** - 主流支付渠道
- **芝麻信用分** - 信用评估
- **微信支付分** - 信用评估

### 5. 风控模块

- IP频率限制
- 设备频率限制
- 用户行为分析
- 实时风险评估

### 6. 消息模块

- 消息推送
- 阅读状态管理
- 消息列表

### 7. 收藏模块

- 收藏商品
- 取消收藏
- 收藏列表管理

### 8. 浏览历史

- 记录浏览足迹
- 清空浏览历史
- 智能推荐数据来源

### 9. IM 即时通讯

- 实时私信
- 一对一聊天
- 消息已读回执
- 会话管理

### 10. 订单验收

- 发起验号
- 确认收货
- 拒绝收货（触发纠纷）

### 11. 退款模块

- 申请退款
- 卖家处理退款
- 管理员仲裁

### 12. 钱包与提现

- 余额查询
- 申请提现
- 提现记录

## 📁 项目结构

```plaintext
src/main/java/com/delta/rental/
├── controller/          # REST API 控制层 (10+ controllers)
├── service/             # 业务逻辑层 (15+ services)
├── repository/          # 数据访问层 (JPA Repositories)
├── entity/              # JPA 实体类 (10+ entities)
├── dto/                 # 数据传输对象
├── config/              # 配置类 (支付、安全、数据源)
├── enums/               # 枚举类
├── exception/           # 异常处理 (全局异常拦截)
└── util/                # 工具类 (JWT、加密、日期)
```

## 🚀 快速开始

### 环境要求

```bash
JDK 21+    # 必须
MySQL 8.0+ # 必须
Maven 3.8+ # 必须
```

### 数据库配置

```sql
CREATE DATABASE IF NOT EXISTS sanJiaoZhouXingDong
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

### 运行项目

```bash
# 1. 克隆项目
git clone <项目地址>
cd game-rental-platform

# 2. 配置数据库连接
# 编辑 application.yml 中的数据库配置

# 3. 编译项目
mvn clean compile

# 4. 运行项目
mvn spring-boot:run

# 服务启动后访问: http://localhost:8080
```

***

## 🔌 API 接口文档

### 统一响应格式

所有接口返回统一 JSON 格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段      | 类型     | 说明          |
| ------- | ------ | ----------- |
| code    | int    | 状态码，200表示成功 |
| message | string | 响应消息        |
| data    | object | 响应数据        |

### 认证方式

需要登录的接口需在请求头携带 JWT Token：

```
Authorization: Bearer <JWT_TOKEN>
```

***

## 1. 用户模块

### 1.1 用户注册

**接口**: `POST /api/users/register`

**请求体**:

| 字段         | 类型     | 必填 | 说明          |
| ---------- | ------ | -- | ----------- |
| yongHuMing | string | 是  | 用户名（3-50字符） |
| miMa       | string | 是  | 密码（6-20字符）  |
| shouJiHao  | string | 是  | 手机号（11位）    |

**请求示例**:

```json
{
  "yongHuMing": "player_001",
  "miMa": "password123",
  "shouJiHao": "13800138000"
}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "yongHuMing": "player_001",
    "shouJiHao": "13800138000",
    "jueSe": 0,
    "zhuangTai": 0,
    "chuangJianShiJian": "2024-01-01 10:00:00"
  }
}
```

***

### 1.2 用户登录

**接口**: `POST /api/users/login`

**请求体**:

| 字段         | 类型     | 必填 | 说明  |
| ---------- | ------ | -- | --- |
| yongHuMing | string | 是  | 用户名 |
| miMa       | string | 是  | 密码  |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "yongHu": {
      "id": 1,
      "yongHuMing": "player_001",
      "jueSe": 0
    }
  }
}
```

***

### 1.3 获取用户信息

**接口**: `GET /api/users/profile`

**请求头**: `Authorization: Bearer <TOKEN>`

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "yongHuMing": "player_001",
    "shouJiHao": "13800138000",
    "zhenShiXingMing": null,
    "shiFouShiMing": 0,
    "jueSe": 0,
    "zhuangTai": 0
  }
}
```

***

### 1.4 更新用户信息

**接口**: `PUT /api/users/profile`

**请求头**: `Authorization: Bearer <TOKEN>`

**请求体**:

| 字段              | 类型     | 必填 | 说明   |
| --------------- | ------ | -- | ---- |
| yongHuMing      | string | 否  | 用户名  |
| zhenShiXingMing | string | 否  | 真实姓名 |
| shenFenZheng    | string | 否  | 身份证号 |

***

### 1.5 实名认证

**接口**: `POST /api/users/verify`

**请求头**: `Authorization: Bearer <TOKEN>`

**请求体**:

| 字段              | 类型     | 必填 | 说明   |
| --------------- | ------ | -- | ---- |
| zhenShiXingMing | string | 是  | 真实姓名 |
| shenFenZheng    | string | 是  | 身份证号 |

***

### 1.6 第三方登录授权

**接口**: `GET /api/oauth/auth-url`

**请求参数**:

| 参数    | 类型     | 必填 | 说明             |
| ----- | ------ | -- | -------------- |
| type  | string | 是  | 登录类型：wechat/qq |
| state | string | 否  | 状态参数           |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "authUrl": "https://open.weixin.qq.com/connect/qrconnect?appid=..."
  }
}
```

***

### 1.7 第三方登录回调

**接口**: `POST /api/oauth/callback`

**请求参数**:

| 参数    | 类型     | 必填 | 说明             |
| ----- | ------ | -- | -------------- |
| type  | string | 是  | 登录类型：wechat/qq |
| code  | string | 是  | 授权码            |
| state | string | 否  | 状态参数           |

***

## 2. 商品模块

### 2.1 获取商品列表

**接口**: `GET /api/products`

**请求参数**:

| 参数        | 类型  | 必填 | 说明        |
| --------- | --- | -- | --------- |
| page      | int | 否  | 页码，默认0    |
| size      | int | 否  | 每页数量，默认10 |
| zhuangTai | int | 否  | 状态筛选      |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 10
  }
}
```

***

### 2.2 获取商品详情

**接口**: `GET /api/products/{id}`

**路径参数**:

| 参数 | 类型   | 说明   |
| -- | ---- | ---- |
| id | long | 商品ID |

***

### 2.3 上架商品

**接口**: `POST /api/products`

**请求头**: `Authorization: Bearer <TOKEN>`

**请求体**:

| 字段           | 类型      | 必填 | 说明   |
| ------------ | ------- | -- | ---- |
| biaoTi       | string  | 是  | 商品标题 |
| miaoShu      | string  | 否  | 商品描述 |
| jiaGe        | decimal | 是  | 价格   |
| zuJieTianShu | int     | 是  | 租赁天数 |
| youXiHao     | string  | 是  | 游戏账号 |
| miMa         | string  | 是  | 游戏密码 |

***

### 2.4 更新商品

**接口**: `PUT /api/products/{id}`

**请求头**: `Authorization: Bearer <TOKEN>`

***

### 2.5 删除商品

**接口**: `DELETE /api/products/{id}`

**请求头**: `Authorization: Bearer <TOKEN>`

***

### 2.6 搜索商品

**接口**: `GET /api/products/search`

**请求参数**:

| 参数      | 类型     | 必填 | 说明    |
| ------- | ------ | -- | ----- |
| keyword | string | 是  | 搜索关键词 |
| page    | int    | 否  | 页码    |
| size    | int    | 否  | 每页数量  |

***

## 3. 订单模块

### 3.1 创建订单

**接口**: `POST /api/orders`

**请求头**: `Authorization: Bearer <TOKEN>`

**请求体**:

| 字段           | 类型   | 必填 | 说明   |
| ------------ | ---- | -- | ---- |
| shangPinId   | long | 是  | 商品ID |
| zuJieTianShu | int  | 是  | 租赁天数 |

***

### 3.2 获取订单详情

**接口**: `GET /api/orders/{id}`

**请求头**: `Authorization: Bearer <TOKEN>`

***

### 3.3 获取我的订单

**接口**: `GET /api/orders/my`

**请求头**: `Authorization: Bearer <TOKEN>`

***

### 3.4 取消订单

**接口**: `PUT /api/orders/{id}/cancel`

**请求头**: `Authorization: Bearer <TOKEN>`

***

### 3.5 提交纠纷

**接口**: `POST /api/disputes`

**请求头**: `Authorization: Bearer <TOKEN>`

**请求参数**:

| 参数         | 类型     | 必填 | 说明   |
| ---------- | ------ | -- | ---- |
| dingDanHao | string | 是  | 订单号  |
| reason     | string | 是  | 纠纷原因 |

***

### 3.6 上传证据

**接口**: `POST /api/disputes/{id}/evidence`

**请求头**: `Authorization: Bearer <TOKEN>`

***

## 4. 支付模块

### 4.1 易宝支付

**接口**: `POST /api/yeepay/pay`

**请求头**: `Authorization: Bearer <TOKEN>`

**请求参数**:

| 参数         | 类型     | 必填 | 说明  |
| ---------- | ------ | -- | --- |
| dingDanHao | string | 是  | 订单号 |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "gatewayUrl": "https://api.yeepay.com/pay",
    "params": {
      "merchantId": "xxx",
      "orderId": "ORD202401010001",
      "amount": "50.00",
      "sign": "xxx"
    }
  }
}
```

***

### 4.2 支付回调

**接口**: `POST /api/yeepay/notify`

**响应**: `success` 或 `fail`

***

### 4.3 查询芝麻信用分

**接口**: `GET /api/alipay-credit/score`

**请求头**: `Authorization: Bearer <TOKEN>`

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "score": 720,
    "userId": "xxx"
  }
}
```

***

### 4.4 查询微信支付分

**接口**: `GET /api/wechat-score/score`

**请求头**: `Authorization: Bearer <TOKEN>`

***

## 5. 风控模块

### 5.1 风险检测

**接口**: `POST /api/fengkong/check`

**请求体**:

| 字段         | 类型     | 必填 | 说明   |
| ---------- | ------ | -- | ---- |
| ip         | string | 是  | IP地址 |
| deviceId   | string | 否  | 设备ID |
| userId     | long   | 否  | 用户ID |
| actionType | string | 是  | 行为类型 |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "riskLevel": "LOW",
    "riskScore": 20,
    "riskDetails": []
  }
}
```

***

### 5.2 获取规则列表

**接口**: `GET /api/fengkong/rules`

**请求头**: `Authorization: Bearer <TOKEN>`（管理员）

***

### 5.3 添加规则

**接口**: `POST /api/fengkong/rules`

**请求头**: `Authorization: Bearer <TOKEN>`（管理员）

***

## 6. 消息模块

### 6.1 获取我的消息

**接口**: `GET /api/messages/my`

**请求头**: `Authorization: Bearer <TOKEN>`

***

### 6.2 获取未读消息

**接口**: `GET /api/messages/unread`

**请求头**: `Authorization: Bearer <TOKEN>`

***

### 6.3 标记为已读

**接口**: `PUT /api/messages/{id}/read`

**请求头**: `Authorization: Bearer <TOKEN>`

***

## 7. 管理员模块

### 7.1 获取用户列表

**接口**: `GET /api/admin/users`

**请求头**: `Authorization: Bearer <TOKEN>`（管理员）

***

### 7.2 封禁用户

**接口**: `PUT /api/admin/users/{id}/ban`

**请求头**: `Authorization: Bearer <TOKEN>`（管理员）

***

### 7.3 审核商品

**接口**: `PUT /api/admin/products/{id}/audit`

**请求头**: `Authorization: Bearer <TOKEN>`（管理员）

**请求参数**:

| 参数     | 类型     | 必填 | 说明        |
| ------ | ------ | -- | --------- |
| status | int    | 是  | 1-通过，3-拒绝 |
| reason | string | 否  | 拒绝原因      |

***

### 7.4 获取订单列表

**接口**: `GET /api/admin/orders`

**请求头**: `Authorization: Bearer <TOKEN>`（管理员）

***

### 7.5 处理纠纷

**接口**: `PUT /api/admin/disputes/{id}/handle`

**请求头**: `Authorization: Bearer <TOKEN>`（管理员）

**请求参数**:

| 参数     | 类型     | 必填 | 说明                     |
| ------ | ------ | -- | ---------------------- |
| result | string | 是  | BUYER\_WIN/SELLER\_WIN |
| remark | string | 否  | 处理备注                   |

***

## 8. 收藏模块

### 8.1 收藏商品

**接口**: `POST /api/favorites`

**请求头**: `Authorization: Bearer <TOKEN>`

**请求参数**:

| 参数       | 类型   | 必填 | 说明   |
| -------- | ---- | -- | ---- |
| shangPinId | long | 是  | 商品ID |

***

### 8.2 获取收藏列表

**接口**: `GET /api/favorites`

**请求头**: `Authorization: Bearer <TOKEN>`

**请求参数**:

| 参数    | 类型  | 必填 | 说明        |
| ------ | --- | -- | --------- |
| page   | int  | 否  | 页码，默认0    |
| size   | int  | 否  | 每页数量，默认10 |

***

### 8.3 取消收藏

**接口**: `DELETE /api/favorites/{id}`

**请求头**: `Authorization: Bearer <TOKEN>`

**路径参数**:

| 参数 | 类型   | 说明   |
| -- | ---- | ---- |
| id | long | 收藏ID |

***

## 9. 浏览历史

### 9.1 获取浏览历史

**接口**: `GET /api/history/browse`

**请求头**: `Authorization: Bearer <TOKEN>`

**请求参数**:

| 参数    | 类型  | 必填 | 说明        |
| ------ | --- | -- | --------- |
| page   | int  | 否  | 页码，默认0    |
| size   | int  | 否  | 每页数量，默认10 |

***

### 9.2 清空浏览历史

**接口**: `DELETE /api/history/browse`

**请求头**: `Authorization: Bearer <TOKEN>`

***

## 10. IM 即时通讯

### 10.1 创建会话

**接口**: `POST /api/im/session/create`

**请求头**: `Authorization: Bearer <TOKEN>`

**请求参数**:

| 参数          | 类型   | 必填 | 说明    |
| ----------- | ---- | -- | ----- |
| targetUserId | long | 是  | 目标用户ID |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "huiHuaHao": "abc123xyz",
    "yongHuId1": 1,
    "yongHuId2": 2,
    "chuangJianShiJian": "2024-01-01 10:00:00"
  }
}
```

***

### 10.2 获取会话列表

**接口**: `GET /api/im/sessions`

**请求头**: `Authorization: Bearer <TOKEN>`

**请求参数**:

| 参数    | 类型  | 必填 | 说明        |
| ------ | --- | -- | --------- |
| page   | int  | 否  | 页码，默认0    |
| size   | int  | 否  | 每页数量，默认10 |

***

### 10.3 获取消息记录

**接口**: `GET /api/im/messages/{sessionId}`

**请求头**: `Authorization: Bearer <TOKEN>`

**路径参数**:

| 参数       | 类型     | 说明     |
| -------- | ------ | ------ |
| sessionId | string | 会话ID   |

**请求参数**:

| 参数    | 类型  | 必填 | 说明        |
| ------ | --- | -- | --------- |
| page   | int  | 否  | 页码，默认0    |
| size   | int  | 否  | 每页数量，默认20 |

***

### 10.4 发送消息

**接口**: `POST /api/im/message/send`

**请求头**: `Authorization: Bearer <TOKEN>`

**请求体**:

| 参数          | 类型     | 必填 | 说明    |
| ----------- | ------ | -- | ----- |
| sessionId   | string | 是  | 会话ID   |
| targetUserId | long   | 是  | 接收者ID  |
| content     | string | 是  | 消息内容  |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "huiHuaHao": "abc123xyz",
    "faSongRenId": 1,
    "shouDaoRenId": 2,
    "neiRong": "您好，请问账号还在吗？",
    "shiFouYiDu": 0,
    "faSongShiJian": "2024-01-01 10:05:00"
  }
}
```

***

## 11. 订单验收

### 11.1 发起验号

**接口**: `POST /api/orders/{dingDanHao}/inspect-start`

**请求头**: `Authorization: Bearer <TOKEN>`

**路径参数**:

| 参数         | 类型     | 说明   |
| ---------- | ------ | ---- |
| dingDanHao | string | 订单号  |

***

### 11.2 确认收货

**接口**: `PUT /api/orders/{dingDanHao}/confirm`

**请求头**: `Authorization: Bearer <TOKEN>`

**路径参数**:

| 参数         | 类型     | 说明   |
| ---------- | ------ | ---- |
| dingDanHao | string | 订单号  |

***

### 11.3 拒绝收货

**接口**: `PUT /api/orders/{dingDanHao}/reject`

**请求头**: `Authorization: Bearer <TOKEN>`

**路径参数**:

| 参数         | 类型     | 说明   |
| ---------- | ------ | ---- |
| dingDanHao | string | 订单号  |

**请求参数**:

| 参数    | 类型     | 必填 | 说明     |
| ------ | ------ | -- | ------ |
| reason | string | 是  | 拒绝原因   |

***

## 12. 退款模块

### 12.1 申请退款

**接口**: `POST /api/orders/{dingDanHao}/refund`

**请求头**: `Authorization: Bearer <TOKEN>`

**路径参数**:

| 参数         | 类型     | 说明   |
| ---------- | ------ | ---- |
| dingDanHao | string | 订单号  |

**请求参数**:

| 参数         | 类型     | 必填 | 说明     |
| ---------- | ------ | -- | ------ |
| reason     | string | 是  | 退款原因   |
| evidenceUrl | string | 否  | 凭证URL   |

***

### 12.2 查询退款状态

**接口**: `GET /api/orders/{dingDanHao}/refund-status`

**请求头**: `Authorization: Bearer <TOKEN>`

**路径参数**:

| 参数         | 类型     | 说明   |
| ---------- | ------ | ---- |
| dingDanHao | string | 订单号  |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "dingDanHao": "ORD202401010001",
    "tuiKuanYuanYin": "账号与描述不符",
    "tuiKuanJinE": 50.00,
    "zhuangTai": 0,
    "shenQingShiJian": "2024-01-01 11:00:00"
  }
}
```

**状态说明**:

| 状态码 | 说明      |
| ---- | ------- |
| 0    | 待处理     |
| 1    | 已退款     |
| 2    | 卖家拒绝    |
| 3    | 管理员拒绝   |

***

### 12.3 卖家处理退款

**接口**: `PUT /api/orders/{dingDanHao}/refund/seller`

**请求头**: `Authorization: Bearer <TOKEN>`

**路径参数**:

| 参数         | 类型     | 说明   |
| ---------- | ------ | ---- |
| dingDanHao | string | 订单号  |

**请求参数**:

| 参数   | 类型      | 必填 | 说明     |
| ---- | ------- | -- | ------ |
| agree | boolean | 是  | 是否同意退款 |

***

### 12.4 管理员处理退款

**接口**: `PUT /api/admin/orders/{dingDanHao}/refund`

**请求头**: `Authorization: Bearer <TOKEN>`（管理员）

**路径参数**:

| 参数         | 类型     | 说明   |
| ---------- | ------ | ---- |
| dingDanHao | string | 订单号  |

**请求参数**:

| 参数    | 类型      | 必填 | 说明     |
| ------ | ------- | -- | ------ |
| agree  | boolean | 是  | 是否同意退款 |
| remark | string  | 否  | 处理备注   |

***

## 13. 钱包与提现

### 13.1 查询钱包余额

**接口**: `GET /api/wallet/balance`

**请求头**: `Authorization: Bearer <TOKEN>`

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "yongHuId": 1,
    "yuE": 100.00,
    "keTiXianJinE": 80.00
  }
}
```

***

### 13.2 申请提现

**接口**: `POST /api/withdraw`

**请求头**: `Authorization: Bearer <TOKEN>`

**请求体**:

| 参数      | 类型      | 必填 | 说明       |
| ------- | ------- | -- | -------- |
| amount  | decimal | 是  | 提现金额    |
| method  | string  | 是  | 收款方式（alipay/wechat/bank） |
| account | string  | 是  | 收款账号    |
| name    | string  | 是  | 收款人姓名   |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "tiXianHao": "TX202401010001",
    "tiXianJinE": 50.00,
    "zhuangTai": 0,
    "shenQingShiJian": "2024-01-01 12:00:00"
  }
}
```

***

### 13.3 获取提现记录

**接口**: `GET /api/withdraw/list`

**请求头**: `Authorization: Bearer <TOKEN>`

**请求参数**:

| 参数    | 类型  | 必填 | 说明        |
| ------ | --- | -- | --------- |
| page   | int  | 否  | 页码，默认0    |
| size   | int  | 否  | 每页数量，默认10 |

***

## 14. IM 管理后台

### 14.1 获取所有会话

**接口**: `GET /api/admin/im/sessions`

**请求头**: `Authorization: Bearer <TOKEN>`（管理员）

**请求参数**:

| 参数    | 类型  | 必填 | 说明        |
| ------ | --- | -- | --------- |
| page   | int  | 否  | 页码，默认0    |
| size   | int  | 否  | 每页数量，默认20 |

***

### 14.2 查看会话消息

**接口**: `GET /api/admin/im/messages/{sessionId}`

**请求头**: `Authorization: Bearer <TOKEN>`（管理员）

**路径参数**:

| 参数       | 类型     | 说明     |
| -------- | ------ | ------ |
| sessionId | string | 会话ID   |

***

### 14.3 删除会话

**接口**: `DELETE /api/admin/im/sessions/{sessionId}`

**请求头**: `Authorization: Bearer <TOKEN>`（管理员）

**路径参数**:

| 参数       | 类型     | 说明     |
| -------- | ------ | ------ |
| sessionId | string | 会话ID   |

***

### 14.4 搜索消息

**接口**: `GET /api/admin/im/search`

**请求头**: `Authorization: Bearer <TOKEN>`（管理员）

**请求参数**:

| 参数      | 类型     | 必填 | 说明     |
| ------- | ------ | -- | ------ |
| keyword | string | 是  | 搜索关键词 |
| page    | int    | 否  | 页码     |
| size    | int    | 否  | 每页数量   |

***

### 14.5 获取统计数据

**接口**: `GET /api/admin/im/statistics`

**请求头**: `Authorization: Bearer <TOKEN>`（管理员）

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalSessions": 100,
    "totalMessages": 5000,
    "todayMessages": 100,
    "activeUsers": 200
  }
}
```

***

## ⚙️ 配置说明

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sanJiaoZhouXingDong
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 支付配置

```yaml
# 易宝支付
yeepay:
  merchant-id: your_merchant_id
  secret-key: your_secret_key
  notify-url: http://your-domain/api/yeepay/notify

# 芝麻信用分
alipay:
  credit:
    app-id: your_app_id
    private-key: your_private_key
    public-key: your_public_key

# 微信支付分
wechat:
  score:
    app-id: your_app_id
    mch-id: your_mch_id
    api-key: your_api_key
```

### JWT 配置

```yaml
jwt:
  secret: your_jwt_secret_key
  expire: 86400000
```

***

## 🔒 安全说明

1. **JWT Token 认证**: 所有接口均需认证（注册、登录除外）
2. **密码加密**: 采用 BCrypt 加密存储
3. **敏感信息**: 通过环境变量配置
4. **频率限制**: 接口访问频率控制

***

## 📝 开发规范

- 遵循 RESTful API 设计规范
- 使用 Lombok 简化代码
- 异常统一处理
- 日志规范记录

***

## 📄 许可证

MIT License
