<template>
  <div class="rental-notice">
    <div class="notice-title">租用须知</div>
    <ul class="notice-list">
      <li v-for="(notice, index) in notices" :key="index" class="notice-item">
        <span class="notice-dot"></span>
        <span v-html="notice"></span>
      </li>
    </ul>
  </div>
</template>

<script setup>
const notices = [
  '账号仅限本人使用，<strong>禁止转租、转借、出售</strong>，违者扣除全部押金并永久封禁平台账号。',
  '严禁使用外挂、脚本、宏等任何违规工具，一经检测立即封号，<strong>押金不予退还</strong>并追究相关责任。',
  '租期结束后请正常退出账号，无违规情况下押金将在<strong>2小时内原路退还</strong>，遇节假日顺延。',
  '如遇账号无法登录、密码错误、被顶号等问题，请立即联系在线客服处理，支持全额退款。',
  '请勿私自转移、销毁账号内道具、货币及材料，<strong>违者将按市场价从押金中扣除</strong>。',
]
</script>

<style scoped>
.rental-notice {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px 24px;
  box-sizing: border-box;
}

.notice-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f2f3f5;
}

.notice-list {
  list-style: none;
}

.notice-item {
  display: flex;
  gap: 10px;
  padding: 8px 0;
  font-size: 13px;
  color: #4e5969;
  line-height: 1.7;
}

.notice-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #00b1fe;
  margin-top: 8px;
  flex-shrink: 0;
}

.notice-item :deep(strong) {
  color: #f53f3f;
  font-weight: 500;
}
</style>
