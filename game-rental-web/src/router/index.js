/**
 * 路由配置文件
 *
 * 该文件定义了应用的所有路由规则，使用Vue Router进行页面导航管理。
 *
 * 路由结构：
 * - 首页 (/) -> HomePage组件
 * - 商品详情页 (/product/:id) -> ProductDetail组件，支持动态商品ID参数
 *
 * 使用HTML5 History模式，URL更加美观，不含#符号
 */

import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../HomePage.vue'
import ProductDetail from '../ProductDetail.vue'
import ShangJiaYeMian from '../Shang_Jia_Ye_Mian.vue'
import PersonalCenter from '../PersonalCenter.vue'
import AuthPage from '../Auth.vue'
import { useUserStore } from '../stores/user'

/**
 * 创建路由器实例
 *
 * 配置说明：
 * - history: 使用createWebHistory启用HTML5 History模式
 * - BASE_URL: 从环境变量获取基础路径，支持部署到子目录
 * - routes: 定义路由映射表
 * - scrollBehavior: 切换路由时滚动到顶部
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
      meta: { requiresAuth: false } // 首页不需要登录
    },
    {
      path: '/product/:id',
      name: 'productDetail',
      component: ProductDetail,
      meta: { requiresAuth: false } // 商品详情不需要登录
    },
    {
      path: '/shelf',
      name: 'shelf',
      component: ShangJiaYeMian,
      meta: { requiresAuth: true } // 上架页面需要登录
    },
    {
      path: '/personal',
      name: 'personal',
      component: PersonalCenter,
      meta: { requiresAuth: true } // 个人中心需要登录
    },
    {
      path: '/login',
      name: 'login',
      component: AuthPage,
      meta: { requiresAuth: false } // 登录页面不需要登录
    },
    {
      path: '/register',
      name: 'register',
      component: AuthPage,
      meta: { requiresAuth: false } // 注册页面不需要登录
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'notFound',
      redirect: '/'
    }
  ],
})

/**
 * 路由守卫
 * 检查需要登录的页面，未登录则重定向到登录页
 */
router.beforeEach((to, from, next) => {
  // 访问登录/注册页面时，如果已登录，重定向到首页
  if ((to.name === 'login' || to.name === 'register')) {
    const userStore = useUserStore()
    if (userStore.isLogin) {
      next('/')
      return
    }
  }

  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    if (!userStore.isLogin) {
      // 未登录，重定向到登录页，并保存当前页面用于登录后跳转
      next({
        name: 'login',
        query: { redirect: to.fullPath }
      })
      return
    }
  }

  next()
})

// 导出路由器实例，供main.js使用
export default router
