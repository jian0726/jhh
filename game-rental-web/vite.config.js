// 导入 Node.js 内置模块，用于处理文件路径
import { fileURLToPath, URL } from 'node:url'

// 导入 Vite 核心配置函数
import { defineConfig } from 'vite'
// 导入 Vue 插件，支持 .vue 文件的编译
import vue from '@vitejs/plugin-vue'
// 导入 JSX 插件，支持 Vue 项目中使用 JSX/TSX 语法
import vueJsx from '@vitejs/plugin-vue-jsx'
// 导入 Vue DevTools 插件，增强开发调试体验
import vueDevTools from 'vite-plugin-vue-devtools'
// 导入自动导入插件，自动导入 Vue 组合式 API 等
import AutoImport from 'unplugin-auto-import/vite'
// 导入组件自动注册插件
import Components from 'unplugin-vue-components/vite'
// 导入 Element Plus 解析器，支持 Element Plus 组件的自动导入
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// 导出 Vite 配置对象
export default defineConfig({
  // 插件配置数组
  plugins: [
    // Vue 官方插件，处理 .vue 文件
    vue(),
    // Vue JSX 支持插件
    vueJsx(),
    // Vue DevTools 调试工具
    vueDevTools(),
    // 自动导入配置：自动导入 Vue API 及 Element Plus 相关函数
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    // 组件自动注册配置：自动识别并注册 Element Plus 组件
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  // 路径解析配置
  resolve: {
    alias: {
      // 配置路径别名 @ 指向项目根目录下的 src 文件夹
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  // 开发服务器配置
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  },
})
