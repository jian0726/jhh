<template>
  <div class="info-section">
    <h2 class="section-title">商品信息</h2>
    <div class="info-grid">
      <div class="info-item">
        <label>账号类型</label>
        <span>{{ accountType || '-' }}</span>
      </div>
      <div class="info-item">
        <label>区服</label>
        <span>{{ server || '-' }}</span>
      </div>
      <div class="info-item">
        <label>结算方式</label>
        <span>{{ settlementType || '-' }}</span>
      </div>
      <div class="info-item">
        <label>封禁记录</label>
        <span>{{ banRecord || '-' }}</span>
      </div>
      <div class="info-item">
        <label>人脸是否本人</label>
        <span>{{ faceSelf || '-' }}</span>
      </div>
      <div class="info-item">
        <label>仓库总资产</label>
        <span>{{ warehouseAssets || '-' }} M</span>
      </div>
      <div class="info-item">
        <label>哈夫克纯币</label>
        <span>{{ pureCoinM || '-' }} M</span>
      </div>
      <div class="info-item">
        <label>训练中心等级</label>
        <span>{{ trainingLevel || '-' }} 级</span>
      </div>
      <div class="info-item">
        <label>靶场等级</label>
        <span>{{ rangeLevel || '-' }} 级</span>
      </div>
      <div class="info-item">
        <label>安全箱</label>
        <span>{{ safeBox || '-' }}</span>
      </div>
      <div class="info-item">
        <label>段位</label>
        <span>{{ rank || '-' }}</span>
      </div>
      <div class="info-item">
        <label>账号等级</label>
        <span>{{ accountLevel || '-' }}</span>
      </div>
      <div class="info-item">
        <label>AWM子弹</label>
        <span>{{ awmBullets || '-' }} 发</span>
      </div>
      <div class="info-item">
        <label>六头</label>
        <span>{{ sixHead || '-' }} 个</span>
      </div>
      <div class="info-item">
        <label>六甲</label>
        <span>{{ sixArmor || '-' }} 个</span>
      </div>
      <div class="info-item">
        <label>绝密KD</label>
        <span>{{ secretKD || '-' }}</span>
      </div>
      <div class="info-item">
        <label>刀皮</label>
        <span>{{ formatArray(knifeSkins) }}</span>
      </div>
      <div class="info-item">
        <label>干员皮</label>
        <span>{{ formatArray(operatorSkins) }}</span>
      </div>
      <div class="info-item">
        <label>武器皮</label>
        <span>{{ formatArray(weaponSkins) }}</span>
      </div>
      <div class="info-item full-width">
        <label>号主禁止项</label>
        <span>{{ ownerBanned || '-' }}</span>
      </div>
      <div class="info-item full-width">
        <label>号主赠送项</label>
        <span>{{ ownerGifted || '-' }}</span>
      </div>
      <div class="info-item full-width">
        <label>备注</label>
        <span>{{ remark || '-' }}</span>
      </div>
      <div class="info-item">
        <label>方便交易时间</label>
        <span>{{ formatTimeRange(tradeStartHour, tradeEndHour) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  accountType: String,
  server: String,
  settlementType: String,
  banRecord: String,
  faceSelf: String,
  warehouseAssets: String,
  pureCoinM: String,
  trainingLevel: String,
  rangeLevel: String,
  safeBox: String,
  rank: String,
  accountLevel: String,
  awmBullets: String,
  sixHead: String,
  sixArmor: String,
  secretKD: String,
  knifeSkins: Array,
  operatorSkins: Array,
  weaponSkins: Array,
  ownerBanned: String,
  ownerGifted: String,
  remark: String,
  tradeStartHour: String,
  tradeEndHour: String
})

const formatArray = (arr) => {
  if (!arr || arr.length === 0) return '-'
  if (Array.isArray(arr)) return arr.join('、')
  return String(arr)
}

const formatTimeRange = (start, end) => {
  if (!start || !end) return '-'
  return `${start}:00 - ${end}:00`
}
</script>

<style scoped>
.info-section {
  background-color: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item.full-width {
  grid-column: span 2;
}

.info-item label {
  font-size: 14px;
  color: #999;
}

.info-item span {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}
</style>
