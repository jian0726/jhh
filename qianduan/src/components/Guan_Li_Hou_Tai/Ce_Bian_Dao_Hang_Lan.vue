<template>
  <aside class="sidebar">
    <el-menu
      :default-active="defaultActive"
      class="sidebar-menu"
      mode="vertical"
      @select="handleMenuSelect"
    >
      <el-menu-item index="dashboard">
        <span>数据看板</span>
      </el-menu-item>

      <el-menu-item index="users">
        <span>用户管理</span>
      </el-menu-item>

      <el-menu-item index="products">
        <span>商品管理</span>
      </el-menu-item>

      <el-menu-item index="orders">
        <span>订单管理</span>
      </el-menu-item>

      <el-menu-item index="disputes">
        <span>纠纷仲裁</span>
      </el-menu-item>

      <el-menu-item index="risk-logs">
        <span>风控日志</span>
      </el-menu-item>

      <el-menu-item index="operate-logs">
        <span>操作日志</span>
      </el-menu-item>

      <el-menu-item index="notice">
        <span>通知发布</span>
      </el-menu-item>

      <el-menu-item index="lunbotu">
        <span>轮播图管理</span>
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<script setup>
defineProps({
  defaultActive: {
    type: String,
    default: 'dashboard',
  },
})

const emit = defineEmits(['menu-change'])

const handleMenuSelect = (index) => {
  emit('menu-change', index)
}
</script>

<style scoped>
/* 与个人中心侧边栏（components/Ge_Ren_Zhong_Xin/Ce_Bian_Dao_Hang_Lan.vue）保持同一套样式 */
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

</style>
