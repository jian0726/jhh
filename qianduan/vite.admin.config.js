/**
 * 管理后台独立构建/开发配置
 *
 * 目标：管理后台跑在自己的端口（默认 5174），该端口只提供后台页面，
 * 商城前台（index.html，5173）完全不暴露后台入口代码。
 *
 * 用法：
 *   npm run dev:admin     # 开发：http://localhost:5174/login
 *   npm run build:admin   # 构建：输出到 dist-admin/
 */
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

/**
 * 把任意前端路由（/login、/ 等）都指向 admin.html。
 * 不做这层改写的话，访问 /login 会命中根目录的 index.html（商城前台）。
 */
const adminHtmlFallback = () => {
  const shouldRewrite = (url) => {
    if (!url) return false
    // 静态资源、模块请求、Vite 内部路径一律放过
    if (url.startsWith('/@') || url.startsWith('/src') || url.startsWith('/node_modules')) return false
    if (url.includes('.')) return false
    return true
  }

  return {
    name: 'admin-html-fallback',
    configureServer(server) {
      server.middlewares.use((req, _res, next) => {
        if (shouldRewrite(req.url)) req.url = '/admin.html'
        next()
      })
    },
    configurePreviewServer(server) {
      server.middlewares.use((req, _res, next) => {
        if (shouldRewrite(req.url)) req.url = '/admin.html'
        next()
      })
    },
  }
}

export default defineConfig({
  // 独立的依赖预构建缓存，避免和商城前台互相覆盖
  cacheDir: 'node_modules/.vite-admin',
  plugins: [
    adminHtmlFallback(),
    vue(),
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5174,
    strictPort: true,
    // 只监听本机，后台不对外暴露
    host: '127.0.0.1',
  },
  preview: {
    port: 5174,
    strictPort: true,
  },
  build: {
    outDir: 'dist-admin',
    rollupOptions: {
      input: fileURLToPath(new URL('./admin.html', import.meta.url)),
    },
  },
})
