<template>
  <div class="personal-center">
    <DingBuDaoHangLan />

    <div class="main-content">
      <CeBianDaoHangLan :default-active="currentMenu" @menu-change="handleMenuChange" />

      <div class="content-area">
        <!-- 未登录：所有菜单统一提示先登录 -->
        <el-card v-if="!userStore.isLogin" class="content-card">
          <template #header>
            <span class="card-title">个人中心</span>
          </template>
          <div class="not-logged-in">
            <p>您还没有登录，登录后可查看个人资料、我的商品、收藏与订单。</p>
            <el-button type="primary" @click="authStore.openLogin()">立即登录</el-button>
          </div>
        </el-card>

        <template v-else>
          <!-- ========== 个人资料 ========== -->
          <template v-if="currentMenu === 'personal'">
            <el-card class="content-card">
              <template #header>
                <span class="card-title">个人资料</span>
              </template>
              <div class="profile-info">
                <div class="info-row"><label>用户ID：</label><span>{{ userStore.userInfo?.id ?? '--' }}</span></div>
                <div class="info-row"><label>用户名：</label><span>{{ userStore.userInfo?.yongHuMing || '--' }}</span></div>
                <div class="info-row"><label>手机号：</label><span>{{ userStore.userInfo?.shouJiHao || '--' }}</span></div>
                <div class="info-row"><label>邮箱：</label><span>{{ userStore.userInfo?.email || '--' }}</span></div>
                <div class="info-row"><label>角色：</label><span>{{ roleText }}</span></div>
              </div>
            </el-card>

            <el-card class="content-card">
              <template #header>
                <span class="card-title">订单统计</span>
              </template>
              <div v-loading="isLoadingData" class="stats-grid">
                <div class="stat-box">
                  <span class="stat-num">{{ myOrders.length }}</span>
                  <span class="stat-label">全部订单</span>
                </div>
                <div class="stat-box">
                  <span class="stat-num">{{ myOrders.filter((o) => o.zhuangTai === 0).length }}</span>
                  <span class="stat-label">待支付</span>
                </div>
                <div class="stat-box">
                  <span class="stat-num">{{ myOrders.filter((o) => o.zhuangTai === 1).length }}</span>
                  <span class="stat-label">租赁中</span>
                </div>
                <div class="stat-box">
                  <span class="stat-num">{{ myOrders.filter((o) => o.zhuangTai === 2).length }}</span>
                  <span class="stat-label">已完成</span>
                </div>
              </div>
            </el-card>
          </template>

          <!-- ========== 我的商品 ========== -->
          <template v-if="currentMenu === 'my-products'">
            <el-card class="content-card" v-loading="isLoadingData">
              <template #header>
                <span class="card-title">我的商品</span>
              </template>
              <div v-if="!isLoadingData && myProducts.length === 0" class="empty-state">
                <el-empty description="暂无上架商品">
                  <el-button type="primary" @click="goShelf">去上架</el-button>
                </el-empty>
              </div>
              <div v-else-if="!isLoadingData" class="favorite-grid">
                <div
                  v-for="item in myProducts"
                  :key="item.id"
                  class="favorite-card"
                  @click="goProduct(item.id)"
                >
                  <img
                    v-if="pickImageUrl(item)"
                    :src="pickImageUrl(item)"
                    :alt="item.mingCheng || item.biaoTi"
                    class="fav-img"
                  />
                  <div v-else class="fav-img-placeholder">
                    <span class="placeholder-text">暂无图片</span>
                  </div>
                  <div class="fav-info">
                    <h4>{{ item.mingCheng || item.biaoTi || '商品' }}</h4>
                    <p class="fav-price">¥{{ item.riZuJiaGe || item.preRent || '0.00' }}/天</p>
                    <p class="fav-sub">状态：{{ productStatusText(item.zhuangTai) }}</p>
                  </div>
                  <el-button
                    v-if="item.zhuangTai === 1"
                    size="small"
                    type="danger"
                    text
                    @click.stop="xiajiaMyProduct(item.id)"
                    >下架</el-button
                  >
                </div>
              </div>
            </el-card>
          </template>

          <!-- ========== 收藏 ========== -->
          <template v-if="currentMenu === 'favorite'">
            <el-card class="content-card" v-loading="isLoadingData">
              <template #header>
                <span class="card-title">我的收藏</span>
              </template>
              <div v-if="!isLoadingData && favorites.length === 0" class="empty-state">
                <el-empty description="暂无收藏" />
              </div>
              <div v-else-if="!isLoadingData" class="favorite-grid">
                <div
                  v-for="item in favorites"
                  :key="item.id"
                  class="favorite-card"
                  @click="goProduct(item.id)"
                >
                  <img
                    v-if="pickImageUrl(item) && !favImgErrors[item.id]"
                    :src="pickImageUrl(item)"
                    :alt="item.biaoTi || item.mingCheng"
                    class="fav-img"
                    @error="handleFavImgError(item)"
                  />
                  <div v-else class="fav-img-placeholder">
                    <span class="placeholder-text">暂无图片</span>
                  </div>
                  <div class="fav-info">
                    <h4>{{ item.biaoTi || item.mingCheng }}</h4>
                    <p class="fav-price">¥{{ item.riZuJiaGe || item.preRent || '0.00' }}/天</p>
                  </div>
                  <el-button size="small" type="danger" text @click.stop="removeFav(item.id)"
                    >取消收藏</el-button
                  >
                </div>
              </div>
            </el-card>
          </template>

          <!-- ========== 我的订单 - 买家 ========== -->
          <template v-if="currentMenu === 'order-buyer'">
            <el-card class="content-card" v-loading="isLoadingData">
              <template #header>
                <span class="card-title">我的订单（我是买家）</span>
              </template>
              <div v-if="!isLoadingData && myOrders.length === 0" class="empty-state">
                <el-empty description="暂无订单" />
              </div>
              <el-table v-else-if="!isLoadingData" :data="myOrders" stripe style="width: 100%">
                <el-table-column prop="dingDanHao" label="订单号" min-width="200" />
                <el-table-column label="金额" width="120">
                  <template #default="{ row }">¥{{ row.zongJinE || '0.00' }}</template>
                </el-table-column>
                <el-table-column label="租期" width="100">
                  <template #default="{ row }">{{ row.zuJieTianShu || '-' }}天</template>
                </el-table-column>
                <el-table-column label="状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="orderStatusType(row.zhuangTai)" size="small">
                      {{ orderStatusText(row.zhuangTai) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="260" fixed="right">
                  <template #default="{ row }">
                    <el-button v-if="row.zhuangTai === 0" size="small" type="primary" @click="payOrder(row)"
                      >去支付</el-button
                    >
                    <el-button v-if="row.zhuangTai === 0" size="small" @click="cancelOrder(row)">取消</el-button>
                    <el-button v-if="row.zhuangTai === 1" size="small" @click="inspectOrder(row)">验号</el-button>
                    <el-button v-if="row.zhuangTai === 1" size="small" type="success" @click="returnOrder(row)"
                      >归还</el-button
                    >
                    <el-button v-if="row.zhuangTai === 5" size="small" type="success" @click="confirmReceive(row)"
                      >确认收货</el-button
                    >
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </template>

          <!-- ========== 我的订单 - 卖家 ========== -->
          <template v-if="currentMenu === 'order-seller'">
            <el-card class="content-card" v-loading="isLoadingData">
              <template #header>
                <span class="card-title">我的订单（我是卖家）</span>
              </template>
              <div v-if="!isLoadingData && sellerOrders.length === 0" class="empty-state">
                <el-empty description="暂无卖出订单" />
              </div>
              <el-table v-else-if="!isLoadingData" :data="sellerOrders" stripe style="width: 100%">
                <el-table-column prop="dingDanHao" label="订单号" min-width="200" />
                <el-table-column label="金额" width="120">
                  <template #default="{ row }">¥{{ row.zongJinE || '0.00' }}</template>
                </el-table-column>
                <el-table-column label="租期" width="100">
                  <template #default="{ row }">{{ row.zuJieTianShu || '-' }}天</template>
                </el-table-column>
                <el-table-column label="状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="orderStatusType(row.zhuangTai)" size="small">
                      {{ orderStatusText(row.zhuangTai) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="买家ID" width="100" prop="maiJiaId2" />
              </el-table>
            </el-card>
          </template>

          <!-- ========== 消息 ========== -->
          <template v-if="currentMenu === 'message'">
            <el-card class="content-card" v-loading="isLoadingData">
              <template #header>
                <div class="card-header-row">
                  <span class="card-title">消息中心</span>
                  <el-button v-if="messages.length > 0" size="small" text @click="markAllRead">全部已读</el-button>
                </div>
              </template>
              <div v-if="!isLoadingData && messages.length === 0" class="empty-state">
                <el-empty description="暂无消息" />
              </div>
              <div v-else-if="!isLoadingData" class="message-list">
                <div
                  v-for="msg in messages"
                  :key="msg.id"
                  class="message-item"
                  :class="{ unread: isUnread(msg) }"
                  @click="markRead(msg)"
                >
                  <div class="msg-header">
                    <span class="msg-type">{{ messageTypeText(msg.leiXing) }}</span>
                    <span class="msg-time">{{ formatTime(msg.faSongShiJian || msg.chuangJianShiJian) }}</span>
                  </div>
                  <div class="msg-title">{{ msg.biaoTi }}</div>
                  <div class="msg-content">{{ msg.neiRong }}</div>
                  <div v-if="isUnread(msg)" class="unread-dot" />
                </div>
              </div>
            </el-card>
          </template>

          <!-- ========== 联系客服 ========== -->
          <template v-if="currentMenu === 'service'">
            <el-card class="content-card">
              <template #header>
                <span class="card-title">联系客服</span>
              </template>
              <div class="service-content">
                <div class="service-item">
                  <div>
                    <h4>在线客服</h4>
                    <p>工作日 9:00 - 22:00</p>
                    <p>周末 10:00 - 20:00</p>
                  </div>
                </div>
                <el-divider />
                <div class="service-item">
                  <div>
                    <h4>客服电话</h4>
                    <p>400-888-0000</p>
                  </div>
                </div>
                <el-divider />
                <div class="service-item">
                  <div>
                    <h4>QQ群</h4>
                    <p>123456789（点击复制）</p>
                  </div>
                </div>
              </div>
            </el-card>
          </template>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import DingBuDaoHangLan from './components/Shou_Ye/Ding_Bu_Dao_Hang_Lan.vue'
import CeBianDaoHangLan from './components/Ge_Ren_Zhong_Xin/Ce_Bian_Dao_Hang_Lan.vue'
import { useUserStore } from './stores/user'
import { useAuthStore } from './stores/auth'
import { favoriteApi } from './api/favorites'
import { orderApi } from './api/order'
import { messageApi } from './api/message'
import { productApi } from './api/product'
import { pickImageUrl } from './utils/imageUrl'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const authStore = useAuthStore()

const currentMenu = ref('personal')

// 数据
const favorites = ref([])
const myOrders = ref([])
const sellerOrders = ref([])
const messages = ref([])
const myProducts = ref([])
const isLoadingData = ref(false)

const roleText = computed(() => {
  const role = userStore.userInfo?.jueSe
  if (role === 0) return '普通用户'
  if (role === 1) return '卖家'
  if (role === 2) return '管理员'
  return '--'
})

const productStatusText = (status) => {
  const map = { 0: '待审核', 1: '已上架', 2: '租出中', 3: '已下架' }
  return map[status] ?? '--'
}

// 订单状态
const orderStatusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '租赁中', type: 'primary' },
  2: { text: '已完成', type: 'success' },
  3: { text: '已取消', type: 'info' },
  4: { text: '纠纷中', type: 'danger' },
  5: { text: '验号中', type: 'warning' },
}

const orderStatusText = (status) => orderStatusMap[status]?.text || '未知'
const orderStatusType = (status) => orderStatusMap[status]?.type || 'info'

/** 消息未读判断：兼容 shiFouDuGuo / shiFouYiDu 两种字段名 */
const isUnread = (msg) => Number(msg.shiFouDuGuo ?? msg.shiFouYiDu ?? 1) === 0

const messageTypeText = (type) => {
  if (type === 0) return '系统消息'
  if (type === 1) return '订单消息'
  return '商品消息'
}

/** 时间格式化：后端返回 ISO 字符串，只保留到分钟 */
const formatTime = (value) => {
  if (!value) return ''
  const text = String(value).replace('T', ' ')
  return text.length > 16 ? text.slice(0, 16) : text
}

/** 兼容后端返回 数组 / {data:[...]} / Page{content:[...]} 三种形态 */
const toList = (res) => {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.content)) return res.content
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res?.data?.data)) return res.data.data
  return []
}

// 加载数据
const loadData = async () => {
  if (!userStore.isLogin) return

  isLoadingData.value = true
  try {
    // 刷新页面后 userInfo 会丢失，这里补一次
    if (!userStore.userInfo) {
      await userStore.restoreUserInfo()
    }

    switch (currentMenu.value) {
      case 'personal': {
        myOrders.value = toList(await orderApi.getMyOrders())
        break
      }
      case 'my-products': {
        myProducts.value = toList(await productApi.getMyProducts({ page: 1, limit: 100 }))
        break
      }
      case 'favorite': {
        favorites.value = toList(await favoriteApi.getFavorites({ page: 0, size: 100 }))
        break
      }
      case 'order-buyer': {
        myOrders.value = toList(await orderApi.getMyOrders())
        break
      }
      case 'order-seller': {
        const myId = userStore.userInfo?.id
        if (myId !== undefined && myId !== null) {
          sellerOrders.value = toList(await orderApi.getSellerOrders(myId))
        }
        break
      }
      case 'message': {
        messages.value = toList(await messageApi.getMessages())
        break
      }
      default:
        break
    }
  } catch (err) {
    console.error('加载数据失败:', err)
    ElMessage.error('加载失败：' + (err?.message || '网络错误'))
  } finally {
    isLoadingData.value = false
  }
}

// 收藏操作
const favImgErrors = ref({})
const handleFavImgError = (item) => {
  favImgErrors.value[item.id] = true
}

const removeFav = async (shangPinId) => {
  try {
    await favoriteApi.removeFavorite(shangPinId)
    ElMessage.success('已取消收藏')
    favorites.value = favorites.value.filter((f) => f.id !== shangPinId)
  } catch (err) {
    ElMessage.error(err.message || '取消收藏失败')
  }
}

// 订单操作
const payOrder = async (row) => {
  try {
    await orderApi.payOrder(row.dingDanHao)
    ElMessage.success('支付成功')
    loadData()
  } catch (err) {
    ElMessage.error(err.message || '支付失败')
  }
}

const cancelOrder = async (row) => {
  try {
    await orderApi.cancelOrder(row.dingDanHao)
    ElMessage.success('已取消')
    loadData()
  } catch (err) {
    ElMessage.error(err.message || '取消失败')
  }
}

const inspectOrder = async (row) => {
  try {
    await orderApi.inspectStart(row.dingDanHao)
    ElMessage.success('验号请求已发起')
    loadData()
  } catch (err) {
    ElMessage.error(err.message || '验号失败')
  }
}

const returnOrder = async (row) => {
  try {
    await orderApi.returnOrder(row.dingDanHao)
    ElMessage.success('已归还')
    loadData()
  } catch (err) {
    ElMessage.error(err.message || '归还失败')
  }
}

const confirmReceive = async (row) => {
  try {
    await orderApi.confirmOrder(row.dingDanHao)
    ElMessage.success('已确认收货')
    loadData()
  } catch (err) {
    ElMessage.error(err.message || '确认收货失败')
  }
}

// 消息操作
const markRead = async (msg) => {
  if (!isUnread(msg)) return
  try {
    await messageApi.markAsRead(msg.id)
    msg.shiFouDuGuo = 1
    msg.shiFouYiDu = 1
  } catch (err) {
    console.error('标记已读失败:', err)
  }
}

const markAllRead = async () => {
  try {
    await messageApi.markAllAsRead()
    messages.value.forEach((m) => {
      m.shiFouDuGuo = 1
      m.shiFouYiDu = 1
    })
    ElMessage.success('全部已读')
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  }
}

// 跳转
const goProduct = (id) => {
  router.push(`/product/${id}`)
}

const goShelf = () => {
  router.push('/shelf')
}

// 下架我的商品（软下架：状态置为已下架，不删除数据）
const xiajiaMyProduct = async (id) => {
  try {
    await ElMessageBox.confirm('确定要下架该商品吗？', '提示', { type: 'warning' })
    await productApi.xiajiaProduct(id)
    ElMessage.success('已下架')
    myProducts.value = myProducts.value.filter((p) => p.id !== id)
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error(err?.message || '下架失败')
    }
  }
}

// 菜单切换（同步更新 URL query 参数）
const handleMenuChange = (menu) => {
  currentMenu.value = menu
  router.replace({ query: { tab: menu } })
  loadData()
}

// 监听路由 query 参数变化（组件复用时响应 tab 切换）
watch(
  () => route.query.tab,
  (newTab) => {
    const tab = newTab || 'personal'
    if (currentMenu.value !== tab) {
      currentMenu.value = tab
      loadData()
    }
  },
)

// 登录成功后自动加载数据（弹窗登录场景）
watch(
  () => userStore.isLogin,
  (logged) => {
    if (logged) loadData()
  },
)

onMounted(async () => {
  const tab = route.query.tab || 'personal'
  currentMenu.value = tab
  if (userStore.isLogin && !userStore.userInfo) {
    await userStore.restoreUserInfo()
  }
  loadData()
})
</script>

<style scoped>
.personal-center {
  min-height: 100vh;
  background-color: #f5f7fa;
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

.main-content {
  display: flex;
  min-height: calc(100vh - 120px);
}

.content-area {
  flex: 1;
  padding: 120px 30px 30px 270px;
  min-height: calc(100vh - 150px);
}

.content-card {
  margin-bottom: 20px;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #333333;
  margin: 0;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 个人资料 */
.profile-info {
  padding: 16px 0;
}

.info-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-row label {
  width: 120px;
  font-weight: 500;
  color: #666;
}

.info-row span {
  color: #333;
}

.not-logged-in {
  text-align: center;
  color: #666;
  padding: 40px;
  font-size: 16px;
}

.not-logged-in p {
  margin: 0 0 20px;
}

/* 统计 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 12px 0;
}

.stat-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
}

.stat-num {
  font-size: 32px;
  font-weight: 700;
  color: #00b1fe;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}

/* 收藏 / 我的商品 */
.favorite-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.favorite-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.favorite-card:hover {
  border-color: #00b1fe;
  box-shadow: 0 2px 8px rgba(0, 177, 254, 0.12);
}

.fav-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  background: #f5f5f5;
  flex-shrink: 0;
}

.fav-img-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.placeholder-text {
  font-size: 12px;
  color: #bbb;
}

.fav-info {
  flex: 1;
  min-width: 0;
}

.fav-info h4 {
  margin: 0 0 6px;
  font-size: 15px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fav-price {
  margin: 0;
  font-size: 14px;
  color: #ff6b6b;
  font-weight: 600;
}

.fav-sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: #999;
}

/* 空状态 */
.empty-state {
  padding: 40px 0;
}

/* 消息列表 */
.message-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: #f0f0f0;
}

.message-item {
  position: relative;
  padding: 16px 20px;
  background: #fff;
  cursor: pointer;
  transition: background 0.15s;
}

.message-item:hover {
  background: #f8f9fa;
}

.message-item.unread {
  background: #f0f9ff;
}

.msg-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.msg-type {
  font-size: 12px;
  color: #00b1fe;
  background: #ecf5ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.msg-time {
  font-size: 12px;
  color: #999;
}

.msg-title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.msg-content {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.unread-dot {
  position: absolute;
  top: 16px;
  left: 8px;
  width: 8px;
  height: 8px;
  background: #f56c6c;
  border-radius: 50%;
}

/* 客服 */
.service-content {
  padding: 8px 0;
}

.service-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 8px 0;
}

.service-item h4 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #333;
}

.service-item p {
  margin: 2px 0;
  font-size: 14px;
  color: #666;
}

@media (max-width: 1024px) {
  .content-area {
    padding: 120px 20px 20px 210px;
  }
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .content-area {
    padding: 105px 16px 16px 200px;
  }
  .favorite-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .content-area {
    padding: 100px 12px 12px 160px;
  }
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  .stat-box {
    padding: 16px 8px;
  }
  .stat-num {
    font-size: 24px;
  }
  .favorite-card {
    flex-wrap: wrap;
  }
  .fav-img,
  .fav-img-placeholder {
    width: 60px;
    height: 60px;
  }
  .info-row {
    flex-direction: column;
    gap: 4px;
    padding: 8px 0;
  }
  .info-row label {
    width: auto;
    font-size: 13px;
  }
}
</style>
