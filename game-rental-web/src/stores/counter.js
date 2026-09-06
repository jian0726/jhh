/**
 * Pinia状态管理Store - 计数器示例
 * 
 * 该文件演示了如何使用Pinia创建全局状态管理。
 * Pinia是Vue 3推荐的状态管理库，替代了Vuex。
 * 
 * Store特点：
 * - 使用Composition API风格定义
 * - 支持响应式状态(ref)和计算属性(computed)
 * - 提供修改状态的方法(actions)
 * 
 * 当前实现了一个简单的计数器功能，包含：
 * - count: 计数值
 * - doubleCount: 计数的双倍值（计算属性）
 * - increment: 增加计数的方法
 */

import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

/**
 * 定义名为'counter'的Store
 * 
 * @returns {Object} 返回store的状态和方法
 * - count: 响应式引用，存储当前计数值
 * - doubleCount: 计算属性，自动根据count变化
 * - increment: 函数，用于增加count值
 */
export const useCounterStore = defineStore('counter', () => {
  // 定义响应式状态：计数器初始值为0
  const count = ref(0)
  
  // 定义计算属性：返回count的双倍值
  // 当count变化时，doubleCount会自动更新
  const doubleCount = computed(() => count.value * 2)
  
  /**
   * 增加计数器的方法
   * 每次调用将count值加1
   */
  function increment() {
    count.value++
  }

  // 返回所有需要在组件中使用的状态和方法
  return { count, doubleCount, increment }
})
