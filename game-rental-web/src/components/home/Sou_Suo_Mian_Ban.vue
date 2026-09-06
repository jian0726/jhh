<template>
  <!-- 
    搜索面板组件
    从顶部向下滑入的动画效果
    
    功能说明：
    - 使用Vue的transition组件实现展开/收起动画
    - v-show控制显示/隐藏，保持DOM结构以支持动画
    - role和aria-label提升无障碍访问体验
  -->
  <transition name="search-slide">
    <div
      v-show="isExpanded"
      class="search-expand"
      role="search"
      aria-label="搜索区域"
      ref="searchPanelRef"
      @click.stop
    >
      <div class="search-container" @click.stop>
        <!-- 
          搜索输入框
          - type="text"，文本输入类型
          - placeholder提供提示文字
          - ref绑定引用，用于自动聚焦和清空操作
          - @keyup.enter监听回车键，触发搜索
        -->
        <input
          type="text"
          class="search-input"
          placeholder="搜索商品..."
          aria-label="搜索输入框"
          ref="searchInputRef"
          @keyup.enter="handleSearch"
        />
        
        <!-- 筛选按钮：点击显示筛选功能提示 -->
        <button class="filter-btn" @click="handleFilter">筛选</button>
      </div>
    </div>
  </transition>
</template>

<script setup>
/**
 * Sou_Suo_Mian_Ban.vue - 搜索面板组件
 * 
 * 该组件提供一个可展开/收起的搜索界面，包含：
 * - 搜索输入框：支持回车搜索
 * - 筛选按钮：提供更多搜索选项（待实现）
 * 
 * 交互特性：
 * - 展开时自动聚焦到输入框
 * - 收起时自动清空输入内容
 * - 平滑的滑入/滑出动画效果
 * - 点击外部区域自动收起
 */
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 定义组件接收的Props
 * 
 * @property {Boolean} isExpanded - 搜索面板是否展开
 *                                  true: 展开显示，false: 收起隐藏
 */
const props = defineProps({
  isExpanded: {
    type: Boolean,
    default: false
  }
})

/**
 * 定义组件事件
 * 
 * @event search - 用户执行搜索时触发
 *                 传递搜索关键词给父组件处理
 * @event close - 用户点击外部区域时触发
 *                通知父组件关闭搜索面板
 */
const emit = defineEmits(['search', 'close'])

/**
 * 搜索输入框的 DOM 引用
 * 用于程序化控制焦点和清空操作
 */
const searchInputRef = ref(null)

/**
 * 搜索面板 DOM 引用
 * 用于判断点击是否在面板内部
 */
const searchPanelRef = ref(null)

/**
 * 处理点击外部区域事件
 * 当点击发生在搜索面板外部时，触发收起
 * 
 * @param {MouseEvent} event - 点击事件对象
 */
const handleClickOutside = (event) => {
  // 如果面板未展开，不处理
  if (!props.isExpanded) {
    return
  }
  
  // 检查点击目标是否在搜索面板内
  const target = event.target
  const panel = searchPanelRef.value
  
  // 如果面板存在且点击发生在面板内部，不处理
  if (panel && (panel === target || panel.contains(target))) {
    return
  }
  
  // 点击发生在外部，通知父组件收起面板
  emit('close')
}

/**
 * 监听 isExpanded 属性变化
 * 
 * 功能：
 * - 展开时：自动聚焦到输入框，提升用户体验
 * - 收起时：清空输入框内容，下次展开时为空白状态
 * 
 * @param {Boolean} newVal - isExpanded 的新值
 */
watch(() => props.isExpanded, (newVal) => {
  if (newVal) {
    // 面板展开：等待 DOM 更新后聚焦输入框
    nextTick(() => {
      if (searchInputRef.value) {
        searchInputRef.value.focus()
      }
    })
  } else {
    // 面板收起：清空输入框内容
    if (searchInputRef.value) {
      searchInputRef.value.value = ''
    }
  }
})

/**
 * 处理搜索事件
 * 当用户在输入框中按下回车键时触发
 * 
 * 逻辑：
 * - 检查输入框是否有值且非纯空格
 * - 通过emit触发search事件，将关键词传递给父组件
 */
const handleSearch = () => {
  if (searchInputRef.value && searchInputRef.value.value.trim()) {
    emit('search', searchInputRef.value.value)
  }
}

/**
 * 处理筛选按钮点击事件
 * 当前显示提示信息，待后续实现高级筛选功能
 */
const handleFilter = () => {
  ElMessage.info('筛选功能开发中，请先使用搜索关键词查找商品')
}

/**
 * 组件挂载时：添加全局点击事件监听器（使用捕获阶段）
 */
onMounted(() => {
  document.addEventListener('click', handleClickOutside, true)
})

/**
 * 组件卸载时：移除全局点击事件监听器，防止内存泄漏
 */
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside, true)
})
</script>

<style scoped>
/**
 * 搜索面板容器样式
 * - position: fixed，固定在页面顶部导航栏下方
 * - top: 80px，紧贴导航栏底部（导航栏高度80px）
 * - height: 25vh，占据视口高度的25%
 * - z-index: 999，低于导航栏(1000)但高于其他内容
 * - box-shadow添加阴影，增强层次感
 * - 背景色与顶部导航栏一致(#1f1f1fff)
 */
.search-expand {
  position: fixed;
  top: 120px;
  left: 0;
  right: 0;
  height: calc(25vh);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 40px;
  background-color: #FFFFFF;
  z-index: 999;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

/**
 * 搜索内容容器样式
 * - 最大宽度900px，避免在大屏幕上过宽
 * - flex布局，输入框和按钮水平排列
 * - gap: 16px，元素间距
 */
.search-container {
  width: 100%;
  max-width: 900px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

/**
 * 搜索输入框样式
 * - 固定宽度 600px，高度 44px
 * - 圆角 8px，符合现代 UI 设计
 * - 深色主题：背景#444444，文字#F2F2F2
 * - 使用苹方字体，中文显示更清晰
 * - transition 实现聚焦时的平滑过渡
 */
.search-input {
  width: 600px;
  height: 44px;
  padding: 0 16px;
  border: 1px solid #00B1FE;
  border-radius: 8px;
  outline: none;
  font-size: 15px;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  color: #111827;
  background-color: #FFFFFF;
  transition: all 0.2s ease;
}

/**
 * 筛选按钮样式
 * - 高度与输入框一致 (44px)
 * - 无背景、无边框，纯文字按钮
 * - 颜色#333333，深灰色，在白色背景上可见
 * - 使用苹方字体
 */
.filter-btn {
  height: 44px;
  padding: 0 24px;
  background: none;
  border: none;
  font-size: 16px;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  color: #333333;
  cursor: pointer;
  transition: color 0.2s ease;
}

/**
 * 筛选按钮悬停状态
 * 颜色变为#00B1FE，与收藏按钮风格一致
 */
.filter-btn:hover {
  color: #00B1FE;
}

/**
 * 输入框聚焦状态
 * - 边框颜色变亮(#666666)
 * - 背景色略微变亮(#4a4a4a)
 */
.search-input:focus {
  border-color: #00B1FE;
  box-shadow: 0 0 0 2px rgba(0, 177, 254, 0.2);
}

/**
 * 输入框占位符样式
 * 颜色#888888，比正常文字更淡
 */
.search-input::placeholder {
  color: #888888;
}

/**
 * Transition动画配置
 * search-slide-enter-active: 进入过程中的过渡效果
 * search-slide-leave-active: 离开过程中的过渡效果
 * - 使用transform替代max-height，避免浏览器重排导致卡顿
 */
.search-slide-enter-active,
.search-slide-leave-active {
  transition: transform 0.3s ease-out;
  transform-origin: top center;
}

/**
 * 进入起始状态
 * - transform: scaleY(0)，初始状态高度为0
 */
.search-slide-enter-from {
  transform: scaleY(0);
}

/**
 * 进入结束状态
 * - transform: scaleY(1)，完全展开
 */
.search-slide-enter-to {
  transform: scaleY(1);
}

/**
 * 离开结束状态
 * - transform: scaleY(0)，收起为0高度
 */
.search-slide-leave-to {
  transform: scaleY(0);
}
</style>
