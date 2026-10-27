<template>
  <div class="product-attributes">
    <div class="attr-title">账号详情</div>
    <div class="attr-grid">
      <div v-for="(attr, index) in attributes" :key="index" class="attr-item">
        <span class="attr-label">{{ attr.label }}</span>
        <span class="attr-value">{{ attr.value }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  product: {
    type: Object,
    required: true,
    default: () => ({}),
  },
})

const attributes = computed(() => {
  const p = props.product
  const attrs = []

  // 基本信息
  if (p.accountType) attrs.push({ label: '账号类型', value: p.accountType })
  if (p.server) attrs.push({ label: '区服', value: p.server })
  if (p.settlementType) attrs.push({ label: '结算方式', value: p.settlementType })
  if (p.banRecord) attrs.push({ label: '封禁记录', value: p.banRecord })
  if (p.faceSelf) attrs.push({ label: '人脸是否本人', value: p.faceSelf })

  // 资产信息
  if (p.warehouseAssets) attrs.push({ label: '仓库总资产', value: p.warehouseAssets + 'M' })
  if (p.pureCoinM) attrs.push({ label: '哈夫克纯币', value: p.pureCoinM + 'M' })
  if (p.halfCoinRatio) attrs.push({ label: '哈夫币比值', value: '1:' + p.halfCoinRatio })
  if (p.trainingLevel) attrs.push({ label: '训练中心等级', value: p.trainingLevel + '级' })
  if (p.rangeLevel) attrs.push({ label: '靶场等级', value: p.rangeLevel + '级' })
  if (p.safeBox) attrs.push({ label: '安全箱', value: p.safeBox })

  // 装备信息
  if (p.rank) attrs.push({ label: '段位', value: p.rank })
  if (p.accountLevel) attrs.push({ label: '账号等级', value: p.accountLevel + '级' })
  if (p.awmBullets) attrs.push({ label: 'AWM子弹', value: p.awmBullets + '发' })
  if (p.sixHead) attrs.push({ label: '六头', value: p.sixHead + '个' })
  if (p.sixArmor) attrs.push({ label: '六甲', value: p.sixArmor + '个' })
  if (p.secretKD) attrs.push({ label: '绝密KD', value: p.secretKD })

  // 皮肤信息
  if (
    p.knifeSkins &&
    p.knifeSkins.length > 0 &&
    !(p.knifeSkins.length === 1 && p.knifeSkins[0] === '无')
  ) {
    attrs.push({ label: '近战刀皮', value: p.knifeSkins.join('、') })
  }
  if (
    p.operatorSkins &&
    p.operatorSkins.length > 0 &&
    !(p.operatorSkins.length === 1 && p.operatorSkins[0] === '无')
  ) {
    attrs.push({ label: '干员皮', value: p.operatorSkins.join('、') })
  }
  if (
    p.weaponSkins &&
    p.weaponSkins.length > 0 &&
    !(p.weaponSkins.length === 1 && p.weaponSkins[0] === '无')
  ) {
    attrs.push({ label: '武器皮', value: p.weaponSkins.join('、') })
  }

  // 其他信息
  if (p.ownerBanned) attrs.push({ label: '号主禁止项', value: p.ownerBanned })
  if (p.ownerGifted) attrs.push({ label: '号主赠送项', value: p.ownerGifted })
  if (p.remark) attrs.push({ label: '备注', value: p.remark })
  if (p.personalTag) attrs.push({ label: '个性标签', value: p.personalTag })

  // 交易时间
  if (p.tradeStartHour !== undefined && p.tradeEndHour !== undefined) {
    attrs.push({
      label: '方便交易时间',
      value: p.tradeStartHour + ':00 - ' + p.tradeEndHour + ':00',
    })
  }

  // 额外道具付费
  if (p.extraItemCharge) {
    attrs.push({ label: '额外道具付费', value: p.extraItemCharge })
    if (p.extraItemCharge === '需要计费') {
      if (p.awmBulletPrice) attrs.push({ label: 'AWM子弹单价', value: p.awmBulletPrice + '元/发' })
      if (p.sixHeadPrice) attrs.push({ label: '六头单价', value: p.sixHeadPrice + '元/个' })
      if (p.sixArmorPrice) attrs.push({ label: '六甲单价', value: p.sixArmorPrice + '元/个' })
    }
  }

  return attrs
})
</script>

<style scoped>
.product-attributes {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px 24px;
  box-sizing: border-box;
}

.attr-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f2f3f5;
}

.attr-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px 20px;
}

.attr-item {
  display: grid;
  grid-template-columns: 100px 1fr;
  font-size: 13px;
  padding: 10px 12px;
  background: #f7f8fa;
  border-radius: 6px;
  transition: background 0.2s;
  min-width: 0;
}

.attr-item:hover {
  background: #f2f3f5;
}

.attr-label {
  color: #86909c;
}

.attr-value {
  color: #1d2129;
  font-weight: 500;
  word-break: break-all;
  text-align: left;
  min-width: 0;
}
</style>
