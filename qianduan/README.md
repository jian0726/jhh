# 三角洲行动 · 游戏账号租赁平台（前端）

Vue 3 + Vite 单页应用，对接 `game-rental-backend`（Spring Boot，默认 `http://localhost:8080`）。

---

## 1. 技术栈（已安装且验证可用的版本）

| 技术 | package.json 声明 | **实际已安装** | 说明 |
| --- | --- | --- | --- |
| Vue | `beta` | **3.6.0-beta.13** | 组合式 API + `<script setup>` |
| Vue Router | `^5.0.4` | 5.0.7 | 前端路由 |
| Vite | `^8.0.8` | 8.0.14 | 构建 / 开发服务器 |
| Element Plus | `^2.14.1` | 2.14.1 | UI 组件库（自动按需导入） |
| Pinia | `^3.0.4` | 3.0.4 | 状态管理 |
| Axios | `^1.16.1` | 1.16.1 | HTTP 客户端 |

> ⚠️ `vue` 与 `vue-router`、`vite` 均为**预发布/大版本更新**（Vue 3.6 beta、Router 5、Vite 8）。
> `node_modules` 已安装可直接运行；若要重装依赖，建议把 `package.json` 中的版本**锁定为上表「实际已安装」的具体版本**，避免浮动的 `beta` 标签拉到新版本后构建失败。

其他开发依赖：ESLint 10 + oxlint、Prettier/oxfmt、Vitest 4、Playwright 1.59。

---

## 2. 快速开始

```bash
npm install          # 依赖已安装时跳过
npm run dev          # 启动开发服务器，默认 http://localhost:5173
```

启动前请确认**后端已在 8080 端口运行**，否则页面能打开但列表为空、并提示「网络异常」。

### 可用脚本

| 命令 | 说明 |
| --- | --- |
| `npm run dev` | 开发服务器（热更新） |
| `npm run build` | 生产构建，产物在 `dist/` |
| `npm run preview` | 本地预览构建产物 |
| `npm run lint` | oxlint + ESLint 自动修复 |
| `npm run format` | oxfmt 格式化 `src/` |
| `npm run test:unit` | Vitest 单元测试 |
| `npm run test:e2e` | Playwright 端到端测试 |

---

## 3. 目录结构

```
src/
├── main.js                    # 应用入口
├── App.vue                    # 根组件（含全局登录弹窗）
├── router/index.js            # 路由表与守卫
├── api/                       # 接口封装（按后端模块划分）
│   ├── auth.js                # /api/auth/*
│   ├── product.js             # /api/shangpin/*
│   ├── order.js               # /api/dingdan/*
│   ├── favorites.js           # /api/favorites/*
│   ├── message.js             # /api/xiaoxi/*
│   └── carousel.js            # /api/lunbotu
├── utils/
│   ├── request.js             # Axios 实例：自动带 token、统一解信封、401 处理
│   ├── imageUrl.js            # 图片 URL 解析
│   └── jwt.js                 # 令牌解析工具
├── stores/                    # Pinia：auth / user / product
├── components/                # 按页面分组的组件
│   ├── Shou_Ye/               # 首页：轮播、商品列表、搜索、导航
│   ├── Shang_Pin_Xiang_Qing/  # 商品详情：图片、属性、租金选择、支付卡
│   ├── Shang_Jia_Ye_Mian/     # 上架：表单、图片上传、结算信息、协议
│   ├── Ge_Ren_Zhong_Xin/      # 个人中心：侧边导航
│   ├── Deng_Lu/               # 登录 / 注册 / 找回密码表单
│   └── common/                # 通用按钮、页面标题
└── 页面（路由级）
    ├── Shou_Ye.vue            # 首页
    ├── Shang_Pin_Xiang_Qing.vue  # 商品详情
    ├── Shang_Jia_Ye_Mian.vue  # 上架商品
    ├── Ge_Ren_Zhong_Xin.vue   # 个人中心
    └── Shang_Ping_Shai_Xuan.vue  # 筛选结果
```

### 路由表

| 路径 | 名称 | 页面 |
| --- | --- | --- |
| `/` | home | 首页（轮播 + 商品列表 + 搜索） |
| `/product/:id` | productDetail | 商品详情 |
| `/shelf` | shelf | 上架商品 |
| `/personal` | personal | 个人中心 |
| `/filter` | filter | 筛选结果 |
| `/:pathMatch(.*)*` | notFound | 兜底（重定向首页） |

---

## 4. 接口对接

### 4.1 后端地址

由环境变量控制（`src/utils/request.js` 读取 `VITE_API_BASE_URL`）：

```
.env.development   VITE_API_BASE_URL=http://localhost:8080
.env.production    VITE_API_BASE_URL=http://localhost:8080   ← 部署前必须改
```

> **部署前必改**：`.env.production` 目前仍指向 `localhost:8080`，打包后会在用户浏览器里请求
> 用户本机的 8080，必须改为真实后端地址（或改成空值并配合 Nginx 反向代理）。

### 4.2 请求与响应约定

`utils/request.js` 已统一处理：

- **自动携带令牌**：`localStorage.token` → `Authorization: Bearer <token>`
- **自动解信封**：后端返回 `{code, message, data}` 时，直接把 `data` 交给调用方
- **错误归一化**：抛出 `Error`，`message` 取后端 `message`，并保留 `error.response`
- **401 处理**：仅当本地存在 token 时才清除并跳首页；`/auth/*` 接口的失败不做跳转

调用示例：

```js
import productApi from '@/api/product'

const { data, total } = await productApi.getProducts({ page: 1, limit: 16 })
```

### 4.3 已对接的接口（30 个唯一路径）

```
/api/auth/denglu              /api/shangpin                  /api/favorites
/api/auth/zhuce               /api/shangpin/:id              /api/favorites/:id
/api/auth/fasong-yanzhengma   /api/shangpin/:id/xiajia       /api/xiaoxi
/api/auth/send-email-code     /api/shangpin/filter           /api/xiaoxi/weidu
/api/auth/reset-password      /api/shangpin/my               /api/xiaoxi/weidu/count
/api/auth/yonghu/:id          /api/shangpin/search           /api/xiaoxi/:id/dushu
/api/lunbotu                  /api/shangpin/upload-image     /api/xiaoxi/quanbudushu
/api/dingdan                  /api/dingdan/:id               /api/dingdan/:id/credential
/api/dingdan/:id/fukuan       /api/dingdan/:id/quxiao        /api/dingdan/:id/return
/api/dingdan/:id/confirm      /api/dingdan/:id/inspect-start /api/dingdan/maimai/:id
```

覆盖：登录/注册/找回密码、首页轮播与商品列表、商品搜索与高级筛选、商品详情、上架（含图片上传）、
个人中心（我的商品、订单、收藏、消息）。

### 4.4 尚未对接的接口（后端已实现）

以下模块后端接口已就绪，但**前端页面尚未开发**：管理后台、IM 私信聊天页、钱包与提现、
商品评价、纠纷申诉、退款申请、浏览历史页、风控与验证码、第三方登录、支付页、第三方信用分。

> 「联系客服」目前只是静态展示电话与营业时间，后端有完整的 IM 模块（9 个接口）但前端没有聊天页。

---

## 5. 已知遗留问题

| 问题 | 位置 | 说明 |
| --- | --- | --- |
| 重复请求 | 首页轮播与商品列表各自 `onMounted` 请求一次 | 属正常行为；控制台若见重复 SQL 多为热更新触发整页 reload |
| 依赖版本激进 | `package.json` | 见 §1 提示 |
| e2e 无用例 | `e2e/` | 脚手架自带的示例用例已删除，`npm run test:e2e` 目前会报「无测试文件」；如需端到端测试再补 |
| 生产地址未替换 | `.env.production` | `VITE_API_BASE_URL` 仍为 `localhost:8080`，部署前必须改 |

> 早期遗留的 Mock 分支（`src/mock/*`、`mockConfig.js`、各 `api/*.js` 的 `USE_MOCK` 判断）、
> 无引用的 `src/Deng_Lu.vue` 与 `src/1|2|3.html`、脚手架示例 `stores/counter.js` 与
> `components/common/InputField.vue` 均已在交付前清理，`src/` 下不再有未参与构建的文件。

---

## 6. 开发约定

1. **所有网络请求走 `utils/request.js`**，不要在组件里直接用 axios。
2. **新增接口写在 `src/api/` 对应模块里**，保持组件只调用 `xxxApi.yyy()`。
3. **登录态从 `stores/auth.js`（或 `localStorage`）读取**，不要在组件内散落判断。
4. **图片地址统一用 `utils/imageUrl.js`** 解析（兼容 `data:` / `blob:` / 绝对 URL / 相对路径）。
5. 组件命名与后端字段保持一致的拼音风格（如 `ShangPin`、`DingDan`），便于前后端对照。
