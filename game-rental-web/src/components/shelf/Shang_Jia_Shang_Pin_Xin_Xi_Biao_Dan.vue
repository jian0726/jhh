<template>
  <div class="detail-form">
    <el-form :model="formData" label-width="180px" class="form-container">
      <!-- 账号类型 -->
      <el-form-item label="* 账号类型">
        <el-radio-group v-model="formData.accountType">
          <el-radio value="纯币号">纯币号</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 区服 -->
      <el-form-item label="* 区服">
        <el-radio-group v-model="formData.server">
          <el-radio value="QQ">QQ</el-radio>
          <el-radio value="微信">微信</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 结算方式 -->
      <el-form-item label="* 结算方式">
        <el-radio-group v-model="formData.settlementType">
          <el-radio value="哈夫币必须打完">哈夫币必须打完</el-radio>
          <el-radio value="协商保底消耗">协商保底消耗</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 封禁记录 -->
      <el-form-item label="* 封禁记录">
        <el-radio-group v-model="formData.banRecord">
          <el-radio value="无封禁记录">无封禁记录</el-radio>
          <el-radio value="有封禁记录">有封禁记录</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 人脸是否本人 -->
      <el-form-item label="* 人脸是否本人">
        <el-radio-group v-model="formData.faceSelf">
          <el-radio value="是">是</el-radio>
          <el-radio value="否">否</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 仓库总资产 -->
      <el-form-item label="仓库总资产">
        <el-input
          v-model="formData.warehouseAssets"
          placeholder="默认 ≥10M，仅限数字"
          type="number"
        />
      </el-form-item>

      <!-- 哈夫克纯币 -->
      <el-form-item label="* 哈夫克纯币 (M)">
        <el-input
          v-model="formData.pureCoinM"
          placeholder="默认 ≥10M"
          type="number"
          @input="handleFormChange"
        />
        <span class="form-tip">温馨提示：资产与纯币差额过大寄售可能会有比较大风险，建议优先清理仓库再登记寄售</span>
      </el-form-item>

      <!-- 训练中心等级 -->
      <el-form-item label="* 训练中心等级 (体力)">
        <el-select v-model="formData.trainingLevel" placeholder="请选择训练中心等级" @change="handleFormChange">
          <el-option value="4" label="4级" />
          <el-option value="5" label="5级" />
          <el-option value="6" label="6级" />
          <el-option value="7" label="7级" />
        </el-select>
      </el-form-item>

      <!-- 靶场等级 -->
      <el-form-item label="* 靶场等级 (负重)">
        <el-select v-model="formData.rangeLevel" placeholder="请选择靶场等级">
          <el-option value="4" label="4级" />
          <el-option value="5" label="5级" />
          <el-option value="6" label="6级" />
          <el-option value="7" label="7级" />
        </el-select>
      </el-form-item>

      <!-- 安全箱 -->
      <el-form-item label="安全箱">
        <el-radio-group v-model="formData.safeBox" direction="vertical" @change="handleFormChange">
          <el-radio value="顶级安全箱3*3">顶级安全箱3*3</el-radio>
          <el-radio value="高级安全箱2*3">高级安全箱2*3</el-radio>
          <el-radio value="进阶安全箱2.2">进阶安全箱2.2</el-radio>
          <el-radio value="基础安全箱1*2">基础安全箱1*2</el-radio>
        </el-radio-group>
        <span class="form-tip">温馨提示：如实按照赛季安全箱填写，否则影响结算价格，建议至少有6格保险。9格体验卡，请在号主禁止项或允许项目一栏写明体验卡天数，否则会影响结算价格。</span>
      </el-form-item>

      <!-- 段位 -->
      <el-form-item label="段位">
        <el-select v-model="formData.rank" placeholder="请选择段位">
          <el-option value="青铜" label="青铜" />
          <el-option value="白银" label="白银" />
          <el-option value="黄金" label="黄金" />
          <el-option value="铂金" label="铂金" />
          <el-option value="钻石" label="钻石" />
          <el-option value="黑鹰" label="黑鹰" />
          <el-option value="三角洲巅峰1-100星" label="三角洲巅峰1-100星" />
          <el-option value="三角洲巅峰100星以上" label="三角洲巅峰100星以上" />
        </el-select>
      </el-form-item>

      <!-- 账号等级 -->
      <el-form-item label="账号等级">
        <el-input
          v-model="formData.accountLevel"
          placeholder="请输入 30-60 整数数值"
          type="number"
          :min="30"
          :max="60"
        />
        <span class="form-tip">温馨提示：账号等级低于 30 无法寄售租赁</span>
      </el-form-item>

      <!-- AWM子弹 -->
      <el-form-item label="AWM 子弹">
        <el-input
          v-model="formData.awmBullets"
          placeholder="请输入数量（发）"
          type="number"
        />
      </el-form-item>

      <!-- 六头 -->
      <el-form-item label="六头">
        <el-input
          v-model="formData.sixHead"
          placeholder="请输入数量（个）"
          type="number"
        />
      </el-form-item>

      <!-- 六甲 -->
      <el-form-item label="六甲">
        <el-input
          v-model="formData.sixArmor"
          placeholder="请输入数量（个）"
          type="number"
        />
      </el-form-item>

      <!-- 额外道具付费设置 -->
      <el-form-item label="额外道具付费设置">
        <el-radio-group v-model="formData.extraItemCharge">
          <el-radio value="需要计费">需要计费</el-radio>
          <el-radio value="无需计费">无需计费</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- AWM子弹单价 -->
      <el-form-item v-if="formData.extraItemCharge === '需要计费'" label="AWM 子弹单价">
        <el-input
          v-model="formData.awmBulletPrice"
          placeholder="默认 0 元/发"
          type="number"
        />
      </el-form-item>

      <!-- 六头单价 -->
      <el-form-item v-if="formData.extraItemCharge === '需要计费'" label="六头单价">
        <el-input
          v-model="formData.sixHeadPrice"
          placeholder="默认 0 元/个"
          type="number"
        />
      </el-form-item>

      <!-- 六甲单价 -->
      <el-form-item v-if="formData.extraItemCharge === '需要计费'" label="六甲单价">
        <el-input
          v-model="formData.sixArmorPrice"
          placeholder="默认 0 元/个"
          type="number"
        />
        <span class="form-tip">温馨提示：此道具计费不计入"预收租金"</span>
      </el-form-item>

      <!-- 绝密KD -->
      <el-form-item label="绝密 KD">
        <el-input
          v-model="formData.secretKD"
          placeholder="请输入绝密KD"
          type="number"
          step="0.1"
        />
      </el-form-item>

      <!-- 刀皮 -->
      <el-form-item label="刀皮">
        <el-select
          v-model="formData.knifeSkins"
          multiple
          placeholder="请选择刀皮"
          placement="bottom-start"
          :popper-options="{ modifiers: [{ name: 'flip', enabled: false }] }"
        >
          <el-option value="无" label="无" />
          <el-option value="战术匕首" label="战术匕首" />
          <el-option value="北极星" label="北极星" />
          <el-option value="暗星" label="暗星" />
          <el-option value="坠星者" label="坠星者" />
          <el-option value="信条" label="信条" />
          <el-option value="怜悯" label="怜悯" />
          <el-option value="黑海" label="黑海" />
          <el-option value="处刑者" label="处刑者" />
          <el-option value="龙牙" label="龙牙" />
          <el-option value="影锋" label="影锋" />
          <el-option value="赤枭" label="赤枭" />
          <el-option value="电锯" label="电锯" />
        </el-select>
        <span class="form-tip">温馨提示：持有刀皮可以加快被选的速度哦</span>
      </el-form-item>

      <!-- 干员皮 -->
      <el-form-item label="干员皮">
        <el-select
          v-model="formData.operatorSkins"
          multiple
          placeholder="请选择干员皮"
          placement="bottom-start"
          :popper-options="{ modifiers: [{ name: 'flip', enabled: false }] }"
        >
          <el-option value="无" label="无" />
          <el-option value="红狼-蚀金玫瑰" label="红狼-蚀金玫瑰" />
          <el-option value="威龙-蛟龙特战队" label="威龙-蛟龙特战队" />
          <el-option value="露娜-黑天际线" label="露娜-黑天际线" />
          <el-option value="威龙-凌霄戍卫" label="威龙-凌霄戍卫" />
          <el-option value="骇爪-维什戴尔" label="骇爪-维什戴尔" />
          <el-option value="威龙-飞虎" label="威龙-飞虎" />
          <el-option value="骇爪-水墨云图" label="骇爪-水墨云图" />
          <el-option value="蛊-能天使午夜邮差" label="蛊-能天使午夜邮差" />
          <el-option value="威龙-壮志凌云" label="威龙-壮志凌云" />
          <el-option value="露娜-劳拉·克劳馥" label="露娜-劳拉·克劳馥" />
          <el-option value="露娜-金牌射手" label="露娜-金牌射手" />
          <el-option value="威龙-铁面判官" label="威龙-铁面判官" />
          <el-option value="蜂医-送葬人无题密令" label="蜂医-送葬人无题密令" />
          <el-option value="红狼-电锯惊魂" label="红狼-电锯惊魂" />
          <el-option value="蛊-不羁人生" label="蛊-不羁人生" />
          <el-option value="红狼-黑鹰坠落" label="红狼-黑鹰坠落" />
          <el-option value="无名-夜鹰" label="无名-夜鹰" />
          <el-option value="深蓝-不破誓约" label="深蓝-不破誓约" />
          <el-option value="乌鲁鲁-荒原猎手" label="乌鲁鲁-荒原猎手" />
          <el-option value="牧羊人-黑鹰坠落" label="牧羊人-黑鹰坠落" />
          <el-option value="乌鲁鲁-黑鹰坠落" label="乌鲁鲁-黑鹰坠落" />
          <el-option value="蜂医-危险物质" label="蜂医-危险物质" />
          <el-option value="牧羊人-街头之王" label="牧羊人-街头之王" />
          <el-option value="蜂医-黑鹰坠落" label="蜂医-黑鹰坠落" />
        </el-select>
      </el-form-item>

      <!-- 武器皮 -->
      <el-form-item label="武器皮">
        <el-select
          v-model="formData.weaponSkins"
          multiple
          placeholder="请选择武器皮"
          placement="bottom-start"
          :popper-options="{ modifiers: [{ name: 'flip', enabled: false }] }"
        >
          <el-option value="无" label="无" />
          <el-option value="KC17造物纪元" label="KC17造物纪元" />
          <el-option value="QBZ95王牌之剑" label="QBZ95王牌之剑" />
          <el-option value="M250电玩S2" label="M250电玩S2" />
          <el-option value="MP7电玩S2" label="MP7电玩S2" />
          <el-option value="ASVal悬赏令" label="ASVal悬赏令" />
          <el-option value="腾龙气象感应" label="腾龙气象感应" />
          <el-option value="SCAR电玩高手" label="SCAR电玩高手" />
          <el-option value="Vector美杜莎" label="Vector美杜莎" />
          <el-option value="M4A1棱镜攻势" label="M4A1棱镜攻势" />
          <el-option value="AUG气象感应" label="AUG气象感应" />
          <el-option value="M7棱镜S2" label="M7棱镜S2" />
          <el-option value="K416命运" label="K416命运" />
        </el-select>
      </el-form-item>

      <!-- 号主禁止项 -->
      <el-form-item label="号主禁止项">
        <el-input
          v-model="formData.ownerBanned"
          placeholder="请输入号主禁止项（100字以内）"
          maxlength="100"
        />
      </el-form-item>

      <!-- 号主赠送项 -->
      <el-form-item label="号主赠送项">
        <el-input
          v-model="formData.ownerGifted"
          placeholder="请输入号主赠送项（100字以内）"
          maxlength="100"
        />
      </el-form-item>

      <!-- 备注 -->
      <el-form-item label="备注">
        <el-input
          v-model="formData.remark"
          placeholder="请输入备注（100字以内）"
          maxlength="100"
        />
      </el-form-item>

      <!-- 个性标签 -->
      <el-form-item label="个性标签">
        <el-input
          v-model="formData.personalTag"
          placeholder="请输入您的个性标题（50字以内）"
          maxlength="50"
        />
      </el-form-item>

      <!-- 方便交易时间 -->
      <el-form-item label="方便交易时间">
        <el-select v-model="formData.tradeStartHour" placeholder="开始">
          <el-option v-for="hour in hours" :key="hour" :value="hour" :label="hour + ':00'" />
        </el-select>
        <span class="time-separator">-</span>
        <el-select v-model="formData.tradeEndHour" placeholder="结束">
          <el-option v-for="hour in hours" :key="hour" :value="hour" :label="hour + ':00'" />
        </el-select>
        <span class="form-tip">温馨提示：请慎重并如实填写您的可协助时段，该时段会影响租客对您账号偏好；协助时段内无法联系到您会导致账号下架</span>
      </el-form-item>

      <!-- 打完改密 -->
      <el-form-item label="打完改密">
        <el-radio-group v-model="formData.selfCheck">
          <el-radio value="已知晓">已知晓</el-radio>
        </el-radio-group>
        <span class="form-tip">温馨提示：号主请在订单结算后的24小时内，拒绝给租号扫码并及时修改密码，同时自检自己的游戏账号是否缺失选定保留物品</span>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
/**
 * Shang_Jia_Shang_Pin_Xin_Xi_Biao_Dan.vue - 上架商品信息表单组件
 *
 * 该组件用于卖家填写游戏账号的详细信息，包含：
 * - 账号基本信息：类型、区服、结算方式、封禁记录、人脸验证
 * - 资产信息：仓库总资产、哈夫克纯币、训练中心等级、靶场等级、安全箱
 * - 装备信息：段位、账号等级、AWM子弹、六头、六甲、绝密KD
 * - 皮肤信息：刀皮、干员皮、武器皮（支持多选）
 * - 交易设置：额外道具付费、方便交易时间、打完改密确认
 *
 * 交互功能：
 * - 表单数据变化时自动触发 form-change 事件
 * - 支持双向数据绑定
 * - 提供表单验证和提示信息
 */
import { reactive, watch } from 'vue'

/**
 * 表单数据对象
 * 包含所有上架商品需要填写的字段
 */
const formData = reactive({
  // 账号基本信息
  accountType: '纯币号',              // 账号类型
  server: '',                         // 区服（QQ/微信）
  settlementType: '',                 // 结算方式
  banRecord: '',                      // 封禁记录
  faceSelf: '',                       // 人脸是否本人

  // 资产信息
  warehouseAssets: '',                // 仓库总资产
  pureCoinM: '',                      // 哈夫克纯币(M)
  trainingLevel: '',                  // 训练中心等级
  rangeLevel: '',                     // 靶场等级
  safeBox: '',                        // 安全箱

  // 装备信息
  rank: '',                           // 段位
  accountLevel: '',                   // 账号等级
  awmBullets: '',                     // AWM子弹
  sixHead: '',                        // 六头
  sixArmor: '',                       // 六甲
  secretKD: '',                       // 绝密KD

  // 皮肤信息（支持多选，使用数组）
  knifeSkins: [],                     // 刀皮
  operatorSkins: [],                  // 干员皮
  weaponSkins: [],                    // 武器皮

  // 其他信息
  ownerBanned: '',                    // 号主禁止项
  ownerGifted: '',                    // 号主赠送项
  remark: '',                         // 备注
  personalTag: '',                    // 个性标签

  // 交易设置
  tradeStartHour: '',                 // 方便交易时间（开始）
  tradeEndHour: '',                   // 方便交易时间（结束）
  selfCheck: '',                      // 打完改密确认

  // 额外道具付费设置
  extraItemCharge: '',                // 是否需要计费
  awmBulletPrice: 0,                  // AWM子弹单价
  sixHeadPrice: 0,                    // 六头单价
  sixArmorPrice: 0                    // 六甲单价
})

/**
 * 小时选项数组（00-23）
 * 用于方便交易时间的选择
 */
const hours = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0'))

/**
 * 定义组件事件
 *
 * @event form-change - 表单数据变化时触发
 *                      传递完整的表单数据给父组件
 */
const emit = defineEmits(['form-change'])

/**
 * 处理表单变化事件
 * 当表单数据发生变化时，向父组件发送最新数据
 */
const handleFormChange = () => {
  emit('form-change', { ...formData })
}

/**
 * 监听表单数据变化
 * 使用 deep: true 深度监听，确保嵌套对象的变化也能被捕获
 * immediate: true 确保组件挂载时立即触发一次
 */
watch(formData, () => {
  emit('form-change', { ...formData })
}, { deep: true, immediate: true })
</script>

<style scoped>
.detail-form {
  padding: 20px 24px;
}

.form-container {
  padding: 0;
}

.el-form-item {
  margin-bottom: 24px;
  align-items: flex-start;
}

.el-form-item__label {
  font-weight: 500;
  color: #333;
  font-size: 16px;
  padding-right: 12px;
  text-align: left;
}

.el-form-item__content {
  flex: 1;
  min-width: 0;
  max-width: 100%;
}

/* Element Plus 表单样式微调 — 用更高优先级的选择器而非 !important */
.detail-form :deep(.el-form-item) {
  align-items: flex-start;
}

.detail-form :deep(.el-form-item__label) {
  text-align: left;
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  white-space: normal;
  word-break: break-word;
}

.detail-form :deep(.el-form-item__content) {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.form-tip {
  display: block;
  font-size: 14px;
  color: #999;
  margin-top: 10px;
  line-height: 1.6;
  padding-right: 20px;
}

.time-separator {
  margin: 0 12px;
  color: #999;
  font-size: 14px;
}

.el-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.el-radio-group.is-vertical {
  flex-direction: column;
  gap: 12px;
}

.el-radio {
  display: flex;
  align-items: center;
}

.el-select,
.el-input {
  width: 100%;
  max-width: 280px;
}

.el-radio+.el-radio {
  margin-left: 0;
}

@media (max-width: 768px) {
  .detail-form {
    padding: 16px;
  }

  .el-form-item {
    margin-bottom: 20px;
  }

  .el-select,
  .el-input {
    max-width: 100%;
  }
  .form-tip {
    margin-left: 0;
  }
}
</style>
