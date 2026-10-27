# 三角洲行动 · 游戏账号租赁平台（后端）

基于 Spring Boot 3.2 的游戏账号租赁平台后端服务。覆盖用户认证、商品上架与审核、订单全流程、退款与钱包、风控、站内消息、IM 私信、管理后台等模块。

> 接口文档（运行时）：启动后访问 `http://localhost:8080/swagger-ui.html`
> API JSON：`http://localhost:8080/api-docs`

---

## 1. 技术栈

| 技术 | 版本 | 说明 |
| --- | --- | --- |
| Java | **17** | `pom.xml` 中 `<java.version>17</java.version>` |
| Spring Boot | 3.2.0 | 应用框架 |
| Spring Security | 6.2 | 无状态 JWT 认证 + 路由级 RBAC |
| Spring Data JPA | 3.2 | ORM（Hibernate 6.3.1） |
| MySQL | 8.0+ | 数据库 `sanJiaoZhouXingDong` |
| HikariCP | 5.0.1 | 连接池 |
| jjwt | 0.12.3 | JWT 签发与验签 |
| springdoc-openapi | 2.3.0 | Swagger UI |
| Lombok | 1.18.30 | 代码简化 |
| Maven | 3.8+ | 构建 |

---

## 2. 快速开始

### 2.1 环境要求

- JDK 17（本机路径示例：`C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot`）
- MySQL 8.0+，账号 `root` / 密码 `123456`（可通过环境变量覆盖，见 §6）
- Maven 3.8+

### 2.2 初始化数据库

脚本为**幂等**设计（`CREATE DATABASE IF NOT EXISTS` + `CREATE TABLE IF NOT EXISTS`），可以重复执行：

```bash
mysql -uroot -p123456 --default-character-set=utf8mb4 < sql/init.sql
```

脚本会：
1. 创建库 `sanJiaoZhouXingDong`（utf8mb4）
2. 创建全部 16 张表
3. 写入种子数据：`yonghu`（admin）、`fengkong_rule`（默认风控规则）

> 表名与列名全部为 **snake_case**，与 JPA 实体的默认命名策略一致，因此应用启动时 Hibernate **不会**再对结构做任何变更。

### 2.3 启动

```bash
# 方式一：命令行
export JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot'
mvn spring-boot:run

# 方式二：IDEA 直接运行 GameRentalApplication
```

启动成功标志：

```
Tomcat started on port 8080 (http) with context path ''
Started GameRentalApplication in X.XXX seconds
```

### 2.4 默认账号

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| admin | admin123 | 管理员（`jueSe = 2`） |

---

## 3. 项目结构

```
src/main/java/com/delta/rental/
├── config/          # 安全、跨域、Swagger、第三方支付配置、演示数据种子
│   ├── SecurityConfig.java          # 路由级 RBAC（唯一鉴权入口）
│   ├── JwtAuthenticationFilter.java # 解析并验签 JWT
│   └── DemoDataSeeder.java          # 启动时补演示商品/轮播（可关闭）
├── security/        # SecurityUtils：取当前登录用户（已验签）
├── controller/      # 22 个 REST 控制器
├── service/         # 19 个业务服务
├── repository/      # 16 个 JPA 仓库
├── entity/          # 16 个实体
├── dto/             # 请求/响应对象
├── exception/       # 全局异常处理 + 业务异常
└── util/            # JwtUtil、加密等

sql/init.sql         # 建库 + 建表 + 种子数据（幂等）
uploads/             # 图片上传目录（运行时生成，不入库）
```

### 数据表（16 张）

| 表 | 说明 |
| --- | --- |
| `yonghu` | 用户 |
| `shangpin` | 商品（出租的游戏账号） |
| `dingdan` | 订单 |
| `lunbotu` | 首页轮播 |
| `shou_cang` | 收藏 |
| `pingjia` | 评价 |
| `liu_lan_li_shi` | 浏览历史 |
| `xiaoxi` | 站内消息 |
| `im_hui_hua` / `im_xiao_xi` | IM 会话 / 消息 |
| `qian_bao` | 钱包 |
| `ti_xian_shen_qing` | 提现申请 |
| `tui_kuan_shen_qing` | 退款申请 |
| `fengkong_rule` | 风控规则 |
| `fengxian` | 风险事件记录 |
| `guanliyuan` | 管理员操作日志 |

---

## 4. 认证与权限

### 4.1 认证方式

登录成功后拿到 JWT，业务接口在请求头携带：

```
Authorization: Bearer <JWT_TOKEN>
```

- 令牌由 `JwtUtil` 用 HS384 签名（密钥来自 `jwt.secret`）
- `JwtAuthenticationFilter` **会校验签名**，被篡改的令牌一律视为未登录
- 令牌中的 `userId` 写入 `SecurityContext` 的 principal，控制器通过
  `SecurityUtils.getCurrentUserId()` 获取，**不再自行解析 Authorization 头**

### 4.2 角色

| `jueSe` | 权限 |
| --- | --- |
| 0 | `ROLE_USER` |
| 1 | `ROLE_SELLER` |
| 2 | `ROLE_ADMIN` |

### 4.3 访问控制矩阵

鉴权规则集中定义在 `config/SecurityConfig.java`。

**匿名可访问（游客）**

| 路径 | 说明 |
| --- | --- |
| `POST /api/auth/denglu`、`/zhuce`、`/fasong-yanzhengma`、`/send-email-code`、`/reset-password` | 登录注册找回密码 |
| `GET /api/auth/yonghu/{id}` | 用户公开信息 |
| `GET /api/lunbotu` | 首页轮播（仅启用中） |
| `GET /api/shangpin/**` | 商品浏览、搜索、筛选 |
| `POST /api/shangpin/filter` | 高级筛选 |
| `GET /api/pingjia/shangpin/**` | 商品评价与评分 |
| `GET /api/captcha`、`POST /api/captcha/verify`、`POST /api/fingerprint`、`POST /api/fengkong/check` | 登录前风控与验证码 |
| `GET /api/oauth/**`、`POST /api/yeepay/notify`、`GET /api/yeepay/return`、`POST /api/wechat-score/notify` | 第三方回调 |
| `/uploads/**`、`/swagger-ui/**`、`/api-docs/**` | 静态资源与文档 |

**仅管理员（ROLE_ADMIN）**

- `/api/admin/**`（含用户封禁、角色调整、商品审核、退款仲裁、IM 后台）
- `GET /api/lunbotu/all`、`POST/PUT/DELETE /api/lunbotu/**`
- `PUT /api/shangpin/{id}/tongguo`、`PUT /api/shangpin/{id}/jujue`
- `/api/fengkong/rules/**`、`/api/fengkong/statistics`、`/api/fengkong/events/**`

**其余接口**：必须登录。

失败响应统一为信封格式，前端可直接取 `message`：

```json
{"code":401,"message":"请先登录后再操作","data":{"success":false,"message":"请先登录后再操作"}}
{"code":403,"message":"没有权限执行该操作","data":{"success":false,"message":"没有权限执行该操作"}}
```

### 4.4 敏感字段保护

以下字段标记为 `JsonProperty.Access.WRITE_ONLY`，**只允许写入、绝不会出现在任何响应里**：

| 字段 | 原因 |
| --- | --- |
| `yonghu.miMa` | 登录密码（BCrypt 哈希） |
| `shangpin.youXiZhangHao` / `youXiMiMa` | 商品绑定的游戏账号密码 |
| `dingdan.linShiZhangHao` / `linShiMiMa` | 订单临时登录凭证 |

租客只能在租期内通过 `GET /api/dingdan/{dingDanHao}/credential` 获取临时凭证（该接口会校验订单归属与状态）。

---

## 5. API 一览

统一响应格式：

```json
{ "code": 200, "message": "success", "data": {} }
```

业务异常同样以真实 HTTP 状态码返回（400/401/403/404/500），`data` 中带有 `message`。

### 5.1 账号与认证 `/api/auth`

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/denglu` | 匿名 | 登录（支持用户名/手机号/邮箱），返回 `token` + `yongHu` |
| POST | `/zhuce` | 匿名 | 注册（手机号模式或邮箱模式，邮箱模式需验证码） |
| GET | `/fasong-yanzhengma` | 匿名 | 发送短信验证码（开发环境直接返回验证码） |
| POST | `/send-email-code` | 匿名 | 发送邮箱验证码 |
| POST | `/reset-password` | 匿名 | 凭邮箱验证码重置密码 |
| GET | `/yonghu/{id}` | 匿名 | 查询用户公开信息（脱敏） |
| POST | `/shiming` | 登录 | 提交实名认证 |
| PUT | `/yonghu` | 登录 | 修改用户名 / 手机号 |
| PUT | `/yonghu/mima` | 登录 | 修改密码 |
| GET | `/yonghu/shiming` | 登录 | 查询实名状态 |

### 5.2 商品 `/api/shangpin`

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `` | 匿名 | 分页获取在售商品 `?page=1&limit=16` |
| GET | `/{id}` | 匿名 | 商品详情（前端格式） |
| GET | `/shangjia` | 匿名 | 商家全部在售商品 |
| GET | `/sousuo` | 匿名 | 关键词搜索 `?keyword=` |
| GET | `/search` | 匿名 | 分页搜索 |
| GET | `/search/boolean` | 匿名 | 布尔模式全文检索 |
| GET | `/filter` | 匿名 | 按价格 / 最短租期筛选 |
| POST | `/filter` | 匿名 | 高级筛选 `{filter, page, limit}` |
| POST | `` | 登录 | 上架商品 |
| GET | `/maijia` | 登录 | 我上架的商品 |
| GET | `/my` | 登录 | 我的商品（前端格式） |
| PUT | `/{id}` | 登录 | 更新商品 |
| DELETE | `/{id}` | 登录 | 删除商品 |
| PUT | `/{id}/xiajia` | 登录 | 下架商品 |
| POST | `/upload-image` | 登录 | 上传商品封面图 |
| PUT | `/{id}/tongguo` | 管理员 | 审核通过 |
| PUT | `/{id}/jujue` | 管理员 | 审核拒绝 |

### 5.3 轮播图 `/api/lunbotu`

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/lunbotu` | 匿名 | 首页轮播（仅 `zhuangTai=1`，按 `shunXu` 升序） |
| GET | `/api/lunbotu/all` | 管理员 | 全部轮播（含停用） |
| POST | `/api/lunbotu` | 管理员 | 新增 |
| PUT | `/api/lunbotu/{id}` | 管理员 | 修改 |
| DELETE | `/api/lunbotu/{id}` | 管理员 | 删除（同时清理磁盘图片） |

### 5.4 订单 `/api/dingdan`

订单状态 `zhuangTai`：`0` 待付款 → `1` 租用中 → `2` 已完成 → `3` 已取消 → `4` 纠纷中。

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `` | 登录 | 买家下单 |
| GET | `` | 登录 | 我的订单列表 |
| GET | `/{dingDanHao}` | 登录 | 订单详情 |
| POST | `/{dingDanHao}/fukuan` | 登录 | 付款 |
| POST | `/{dingDanHao}/wancheng` | 登录 | 完成订单 |
| POST | `/{dingDanHao}/quxiao` | 登录 | 取消订单 |
| GET | `/maijia` | 登录 | 我作为卖家的订单 |
| GET | `/maimai/{maiJiaId}` | 登录 | 按卖家 ID 查询订单 |
| GET | `/shangpin/{shangPinId}` | 登录 | 按商品查询订单 |
| GET | `/{dingDanHao}/credential` | 登录 | 获取临时登录凭证（校验归属与状态） |
| GET | `/{dingDanHao}/remaining` | 登录 | 租期剩余时间 |
| POST | `/{dingDanHao}/inspect-start` | 登录 | 发起验号 |
| PUT | `/{dingDanHao}/confirm` | 登录 | 确认收货 |
| PUT | `/{dingDanHao}/reject` | 登录 | 拒绝收货（触发纠纷） |
| PUT | `/{dingDanHao}/return` | 登录 | 归还账号 |
| POST | `/{dingDanHao}/jiufeng` | 登录 | 发起纠纷申诉 |
| POST | `/{dingDanHao}/chuli` | 登录 | 处理纠纷申诉 |

### 5.5 纠纷 `/api/disputes`

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/my` | 登录 | 我的纠纷列表 |
| GET | `/{id}` | 登录 | 纠纷详情 |
| POST | `` | 登录 | 提交纠纷 |
| POST | `/{id}/evidence` | 登录 | 上传证据 |

### 5.6 退款与钱包

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/orders/{dingDanHao}/refund` | 登录 | 申请退款 |
| GET | `/api/orders/{dingDanHao}/refund-status` | 登录 | 退款进度 |
| GET | `/api/refunds` | 登录 | 我的退款申请 |
| PUT | `/api/orders/{dingDanHao}/refund/seller` | 登录 | 卖家处理退款 |
| PUT | `/api/admin/orders/{dingDanHao}/refund` | 管理员 | 管理员仲裁退款 |
| POST | `/api/withdraw` | 登录 | 申请提现 |
| GET | `/api/withdraw/list` | 登录 | 提现记录 |
| GET | `/api/wallet/balance` | 登录 | 钱包余额 |

### 5.7 其余模块

| 模块 | 基础路径 | 权限 | 主要接口 |
| --- | --- | --- | --- |
| 评价 | `/api/pingjia` | 部分匿名 | `GET /shangpin/{id}`（匿名列表）、`GET /shangpin/{id}/pingfen`（匿名评分）、`POST /`、`GET /yonghu` |
| 收藏 | `/api/favorites` | 登录 | `POST /`、`GET /`、`DELETE /{shangPinId}` |
| 浏览历史 | `/api/history` | 登录 | `GET /browse`、`DELETE /browse` |
| 站内消息 | `/api/xiaoxi` | 登录 | `GET /`、`GET /weidu`、`GET /weidu/count`、`PUT /{id}/dushu`、`PUT /quanbudushu` |
| IM 私信 | `/api/im` | 登录 | `POST /session/create`、`GET /sessions`、`GET /messages/{sessionId}`、`POST /message/send` |
| 风控 | `/api/fengkong` | 混合 | `POST /check`（匿名）、`GET /rules`（管理员）、`PUT /rules/{id}`（管理员）、`GET /statistics`（管理员）、`GET /events`（登录） |
| 指纹与验证码 | `/api/captcha`、`/api/fingerprint`、`/api/risk/status` | 混合 | `GET /captcha`（匿名）、`POST /captcha/verify`（匿名）、`POST /fingerprint`（匿名）、`GET /risk/status`（登录） |
| 文件上传 | `/api/upload` | 登录 | `POST /image` |
| 支付 | `/api/pay`、`/api/yeepay` | 混合 | `POST /api/pay`、`GET /api/pay/status/{dingDanHao}`、`POST /api/yeepay/pay` |
| 第三方信用 | `/api/alipay-credit`、`/api/wechat-score` | 登录 | `GET /score`、`POST /authorize`、`POST /order` |
| 第三方登录 | `/api/oauth` | 匿名 | `GET /auth-url`、`POST /callback` |
| 管理后台 | `/api/admin` | 管理员 | `GET /users`、`PUT /users/{id}/ban`、`GET /products`、`PUT /products/{id}/audit`、`GET /orders`、`GET /disputes`、`PUT /disputes/{id}/judge`、`GET /dashboard`、`POST /notice`、`GET /tongji`、`GET /yonghu`、`PUT /yonghu/{id}/fengjin`、`PUT /yonghu/{id}/jiefeng`、`PUT /yonghu/{id}/jueses`、`GET /im/sessions` 等 |

---

## 6. 配置说明

`src/main/resources/application.yml` 中**所有敏感项都走环境变量**，冒号后为本地开发默认值。

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/sanJiaoZhouXingDong?...` | 数据库连接 |
| `DB_USERNAME` | `root` | 数据库用户 |
| `DB_PASSWORD` | `123456` | 数据库密码 |
| `JWT_SECRET` | 内置开发密钥 | **生产必须覆盖**，长度 ≥ 32 字节 |
| `JWT_EXPIRATION` | `86400000` | 令牌有效期（毫秒，默认 24h） |
| `CORS_ALLOWED_ORIGINS` | `*` | 允许跨域的来源，逗号分隔 |
| `SEED_DEMO` | `true` | 是否在表为空时写入演示商品/轮播 |
| `SQL_SHOW` | `false` | 是否打印 SQL |
| `SQL_LOG_LEVEL` | `info` | `debug` 可查看 SQL |

生产环境示例：

```bash
export DB_PASSWORD='<强密码>'
export JWT_SECRET='<至少32字节的随机字符串>'
export CORS_ALLOWED_ORIGINS='https://your-frontend-domain.com'
export SEED_DEMO=false
```

第三方登录 / 支付（微信、QQ、易宝、芝麻信用、微信支付分）的配置模板见仓库根目录的
`application-example.yml`，需要时把对应片段合并进 `application.yml` 并填入真实密钥。
**当前 `application.yml` 未包含这些配置段**，相关功能在未配置时会返回失败而不是崩溃。

---

## 7. 常见问题

**Q：命令行 `mvn` 报「找不到或无法加载主类 Launcher」？**
本机 choco 安装的 Maven shim 有问题，直接用全路径：

```bash
export JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot'
"/c/ProgramData/chocolatey/lib/maven/apache-maven-3.9.14/bin/mvn.cmd" spring-boot:run
```

注意 `JAVA_HOME` 必须用**反斜杠**路径，正斜杠会被判定为无效。

**Q：启动报 `Unknown database 'sanjiaozhouxingdong'`？**
先执行 `sql/init.sql`（见 §2.2）。

**Q：表结构与实体不一致？**
`init.sql` 与实体均为 snake_case。若曾用旧版脚本建表出现 camelCase + snake_case 双列，
删库后重新执行 `sql/init.sql` 即可。

---

## 8. 安全注意事项

1. **不要把 `application.yml` 的真实密钥提交到仓库** —— 用环境变量覆盖。
2. 令牌验签在 `JwtAuthenticationFilter`，控制器一律通过 `SecurityUtils` 取用户，禁止自行解析 JWT。
3. 新增接口时**必须**同步在 `SecurityConfig` 中声明权限，否则会落到 `anyRequest().authenticated()`（默认需登录，偏安全）。
4. 敏感字段请加 `@JsonProperty(access = WRITE_ONLY)`。
