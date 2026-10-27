/**
 * 路由配置文件
 *
 * 该文件定义了应用的所有路由规则，使用Vue Router进行页面导航管理。
 *
 * 路由结构：
 * - 首页 (/) -> ShouYe组件
 * - 商品详情页 (/product/:id) -> ShangPinXiangQing组件，支持动态商品ID参数
 *
 * 使用HTML5 History模式，URL更加美观，不含#符号
 */

import { createRouter, createWebHistory } from 'vue-router'
import ShouYe from '../Shou_Ye.vue'
import ShangPinXiangQing from '../Shang_Pin_Xiang_Qing.vue'
import ShangJiaYeMian from '../Shang_Jia_Ye_Mian.vue'
import GeRenZhongXin from '../Ge_Ren_Zhong_Xin.vue'
import ShangPingShaiXuan from '../Shang_Ping_Shai_Xuan.vue'
import { useUserStore } from '../stores/user'
import { useAuthStore } from '../stores/auth'

/**
 * 创建路由器实例
 *
 * 配置说明：
 * - history: 使用createWebHistory启用HTML5 History模式
 * - BASE_URL: 从环境变量获取基础路径，支持部署到子目录
 * - routes: 定义路由映射表
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    {
      path: '/', // 根路径，匹配首页
      name: 'home', // 路由名称，用于编程式导航
      component: ShouYe, // 对应的组件
    },
    {
      path: '/product/:id', // 商品详情路径，:id 为动态参数
      name: 'productDetail', // 路由名称
      component: ShangPinXiangQing, // 对应的组件
    },
    {
      path: '/shelf', // 上架页面路径
      name: 'shelf', // 路由名称
      component: ShangJiaYeMian, // 对应的组件
    },
    {
      path: '/personal', // 个人中心页面路径
      name: 'personal', // 路由名称
      component: GeRenZhongXin, // 对应的组件
    },
    {
      path: '/filter', // 筛选结果页面路径
      name: 'filter', // 路由名称
      component: ShangPingShaiXuan, // 对应的组件
    },
    {
      path: '/:pathMatch(.*)*', // 未匹配到的路径
      name: 'notFound', // 路由名称
      redirect: '/', // 统一回首页
    },
  ],
})

/**
 * 路由守卫
 *
 * 个人中心需要登录：未登录时弹出登录框并回到首页，
 * 而不是让用户停留在空白页面（项目使用弹窗登录，没有独立登录页）。
 */
router.beforeEach((to) => {
  if (to.name === 'personal') {
    const userStore = useUserStore()
    if (!userStore.isLogin) {
      const authStore = useAuthStore()
      authStore.openLogin()
      return { name: 'home' }
    }
  }

  return true
})

// 导出路由器实例，供main.js使用
export default router
