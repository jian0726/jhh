<template>
  <!-- 搜索/筛选面板 -->
  <transition name="panel-slide">
    <div
      v-show="isExpanded"
      class="panel-expand"
      :class="{ 'is-filter': isFilterMode }"
      role="search"
      aria-label="搜索筛选区域"
      ref="panelRef"
      @click.stop
    >
      <div class="panel-container" @click.stop>
        <!-- 搜索模式 -->
        <div v-show="!isFilterMode" class="search-content">
          <input
            type="text"
            class="search-input"
            placeholder="搜索商品..."
            aria-label="搜索输入框"
            ref="searchInputRef"
            @keyup.enter="handleSearch"
          />

          <!-- 筛选按钮 -->
          <button
            class="filter-btn"
            @mousedown.prevent
            @click="toggleFilter"
            @mouseenter="isFilterHovered = true"
            @mouseleave="isFilterHovered = false"
          >
            <img
              :src="
                isFilterHovered ? '../../img/filter-3-line-blue.svg' : '../../img/filter-3-line.svg'
              "
              alt="筛选"
              class="filter-icon"
            />
          </button>
        </div>

        <!-- 筛选模式 -->
        <div v-show="isFilterMode" class="filter-panel-content">
          <!-- 筛选头部 -->
          <div class="filter-header">
            <div class="filter-title">筛选条件</div>
            <div class="filter-header-actions">
              <button class="link-btn" @click="handleExpandAll">展开全部</button>
              <button class="link-btn" @click="handleResetAll">重置筛选</button>
            </div>
          </div>

          <!-- 已选条件 -->
          <div class="selected-bar" :class="{ show: selectedCount > 0 }">
            <div class="selected-bar-head">
              <span class="selected-label">已选条件：</span>
              <span class="selected-count">{{ selectedCount }}个</span>
              <span class="selected-tip">鼠标悬停查看详情</span>
            </div>
            <div class="selected-tags-wrap">
              <span v-for="(tag, index) in selectedTags" :key="index" class="selected-tag">
                {{ tag }}
                <span class="close" @click="removeTag(index)">×</span>
              </span>
            </div>
          </div>

          <!-- 筛选行 -->
          <div class="filter-line">
            <div class="filter-label">账号类型</div>
            <div class="filter-content">
              <div class="tag-list">
                <span
                  v-for="item in filterOptions.accountType"
                  :key="item"
                  class="tag-item"
                  :class="{ active: isSelected('accountType', item) }"
                  @click="toggleTag('accountType', item)"
                  >{{ item }}</span
                >
              </div>
            </div>
          </div>

          <div class="filter-line">
            <div class="filter-label">账号等级</div>
            <div class="filter-content">
              <div class="input-group">
                <input
                  type="number"
                  class="single-input"
                  placeholder="请输入账号等级（30-60级）"
                  min="30"
                  max="60"
                  v-model="filterData.accountLevel"
                  @blur="validateLevel"
                />
                <span class="range-unit">级</span>
              </div>
            </div>
          </div>

          <div class="filter-line">
            <div class="filter-label">训练中心等级</div>
            <div class="filter-content">
              <div class="tag-list">
                <span
                  v-for="item in filterOptions.training"
                  :key="item"
                  class="tag-item"
                  :class="{ active: isSelected('training', item) }"
                  @click="toggleTag('training', item)"
                  >{{ item }}</span
                >
              </div>
            </div>
          </div>

          <div class="filter-line">
            <div class="filter-label">靶场等级</div>
            <div class="filter-content">
              <div class="tag-list">
                <span
                  v-for="item in filterOptions.shootingRange"
                  :key="item"
                  class="tag-item"
                  :class="{ active: isSelected('shootingRange', item) }"
                  @click="toggleTag('shootingRange', item)"
                  >{{ item }}</span
                >
              </div>
            </div>
          </div>

          <div class="filter-line">
            <div class="filter-label">潜水中心等级</div>
            <div class="filter-content">
              <div class="tag-list">
                <span
                  v-for="item in filterOptions.diveCenter"
                  :key="item"
                  class="tag-item"
                  :class="{ active: isSelected('diveCenter', item) }"
                  @click="toggleTag('diveCenter', item)"
                  >{{ item }}</span
                >
              </div>
            </div>
          </div>

          <div class="filter-line">
            <div class="filter-label">近战刀皮</div>
            <div class="filter-content">
              <div class="tag-list" :class="{ collapsed: !expandedSections.melee }">
                <span
                  v-for="item in filterOptions.melee"
                  :key="item"
                  class="tag-item"
                  :class="{ active: isSelected('melee', item) }"
                  @click="toggleTag('melee', item)"
                  >{{ item }}</span
                >
              </div>
              <span class="select-all-btn" @click="toggleSelectAll('melee')">
                {{ isAllSelected('melee') ? '取消全选' : '全选' }}
              </span>
              <span
                class="toggle-more"
                :class="{ open: expandedSections.melee }"
                @click="toggleSection('melee')"
              >
                更多 <span class="arrow">∨</span>
              </span>
            </div>
          </div>

          <div class="filter-line">
            <div class="filter-label">特战干员外观</div>
            <div class="filter-content">
              <div class="tag-list" :class="{ collapsed: !expandedSections.agent }">
                <span
                  v-for="item in filterOptions.agent"
                  :key="item"
                  class="tag-item"
                  :class="{ active: isSelected('agent', item) }"
                  @click="toggleTag('agent', item)"
                  >{{ item }}</span
                >
              </div>
              <span class="select-all-btn" @click="toggleSelectAll('agent')">
                {{ isAllSelected('agent') ? '取消全选' : '全选' }}
              </span>
              <span
                class="toggle-more"
                :class="{ open: expandedSections.agent }"
                @click="toggleSection('agent')"
              >
                更多 <span class="arrow">∨</span>
              </span>
            </div>
          </div>

          <div class="filter-line">
            <div class="filter-label">典藏枪皮</div>
            <div class="filter-content">
              <div class="tag-list" :class="{ collapsed: !expandedSections.gunSkin }">
                <span
                  v-for="item in filterOptions.gunSkin"
                  :key="item"
                  class="tag-item"
                  :class="{ active: isSelected('gunSkin', item) }"
                  @click="toggleTag('gunSkin', item)"
                  >{{ item }}</span
                >
              </div>
              <span class="select-all-btn" @click="toggleSelectAll('gunSkin')">
                {{ isAllSelected('gunSkin') ? '取消全选' : '全选' }}
              </span>
              <span
                class="toggle-more"
                :class="{ open: expandedSections.gunSkin }"
                @click="toggleSection('gunSkin')"
              >
                更多 <span class="arrow">∨</span>
              </span>
            </div>
          </div>

          <div class="filter-line">
            <div class="filter-label">安全箱</div>
            <div class="filter-content">
              <div class="tag-list">
                <span
                  v-for="item in filterOptions.safeBox"
                  :key="item"
                  class="tag-item"
                  :class="{ active: isSelected('safeBox', item) }"
                  @click="toggleTag('safeBox', item)"
                  >{{ item }}</span
                >
              </div>
            </div>
          </div>

          <div class="filter-line">
            <div class="filter-label">当前段位</div>
            <div class="filter-content">
              <div class="tag-list">
                <span
                  v-for="item in filterOptions.rank"
                  :key="item"
                  class="tag-item"
                  :class="{ active: isSelected('rank', item) }"
                  @click="toggleTag('rank', item)"
                  >{{ item }}</span
                >
              </div>
            </div>
          </div>

          <div class="filter-line">
            <div class="filter-label">价格区间</div>
            <div class="filter-content">
              <div class="range-group">
                <input
                  type="number"
                  class="range-input"
                  placeholder="最低价"
                  min="0"
                  v-model="filterData.priceMin"
                  @blur="validatePriceMin"
                />
                <span class="range-split">—</span>
                <input
                  type="number"
                  class="range-input"
                  placeholder="最高价"
                  min="0"
                  v-model="filterData.priceMax"
                  @blur="validatePriceMax"
                />
                <span class="range-unit">元</span>
              </div>
            </div>
          </div>

          <!-- 底部操作 -->
          <div class="filter-footer">
            <button class="btn btn-default" @click="handleBackToSearch">返回搜索</button>
            <button class="btn btn-default" @click="handleResetAll">重置</button>
            <button class="btn btn-primary" @click="handleConfirm">确认筛选</button>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
/**
 * Sou_Suo_Mian_Ban.vue - 搜索面板组件
 *
 * 包含搜索和筛选两个功能：
 * - 搜索模式：显示搜索输入框和筛选按钮
 * - 筛选模式：点击筛选按钮后，搜索内容消失，面板从底部展开显示筛选条件
 */
import { ref, reactive, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps({
  isExpanded: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['search', 'close', 'filter'])

const searchInputRef = ref(null)
const panelRef = ref(null)
const isFilterHovered = ref(false)
const isFilterMode = ref(false)

// 筛选选项数据
const filterOptions = reactive({
  accountType: ['QQ', '微信'],
  training: ['5级', '6级', '7级'],
  shootingRange: ['5级', '6级', '7级'],
  diveCenter: ['1级', '2级', '3级'],
  melee: [
    '近战-北极星',
    '近战-信条',
    '近战-坠星者',
    '近战-怜悯',
    '近战-暗星',
    '近战-赤枭',
    '近战-黑海',
    '近战-龙牙',
    '近战-影锋',
    '近战-电锯惊魂',
    '近战-处刑者',
    '近战-黑鹰',
    '近战-挽歌',
  ],
  agent: [
    '银翼-未结卷宗',
    '红狼-蚀金玫瑰',
    '露娜-黑天际线',
    '威龙-凌霄戍卫',
    '骇爪-维什戴尔',
    '威龙-飞虎',
    '骇爪-水墨云图',
    '蛊-能天使午夜邮差',
    '威龙-蛟龙特战队',
    '威龙-铁面判官',
    '露娜-劳拉·克劳馥',
    '蜂医-送葬人无题密令',
    '威龙-壮志凌云',
    '露娜-金牌射手',
    '红狼-电锯惊魂',
    '蜂医-危险物质',
    '乌鲁鲁-荒原猎手',
    '蛊-不羁人生',
    '无名-夜鹰',
    '深蓝-不破誓约',
    '牧羊人-街头之王',
    '蜂医-黑鹰坠落',
    '红狼-黑鹰坠落',
    '乌鲁鲁-黑鹰坠落',
    '牧羊人-黑鹰坠落',
  ],
  gunSkin: [
    'KC17造物纪元',
    'M250电玩S2',
    'MP7电玩S2',
    'ASVal悬赏令',
    '腾龙气象感应',
    'AUG气象感应',
    'M7棱镜S2',
    'K416命运',
    'QBZ95王牌之剑',
    'SCAR电玩高手',
    'Vector美杜莎',
    'M4A1棱镜攻势',
  ],
  safeBox: ['无安全箱', '进阶安全箱4(2×2)', '高级安全箱6(2×3)', '顶级安全箱9(3×3)'],
  rank: ['三角洲巅峰', '黑鹰', '钻石', '铂金', '白银', '黄金'],
})

// 筛选数据
const filterData = reactive({
  accountType: [],
  training: [],
  shootingRange: [],
  diveCenter: [],
  melee: [],
  agent: [],
  gunSkin: [],
  safeBox: [],
  rank: [],
  accountLevel: '',
  priceMin: '',
  priceMax: '',
})

// 展开状态
const expandedSections = reactive({
  melee: false,
  agent: false,
  gunSkin: false,
})

// 计算已选数量
const selectedCount = computed(() => {
  let count = 0
  Object.keys(filterData).forEach((key) => {
    if (Array.isArray(filterData[key])) {
      count += filterData[key].length
    } else if (filterData[key] !== '' && filterData[key] !== null) {
      count++
    }
  })
  return count
})

// 已选标签列表
const selectedTags = computed(() => {
  const tags = []
  Object.keys(filterData).forEach((key) => {
    if (Array.isArray(filterData[key])) {
      filterData[key].forEach((item) => tags.push(item))
    } else if (filterData[key] !== '' && filterData[key] !== null) {
      const labelMap = {
        accountLevel: `等级${filterData.accountLevel}级`,
        priceMin: `最低价${filterData.priceMin}元`,
        priceMax: `最高价${filterData.priceMax}元`,
      }
      if (labelMap[key]) tags.push(labelMap[key])
    }
  })
  return tags
})

// 判断是否选中
const isSelected = (type, value) => {
  return filterData[type]?.includes(value)
}

// 切换标签
const toggleTag = (type, value) => {
  const multiSelectTypes = ['melee', 'agent', 'gunSkin']
  if (multiSelectTypes.includes(type)) {
    const index = filterData[type].indexOf(value)
    if (index > -1) {
      filterData[type].splice(index, 1)
    } else {
      filterData[type].push(value)
    }
  } else {
    if (filterData[type].includes(value)) {
      filterData[type] = []
    } else {
      filterData[type] = [value]
    }
  }
}

// 全选/取消全选
const toggleSelectAll = (type) => {
  if (isAllSelected(type)) {
    filterData[type] = []
  } else {
    filterData[type] = [...filterOptions[type]]
  }
}

// 是否全选
const isAllSelected = (type) => {
  return filterData[type].length === filterOptions[type].length
}

// 展开/收起更多
const toggleSection = (type) => {
  expandedSections[type] = !expandedSections[type]
}

// 展开全部
const handleExpandAll = () => {
  expandedSections.melee = true
  expandedSections.agent = true
  expandedSections.gunSkin = true
}

// 重置筛选
const handleResetAll = () => {
  Object.keys(filterData).forEach((key) => {
    if (Array.isArray(filterData[key])) {
      filterData[key] = []
    } else {
      filterData[key] = ''
    }
  })
}

// 确认筛选
const handleConfirm = () => {
  // 将筛选数据通过路由参数传递到筛选结果页面
  const filterParams = encodeURIComponent(JSON.stringify(filterData))
  router.push({ path: '/filter', query: { filters: filterParams } })
}

// 移除标签
const removeTag = (index) => {
  const tag = selectedTags.value[index]
  Object.keys(filterData).forEach((key) => {
    if (Array.isArray(filterData[key])) {
      const i = filterData[key].indexOf(tag)
      if (i > -1) filterData[key].splice(i, 1)
    }
  })
}

// 校验等级
const validateLevel = () => {
  let val = parseInt(filterData.accountLevel)
  if (isNaN(val)) return
  if (val < 30) filterData.accountLevel = 30
  if (val > 60) filterData.accountLevel = 60
}

// 校验价格
const validatePriceMin = () => {
  let val = parseFloat(filterData.priceMin)
  if (isNaN(val)) return
  if (val < 0) filterData.priceMin = 0
}

const validatePriceMax = () => {
  let val = parseFloat(filterData.priceMax)
  if (isNaN(val)) return
  if (val < 0) filterData.priceMax = 0
}

// 切换筛选模式
const toggleFilter = () => {
  isFilterMode.value = true
  nextTick(() => {
    if (searchInputRef.value) {
      searchInputRef.value.blur()
    }
  })
}

// 返回搜索模式
const handleBackToSearch = () => {
  isFilterMode.value = false
  nextTick(() => {
    if (searchInputRef.value) {
      searchInputRef.value.focus()
    }
  })
}

// 处理搜索
const handleSearch = () => {
  if (searchInputRef.value && searchInputRef.value.value.trim()) {
    emit('search', searchInputRef.value.value)
  }
}

// 点击外部关闭
const handleClickOutside = (event) => {
  const target = event.target
  const panel = panelRef.value

  const inPanel = panel && (panel === target || panel.contains(target))

  if (!inPanel) {
    if (isFilterMode.value) {
      // 筛选模式下，点击外部先返回搜索模式
      isFilterMode.value = false
    } else {
      // 搜索模式下，点击外部关闭面板
      emit('close')
    }
  }
}

// 监听外部 isExpanded 变化
watch(
  () => props.isExpanded,
  (newVal) => {
    if (newVal) {
      isFilterMode.value = false
    } else {
      isFilterMode.value = false
      if (searchInputRef.value) {
        searchInputRef.value.value = ''
      }
    }
  },
)

onMounted(() => {
  document.addEventListener('click', handleClickOutside, true)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside, true)
})
</script>

<style scoped>
/* 面板容器 */
.panel-expand {
  position: fixed;
  top: 120px;
  left: 0;
  right: 0;
  height: calc(25vh);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 40px;
  background-color: #ffffff;
  z-index: 999;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  will-change: height, opacity;
  transition:
    height 0.3s ease,
    opacity 0.2s ease,
    align-items 0.3s ease,
    padding 0.3s ease;
}

/* 筛选模式下，面板从底部展开 */
.panel-expand.is-filter {
  height: 80vh;
  align-items: flex-start;
}

.panel-expand.is-filter .panel-container {
  max-height: calc(80vh - 40px);
  overflow-y: auto;
}

.panel-container {
  width: 100%;
  max-width: 1200px;
}

/* 搜索内容 */
.search-content {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
}

.search-input {
  width: 600px;
  height: 44px;
  padding: 0 16px;
  border: 1px solid #333;
  border-radius: 8px;
  outline: none;
  font-size: 15px;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  color: #111827;
  background-color: #ffffff;
  transition: all 0.2s ease;
}

.search-input:focus {
  border-color: #00b1fe;
  box-shadow: 0 0 0 2px rgba(0, 177, 254, 0.2);
}

.search-input::placeholder {
  color: #888888;
}

.filter-btn {
  height: 44px;
  padding: 0 12px;
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease;
}

.filter-btn:hover {
  color: #00b1fe;
}

.filter-btn:focus {
  outline: none;
}

.filter-icon {
  width: 24px;
  height: 24px;
}

/* 筛选内容 */
.filter-panel-content {
  padding: 20px 24px;
  font-size: 14px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f2f3f5;
}

.filter-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.filter-header-actions {
  display: flex;
  gap: 12px;
  font-size: 13px;
}

.link-btn {
  background: none;
  border: none;
  color: #333;
  cursor: pointer;
  transition: color 0.2s;
}

.link-btn:hover {
  color: #00b1fe;
}

.selected-bar {
  display: none;
  background: #f7f8fa;
  border-radius: 6px;
  margin-bottom: 16px;
  overflow: hidden;
  max-height: 40px;
  transition: max-height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.selected-bar.show {
  display: block;
}

.selected-bar:hover {
  max-height: 220px;
}

.selected-bar-head {
  padding: 10px 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.selected-label {
  color: #86909c;
  font-size: 13px;
}

.selected-count {
  color: #00b1fe;
  font-size: 13px;
  font-weight: 500;
}

.selected-tip {
  color: #c9cdd4;
  font-size: 12px;
  margin-left: 8px;
}

.selected-tags-wrap {
  padding: 0 14px 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selected-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  background: #e8f3ff;
  color: #00b1fe;
  border-radius: 4px;
  font-size: 12px;
}

.selected-tag .close {
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.selected-tag .close:hover {
  opacity: 1;
}

.filter-line {
  display: flex;
  margin-bottom: 14px;
  align-items: flex-start;
}

.filter-line:last-child {
  margin-bottom: 0;
}

.filter-label {
  width: 110px;
  flex-shrink: 0;
  padding-top: 6px;
  color: #333;
  font-weight: 500;
  font-size: 13px;
}

.filter-content {
  flex: 1;
  position: relative;
  padding-right: 110px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 10px;
}

.tag-item {
  padding: 4px 12px;
  border: 1px solid #e5e6eb;
  border-radius: 4px;
  background: #ffffff;
  color: #333;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.tag-item:hover {
  border-color: #00b1fe;
  color: #00b1fe;
  background: #f2f6ff;
}

.tag-item.active {
  background: #00b1fe;
  border-color: #00b1fe;
  color: #ffffff;
}

.tag-list.collapsed .tag-item:nth-child(n + 7) {
  display: none;
}

.select-all-btn {
  position: absolute;
  right: 55px;
  top: 6px;
  font-size: 12px;
  color: #333;
  cursor: pointer;
  background: #fff;
  padding-left: 8px;
  transition: color 0.2s;
}

.select-all-btn:hover {
  color: #00b1fe;
}

.toggle-more {
  position: absolute;
  right: 0;
  top: 6px;
  font-size: 12px;
  color: #333;
  cursor: pointer;
  background: #fff;
  padding-left: 8px;
  transition: color 0.2s;
}

.toggle-more:hover {
  color: #00b1fe;
}

.toggle-more .arrow {
  display: inline-block;
  transition: transform 0.2s;
  margin-left: 2px;
}

.toggle-more.open .arrow {
  transform: rotate(180deg);
}

.input-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.single-input {
  width: 180px;
  height: 30px;
  padding: 0 10px;
  border: 1px solid #e5e6eb;
  border-radius: 4px;
  outline: none;
  font-size: 13px;
  transition: border-color 0.2s;
}

.single-input:focus {
  border-color: #00b1fe;
}

.range-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.range-input {
  width: 120px;
  height: 30px;
  padding: 0 10px;
  border: 1px solid #e5e6eb;
  border-radius: 4px;
  outline: none;
  font-size: 13px;
  transition: border-color 0.2s;
}

.range-input:focus {
  border-color: #00b1fe;
}

.range-split {
  color: #c9cdd4;
}

.range-unit {
  color: #86909c;
  font-size: 13px;
  margin-left: 4px;
}

.filter-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 12px;
  border-top: 1px solid #f2f3f5;
}

.btn {
  height: 32px;
  padding: 0 18px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.btn-default {
  background: #fff;
  border-color: #e5e6eb;
  color: #333;
}

.btn-default:hover {
  border-color: #00b1fe;
  color: #00b1fe;
}

.btn-primary {
  background: #00b1fe;
  color: #fff;
}

.btn-primary:hover {
  background: #009ae0;
}

/* 动画 */
.panel-slide-enter-active,
.panel-slide-leave-active {
  transition:
    height 0.3s ease,
    opacity 0.2s ease;
  overflow: hidden;
}

.panel-slide-enter-from {
  height: 0;
  opacity: 0;
}

.panel-slide-enter-to {
  height: calc(25vh);
  opacity: 1;
}

.panel-slide-leave-to {
  height: 0;
  opacity: 0;
}
</style>
