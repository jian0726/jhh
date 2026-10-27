<template>
  <aside class="sidebar">
    <el-menu
      :default-active="defaultActive"
      class="sidebar-menu"
      mode="vertical"
      @select="handleMenuSelect"
    >
      <!-- 个人中心 -->
      <el-menu-item index="personal">
        <span>个人中心</span>
      </el-menu-item>

      <!-- 我的商品 -->
      <el-menu-item index="my-products">
        <span>我的商品</span>
      </el-menu-item>

      <!-- 收藏 -->
      <el-menu-item index="favorite">
        <span>收藏</span>
      </el-menu-item>

      <!-- 我的订单（带下拉） -->
      <el-sub-menu index="order">
        <template #title>
          <span>我的订单</span>
        </template>
        <el-menu-item index="order-buyer">我是买家</el-menu-item>
        <el-menu-item index="order-seller">我是卖家</el-menu-item>
      </el-sub-menu>

      <!-- 消息 -->
      <el-menu-item index="message">
        <span>消息</span>
      </el-menu-item>

      <!-- 联系客服 -->
      <el-menu-item index="service">
        <span>联系客服</span>
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<script setup>
defineProps({
  defaultActive: {
    type: String,
    default: 'personal',
  },
})

const emit = defineEmits(['menu-change'])

const handleMenuSelect = (index) => {
  emit('menu-change', index)
}
</script>

<style scoped>
.sidebar {
  width: 240px;
  min-height: calc(100vh - 120px);
  background-color: #ffffff;
  padding-top: 120px;
  position: fixed;
  left: 0;
  top: 0;
}

.sidebar-menu {
  width: 100%;
  border-right: none;
}

:deep(.el-menu-item) {
  font-size: 16px;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  color: #333333;
  height: 64px;
  line-height: 64px;
  padding: 4px 24px;
  margin: 4px 8px;
  border-radius: 8px;
}

:deep(.el-menu-item:hover) {
  background-color: #f5f7fa;
}

:deep(.el-menu-item.is-active) {
  background-color: #e6f7ff;
  color: #00b1fe;
}

:deep(.el-sub-menu__title) {
  font-size: 16px;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  color: #333333;
  height: 64px;
  line-height: 64px;
  padding: 4px 24px;
  margin: 4px 8px;
  border-radius: 8px;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #f5f7fa;
}

:deep(.el-sub-menu.is-active .el-sub-menu__title) {
  background-color: #e6f7ff;
  color: #00b1fe;
}

:deep(.el-menu--vertical .el-sub-menu .el-menu-item) {
  padding-left: 48px;
  font-size: 14px;
  color: #333333;
}

:deep(.el-menu--vertical .el-sub-menu .el-menu-item.is-active) {
  color: #00b1fe;
  background-color: #f0f5ff;
}

:deep(.el-menu--vertical .el-sub-menu .el-menu-item:hover) {
  background-color: #f0f5ff;
}

</style>
