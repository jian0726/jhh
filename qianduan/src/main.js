/**
 * 应用程序入口文件
 *
 * 该文件是整个Vue应用的启动点，负责：
 * 1. 创建Vue应用实例
 * 2. 初始化Pinia状态管理
 * 3. 配置Vue Router路由系统
 * 4. 将应用挂载到DOM元素
 */

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'

import App from './App.vue'
import router from './router'
import 'element-plus/dist/index.css'

// 创建Vue应用实例，传入根组件App
const app = createApp(App)

// 安装Vue插件：Element Plus
app.use(ElementPlus)

// 安装Pinia状态管理插件，用于全局状态管理
app.use(createPinia())

// 安装Vue Router路由插件，用于页面导航和路由管理
app.use(router)

// 将Vue应用挂载到index.html中id为'app'的DOM元素上
app.mount('#app')
