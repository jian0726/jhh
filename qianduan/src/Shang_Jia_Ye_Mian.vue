<template>
  <div class="shang-jia-ye-mian">
    <Ding_Bu_Dao_Hang_Lan :isSearchExpanded="isSearchExpanded" @toggleSearch="toggleSearch" />
    <Sou_Suo_Mian_Ban :isExpanded="isSearchExpanded" @search="handleSearch" />

    <main class="shang-jia-nei-rong" @click="closeSearch">
      <h1 class="ye-mian-biao-ti">上架商品</h1>

      <div v-if="draftHasContent" class="cao-gao-ti-shi">
        <span>已恢复上次填写的内容（{{ draftTimeText }}），图片需重新上传；点击下方「重置」可清空。</span>
      </div>

      <div class="zhu-yao-rong-qi">
        <div class="padding-wai-kuang">
          <div class="tong-yi-rong-qi">
            <div class="nei-rong-qu">
              <h2 class="qu-biao-ti">商品信息</h2>
              <Shang_Jia_Shang_Pin_Xin_Xi_Biao_Dan
                :key="'form-' + formKey"
                :initial-form="initialForm"
                @form-change="handleFormChange"
              />
            </div>

            <div class="nei-rong-qu">
              <h2 class="qu-biao-ti">结算信息</h2>
              <Jie_Suan_Xin_Xi_Mian_Ban
                :key="'settle-' + formKey"
                :initial-settlement="initialSettlement"
                :pure-coin-m="formData.pureCoinM"
                :training-level="formData.trainingLevel"
                :safe-box="formData.safeBox"
                @settlement-change="handleSettlementChange"
              />
            </div>

            <div class="nei-rong-qu">
              <h2 class="qu-biao-ti">商品图片</h2>
              <Shang_Pin_Tu_Pian_Shang_Chuan @image-change="handleImageChange" />
              <Xie_Yi_Tong_Yi_He_An_Niu
                ref="xieYiTongYiHeAnNiu"
                :disabled="!selectedImage || !agreed"
                :loading="submitting"
                @reset="handleReset"
                @submit="handleSubmit"
                @agreement-change="handleAgreementChange"
              />
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import Ding_Bu_Dao_Hang_Lan from './components/Shou_Ye/Ding_Bu_Dao_Hang_Lan.vue'
import Sou_Suo_Mian_Ban from './components/Shou_Ye/Sou_Suo_Mian_Ban.vue'
import Shang_Jia_Shang_Pin_Xin_Xi_Biao_Dan from './components/Shang_Jia_Ye_Mian/Shang_Jia_Shang_Pin_Xin_Xi_Biao_Dan.vue'
import Jie_Suan_Xin_Xi_Mian_Ban from './components/Shang_Jia_Ye_Mian/Jie_Suan_Xin_Xi_Mian_Ban.vue'
import Shang_Pin_Tu_Pian_Shang_Chuan from './components/Shang_Jia_Ye_Mian/Shang_Pin_Tu_Pian_Shang_Chuan.vue'
import Xie_Yi_Tong_Yi_He_An_Niu from './components/Shang_Jia_Ye_Mian/Xie_Yi_Tong_Yi_He_An_Niu.vue'
import { productApi } from './api/product'

// 状态
const isSearchExpanded = ref(false)
const selectedImage = ref(null)
const selectedFile = ref(null)
const agreed = ref(false)
const xieYiTongYiHeAnNiu = ref(null)
const submitting = ref(false)

/* ==================== 草稿保存 / 恢复 ==================== */
const DRAFT_KEY = 'delta_shangjia_draft_v1'

/** 读取草稿（同步执行，保证子组件首次渲染就能拿到初始值） */
const loadDraft = () => {
  try {
    const raw = localStorage.getItem(DRAFT_KEY)
    if (!raw) return null
    const draft = JSON.parse(raw)
    if (!draft || typeof draft !== 'object') return null
    return draft
  } catch {
    return null
  }
}

const draft = loadDraft()

const formData = ref(draft?.formData || {})
const settlementInfo = ref(draft?.settlementInfo || {})
// 传给子组件的初始值：仅在子组件挂载时读取，重置时换成空对象并靠 key 强制重建
const initialForm = ref(draft?.formData || {})
const initialSettlement = ref(draft?.settlementInfo || {})
const formKey = ref(0)
// 重置后短暂停写草稿，避免子组件重新挂载时把默认值又写成草稿
let draftBlockedUntil = 0

/** 草稿里是否有实际填写内容（避免空表单也提示“已恢复”） */
const draftHasContent = ref(
  Boolean(
    draft &&
      ((draft.formData && Object.values(draft.formData).some((v) => (Array.isArray(v) ? v.length : v))) ||
        (draft.settlementInfo && Object.values(draft.settlementInfo).some((v) => v))),
  ),
)

let draftTimer = null
/** 防抖保存草稿（800ms），图片是 base64 且体积大，不写入草稿 */
const saveDraft = () => {
  clearTimeout(draftTimer)
  draftTimer = setTimeout(() => {
    if (Date.now() < draftBlockedUntil) return
    try {
      if (!Object.keys(formData.value).length && !Object.keys(settlementInfo.value).length) return
      localStorage.setItem(
        DRAFT_KEY,
        JSON.stringify({
          formData: formData.value,
          settlementInfo: settlementInfo.value,
          savedAt: Date.now(),
        }),
      )
    } catch (err) {
      console.warn('草稿保存失败（可能超出 localStorage 容量）', err)
    }
  }, 800)
}

const clearDraft = () => {
  clearTimeout(draftTimer)
  localStorage.removeItem(DRAFT_KEY)
}

watch([formData, settlementInfo], saveDraft, { deep: true })

onBeforeUnmount(() => {
  clearTimeout(draftTimer)
  // 离开页面前落盘一次，避免最后 800ms 内的输入丢失
  if (Object.keys(formData.value).length || Object.keys(settlementInfo.value).length) {
    saveDraft()
  }
})

/** 草稿保存时间文案 */
const draftTimeText = draft?.savedAt
  ? new Date(draft.savedAt).toLocaleString('zh-CN', { hour12: false })
  : ''

// 切换搜索面板
const toggleSearch = () => {
  isSearchExpanded.value = !isSearchExpanded.value
}

// 关闭搜索面板
const closeSearch = () => {
  isSearchExpanded.value = false
}

// 处理搜索
const handleSearch = (keyword) => {
  console.log('搜索:', keyword)
  ElMessage.info(`搜索功能开发中：${keyword}`)
}

// 处理表单变化
const handleFormChange = (data) => {
  formData.value = { ...data }
}

// 处理结算信息变化
const handleSettlementChange = (data) => {
  settlementInfo.value = { ...data }
}

// 处理图片变化
const handleImageChange = ({ image, file }) => {
  selectedImage.value = image
  selectedFile.value = file
}

// 处理协议同意状态变化
const handleAgreementChange = (value) => {
  agreed.value = value
}

// 重置表单
const handleReset = () => {
  selectedImage.value = null
  selectedFile.value = null
  agreed.value = false
  formData.value = {}
  settlementInfo.value = {}
  initialForm.value = {}
  initialSettlement.value = {}
  formKey.value += 1
  clearDraft()
  draftBlockedUntil = Date.now() + 1500
  draftHasContent.value = false
  if (xieYiTongYiHeAnNiu.value) {
    xieYiTongYiHeAnNiu.value.resetAgreement()
  }
}

// 提交商品
const handleSubmit = async () => {
  // 验证必填项
  if (!formData.value.accountType) {
    ElMessage.warning('请选择账号类型')
    return
  }
  if (!formData.value.server) {
    ElMessage.warning('请选择区服')
    return
  }
  if (!formData.value.settlementType) {
    ElMessage.warning('请选择结算方式')
    return
  }
  if (!formData.value.banRecord) {
    ElMessage.warning('请选择封禁记录')
    return
  }
  if (!formData.value.faceSelf) {
    ElMessage.warning('请选择人脸是否本人')
    return
  }
  if (!formData.value.selfCheck) {
    ElMessage.warning('请确认打完改密')
    return
  }
  if (!selectedFile.value) {
    ElMessage.warning('请先上传商品图片')
    return
  }
  if (!agreed.value) {
    ElMessage.warning('请先阅读并同意相关协议')
    return
  }
  if (formData.value.settlementRatio === 'quick' && !formData.value.quickRatioValue) {
    ElMessage.warning('请输入快速比例值')
    return
  }

  submitting.value = true

  try {
    // 先上传封面图到后端，拿到可被 <img> 访问的 URL；失败时回退为本地 base64 预览图
    let coverUrl = selectedImage.value
    if (selectedFile.value) {
      try {
        const uploadRes = await productApi.uploadImage(selectedFile.value)
        const uploaded = uploadRes?.coverImageUrl || uploadRes?.url
        if (uploaded) coverUrl = uploaded
      } catch (uploadError) {
        console.warn('封面图上传失败，改用本地预览图提交', uploadError)
      }
    }

    // 构建完整的商品数据
    const product = {
      // 基础信息
      personalTag:
        formData.value.personalTag ||
        `${formData.value.accountType} - ${formData.value.rank || '账号'}`,
      coverImageUrl: coverUrl,

      // 账号基本信息
      accountType: formData.value.accountType,
      server: formData.value.server,
      settlementType: formData.value.settlementType,
      banRecord: formData.value.banRecord,
      faceSelf: formData.value.faceSelf,

      // 资产信息
      warehouseAssets: formData.value.warehouseAssets,
      pureCoinM: formData.value.pureCoinM,

      // 训练信息
      trainingLevel: formData.value.trainingLevel,
      rangeLevel: formData.value.rangeLevel,
      safeBox: formData.value.safeBox,

      // 装备信息
      rank: formData.value.rank,
      accountLevel: formData.value.accountLevel,
      awmBullets: formData.value.awmBullets,
      sixHead: formData.value.sixHead,
      sixArmor: formData.value.sixArmor,
      secretKD: formData.value.secretKD,

      // 皮肤信息
      knifeSkins: formData.value.knifeSkins || [],
      operatorSkins: formData.value.operatorSkins || [],
      weaponSkins: formData.value.weaponSkins || [],

      // 其他信息
      ownerBanned: formData.value.ownerBanned,
      ownerGifted: formData.value.ownerGifted,
      remark: formData.value.remark,
      tradeStartHour: formData.value.tradeStartHour,
      tradeEndHour: formData.value.tradeEndHour,
      selfCheck: formData.value.selfCheck,

      // 结算信息
      settlementRatio: settlementInfo.value.settlementType === 'standard' ? 'standard' : 'quick',
      quickRatioValue: settlementInfo.value.quickRatioValue || formData.value.quickRatioValue,
      preRent: settlementInfo.value.preRent,
      deposit: settlementInfo.value.deposit,
      halfCoinRatio: settlementInfo.value.halfCoinRatio,

      // 卡片展示字段（兼容）
      tuPianUrl: coverUrl,
      zongZiChan: formData.value.warehouseAssets,
      quFu: formData.value.server,
      zuJin: settlementInfo.value.preRent,
      mingCheng:
        formData.value.personalTag ||
        `${formData.value.accountType} - ${formData.value.rank || '账号'}`,
      leiBie: formData.value.accountType,
    }

    // 调用API上传商品
    await productApi.uploadProduct(product)

    ElMessage.success('上架成功！商品已保存至数据库')

    // 上架成功后清掉草稿，避免下次打开又弹出旧内容
    clearDraft()
    // 重置表单
    handleReset()
  } catch (error) {
    console.error('上架失败:', error)
    ElMessage.error('上架失败：' + (error.response?.data?.message || error.message || '请重试'))
  } finally {
    submitting.value = false
  }
}

// 组件挂载时重置表单
onMounted(() => {
  // 没有草稿才重置；有草稿时保留恢复出来的内容
  if (!draftHasContent.value) {
    handleReset()
  } else {
    ElMessage({
      message: `已恢复 ${draftTimeText} 保存的草稿，图片需要重新上传`,
      type: 'info',
      duration: 4000,
    })
  }
})
</script>

<style scoped>
.shang-jia-ye-mian {
  min-height: 100vh;
  background-color: #ffffffff;
  font-family:
    'PingFang SC',
    'Microsoft YaHei',
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    Roboto,
    'Helvetica Neue',
    Arial,
    sans-serif;
}

.shang-jia-nei-rong {
  padding-top: 140px;
  padding-bottom: 40px;
  padding-left: 20px;
  padding-right: 20px;
}

.ye-mian-biao-ti {
  font-size: 32px;
  font-weight: 600;
  color: #333;
  margin: 0 0 30px 50px;
  text-align: left;
}

.cao-gao-ti-shi {
  margin: -10px 0 24px 50px;
  padding: 10px 16px;
  max-width: 900px;
  font-size: 14px;
  color: #00b1fe;
  background: #e6f7ff;
  border: 1px solid #b3e5ff;
  border-radius: 8px;
}

.zhu-yao-rong-qi {
  max-width: 1000px;
}

.padding-wai-kuang {
  background-color: transparent;
  border: none;
  border-radius: 12px;
  padding: 32px 32px 32px 50px;
}

.tong-yi-rong-qi {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.nei-rong-qu {
  background-color: transparent;
  padding: 0;
}

.qu-biao-ti {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  text-align: left;
}

.ka-pian {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  margin-bottom: 24px;
}

.ka-pian:last-child {
  margin-bottom: 0;
}

.ka-pian :deep(.el-card__header) {
  padding: 20px 24px;
  background-color: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.ka-pian :deep(.el-card__body) {
  padding: 24px;
}

.shang-chuan-qu {
  position: sticky;
  top: 100px;
}

@media (max-width: 1024px) {
  .zhu-yao-rong-qi {
    grid-template-columns: 1fr;
  }

  .shang-chuan-qu {
    position: static;
  }
}

@media (max-width: 768px) {
  .shang-jia-nei-rong {
    padding-left: 16px;
    padding-right: 16px;
  }

  .ye-mian-tou-bu {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>
