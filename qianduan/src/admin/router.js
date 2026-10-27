/**
 * 管理后台路由
 *
 * 路由表里只有两个页面：
 * - /login  独立登录页（校验管理员角色）
 * - /       管理后台主页面
 *
 * 守卫规则（任何入口都要过一遍）：
 * 1. 未登录 → 跳登录页
 * 2. 已登录但角色不是管理员（jueSe !== 2）→ 清掉登录态并跳登录页
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import AdminLogin from './AdminLogin.vue'
import GuanLiHouTai from '../Guan_Li_Hou_Tai.vue'

const routes = [
  {
    path: '/login',
    name: 'adminLogin',
    component: AdminLogin,
    meta: { public: true },
  },
  {
    path: '/',
    name: 'adminHome',
    component: GuanLiHouTai,
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()

  if (to.meta.public) {
    // 已登录且是管理员，访问登录页直接进后台
    if (userStore.isLogin) {
      if (!userStore.userInfo) await userStore.restoreUserInfo()
      if (userStore.userInfo?.jueSe === 2) return { name: 'adminHome' }
    }
    return true
  }

  if (!userStore.isLogin) {
    return { name: 'adminLogin', query: { redirect: to.fullPath } }
  }

  // 刷新页面后 userInfo 为空，先用 token 反解一次
  if (!userStore.userInfo) {
    await userStore.restoreUserInfo()
  }

  if (userStore.userInfo?.jueSe !== 2) {
    userStore.logout()
    return { name: 'adminLogin', query: { denied: '1' } }
  }

  return true
})

export default router
