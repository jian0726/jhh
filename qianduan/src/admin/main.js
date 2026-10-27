/**
 * 管理后台独立入口
 *
 * 与商城前台（src/main.js）完全分开：
 * - 只注册后台用到的路由与页面，前台页面不会被打包进管理端
 * - 独立端口启动（见 vite.admin.config.js，默认 5174）
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'

import AdminApp from './AdminApp.vue'
import router from './router'
import 'element-plus/dist/index.css'
import '../styles/admin-base.css'

const app = createApp(AdminApp)

app.use(ElementPlus)
app.use(createPinia())
app.use(router)

app.mount('#admin-app')
