<template>
  <div class="settlement-panel">
    <el-form label-width="140px" class="form-container">
      <!-- 结算比例 -->
      <el-form-item label="结算比例">
        <div class="ratio-container">
          <div class="ratio-option" :class="{ active: settlementData.settlementRatio === 'quick' }">
            <el-radio v-model="settlementData.settlementRatio" value="quick" border />
            <div class="ratio-info">
              <h4 class="ratio-label">快速比例</h4>
              <div class="ratio-content">
                <span class="ratio-text">1元 = </span>
                <el-input
                  v-model="settlementData.quickRatioValue"
                  placeholder="输入比例"
                  type="number"
                  class="ratio-input"
                  @input="calculateRent"
                />
                <span class="ratio-text">万哈夫币/资产</span>
              </div>
              <p class="ratio-tip">温馨提示：更多曝光，能更快吸引买家，加速成交（一般当天成交）</p>
            </div>
          </div>

          <div
            class="ratio-option"
            :class="{ active: settlementData.settlementRatio === 'standard' }"
          >
            <el-radio v-model="settlementData.settlementRatio" value="standard" border />
            <div class="ratio-info">
              <h4 class="ratio-label">标准比例</h4>
              <div class="ratio-content">
                <span class="ratio-text">1元 = </span>
                <el-input :value="standardRatioValue" readonly class="ratio-input" />
                <span class="ratio-text">万哈夫币/资产</span>
              </div>
              <p class="ratio-tip">温馨提示：系统智能估价，流程简单直接（一般2-4天有人下单）</p>
            </div>
          </div>
        </div>
        <p class="form-tip">
          请先填写"哈夫币纯币"、"安全箱"和"训练中心等级（体力）"，展示推荐比例。该结算比例为平台最终与号主结算比例，平台在实际运营中将根据实际成本和持续运营需要提升一定比例作为销售比例。该比例为平台计算仅供参考。
        </p>
      </el-form-item>

      <!-- 预收租金 -->
      <el-form-item label="预收租金">
        <el-input v-model="settlementData.preRent" placeholder="自动获取" type="number" />
        <span class="form-tip">元</span>
      </el-form-item>

      <!-- 推荐租期 -->
      <el-form-item label="推荐租期">
        <el-input :value="recommendedRentDays" placeholder="自动获取" readonly />
        <span class="form-tip">天</span>
        <span class="form-tip"
          >温馨提示：平台保底结算周期，大部分情况下租客打完后会发起结算，平台计算仅供参考</span
        >
      </el-form-item>

      <!-- 押金 -->
      <el-form-item label="押金">
        <el-input v-model="settlementData.deposit" placeholder="自动获取" type="number" />
        <span class="form-tip">元</span>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
/**
 * Jie_Suan_Xin_Xi_Mian_Ban.vue - 结算信息面板组件
 *
 * 该组件用于处理商品上架时的结算相关计算，包含：
 * - 结算比例：快速比例（卖家自定义）和标准比例（系统自动计算）
 * - 预收租金：根据纯币数量和结算比例自动计算
 * - 推荐租期：根据纯币数量自动推荐（7天或14天）
 * - 押金：根据标准比例自动计算
 *
 * 计算逻辑：
 * - 快速比例：卖家手动输入 1元 = X万哈夫币，预收租金 = 纯币总量 / 快速比例
 * - 标准比例：系统根据训练中心等级和安全箱自动计算（35或38），预收租金 = 纯币总量 / 标准比例
 * - 推荐租期：纯币 < 1亿 推荐7天，>= 1亿 推荐14天
 * - 押金：标准比例 * 10
 */
import { reactive, computed, watch } from 'vue'

/**
 * 定义组件接收的Props
 *
 * @property {String} pureCoinM - 哈夫克纯币数量（单位：M）
 * @property {String} trainingLevel - 训练中心等级（4-7）
 * @property {String} safeBox - 安全箱类型
 */
const props = defineProps({
  pureCoinM: {
    type: String,
    default: '',
  },
  trainingLevel: {
    type: String,
    default: '',
  },
  safeBox: {
    type: String,
    default: '',
  },
  // 草稿恢复用：上次保存的结算信息
  initialSettlement: {
    type: Object,
    default: () => ({}),
  },
})

/**
 * 结算数据对象
 * 包含结算比例、预收租金、押金等数据
 */
const settlementData = reactive({
  settlementRatio: 'quick', // 结算比例类型（quick=快速, standard=标准）
  quickRatioValue: '', // 快速比例值
  preRent: '', // 预收租金
  deposit: '', // 押金
})

// 草稿恢复：只覆盖确实存在的字段
Object.keys(props.initialSettlement || {}).forEach((key) => {
  if (settlementData[key] !== undefined && props.initialSettlement[key] != null) {
    settlementData[key] = props.initialSettlement[key]
  }
})

/**
 * 定义组件事件
 *
 * @event settlement-change - 结算数据变化时触发
 *                            传递预收租金、哈夫币比例、结算类型给父组件
 */
const emit = defineEmits(['settlement-change'])

/**
 * 标准比例计算属性
 * 根据训练中心等级和安全箱类型自动计算
 * - 训练中心7级 + 顶级安全箱 = 38
 * - 其他情况 = 35
 */
const standardRatioValue = computed(() => {
  if (props.trainingLevel === '7' && props.safeBox === '顶级安全箱3*3') {
    return 38
  }
  return 35
})

/**
 * 推荐租期计算属性
 * 根据纯币数量自动推荐租期
 * - 纯币 < 1亿（100M）= 7天
 * - 纯币 >= 1亿 = 14天
 * - 纯币为0 = 无法获取
 */
const recommendedRentDays = computed(() => {
  const pureCoinM = parseFloat(props.pureCoinM) || 0
  const pureCoin = pureCoinM * 100
  if (pureCoin === 0) {
    return '无法获取'
  }
  if (pureCoin < 100000000) {
    return 7
  }
  return 14
})

/**
 * 计算租金和押金
 * 根据结算比例类型和纯币数量计算预收租金
 * - 快速比例：使用卖家输入的比例值
 * - 标准比例：使用系统计算的标准比例值
 * - 押金始终使用标准比例计算
 */
const calculateRent = () => {
  const pureCoinM = parseFloat(props.pureCoinM) || 0
  const pureCoin = pureCoinM * 100

  if (settlementData.settlementRatio === 'quick') {
    const quickRatio = parseFloat(settlementData.quickRatioValue) || 0
    if (quickRatio > 0 && pureCoin > 0) {
      settlementData.preRent = (pureCoin / quickRatio).toFixed(2)
    }
  } else if (settlementData.settlementRatio === 'standard') {
    if (pureCoin > 0) {
      settlementData.preRent = (pureCoin / standardRatioValue.value).toFixed(2)
    }
  }

  settlementData.deposit = (standardRatioValue.value * 10).toString()
}

/**
 * 监听父组件传入的表单数据变化
 * 当纯币、训练中心等级、安全箱变化时重新计算
 */
watch(
  () => [props.pureCoinM, props.trainingLevel, props.safeBox],
  () => {
    calculateRent()
  },
  { immediate: true },
)

/**
 * 监听结算比例类型变化
 * 切换快速/标准比例时重新计算
 */
watch(
  () => settlementData.settlementRatio,
  () => {
    calculateRent()
  },
)

/**
 * 监听结算数据变化
 * 当任何结算相关数据变化时，向父组件发送最新数据
 */
watch(
  () => [
    settlementData.settlementRatio,
    settlementData.quickRatioValue,
    settlementData.preRent,
    props.trainingLevel,
    props.safeBox,
  ],
  () => {
    emit('settlement-change', {
      preRent: settlementData.preRent,
      halfCoinRatio:
        settlementData.settlementRatio === 'quick'
          ? settlementData.quickRatioValue
          : standardRatioValue.value.toString(),
      settlementType: settlementData.settlementRatio,
    })
  },
  { immediate: true },
)
</script>

<style scoped>
.settlement-panel {
  padding: 20px 24px;
}

.form-container {
  padding: 0;
}

.el-form-item {
  margin-bottom: 24px;
}

.el-form-item__label {
  font-weight: 500;
  color: #333;
  font-size: 16px;
  padding-right: 12px;
}

.el-form-item__content {
  flex: 1;
  min-width: 0;
}

.form-tip {
  display: block;
  font-size: 14px;
  color: #999;
  margin-top: 10px;
  line-height: 1.6;
  padding-right: 20px;
}

.ratio-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ratio-option {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  border-radius: 8px;
  border: 2px solid #f0f0f0;
  transition: all 0.3s ease;
}

.ratio-option:hover {
  border-color: #e0e0e0;
}

.ratio-option.active {
  border-color: #00b1fe;
  background-color: #f8fcff;
}

.ratio-option .el-radio {
  margin-top: 4px;
  flex-shrink: 0;
}

.ratio-info {
  flex: 1;
}

.ratio-label {
  display: block;
  font-weight: 600;
  margin-bottom: 12px;
  font-size: 14px;
  color: #333;
}

.ratio-content {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.ratio-text {
  font-size: 14px;
  color: #666;
}

.ratio-input {
  width: 120px;
  flex-shrink: 0;
}

.ratio-tip {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 0;
  padding-right: 0;
  line-height: 1.5;
}

.el-select,
.el-input {
  width: 100%;
  max-width: 280px;
}

@media (max-width: 768px) {
  .settlement-panel {
    padding: 16px;
  }

  .el-form-item {
    margin-bottom: 20px;
  }

  .el-select,
  .el-input {
    max-width: 100%;
  }
}
</style>
