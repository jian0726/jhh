<template>
  <div class="admin-page">
    <AdminDingBuLan />

    <div class="main-content">
      <CeBianDaoHangLan :default-active="currentMenu" @menu-change="handleMenuChange" />

      <div class="content-area">
        <!-- 兜底：路由守卫已拦截，正常情况不会走到这里 -->
        <el-card v-if="!isAdmin" class="content-card">
          <template #header>
            <span class="card-title">管理后台</span>
          </template>
          <div class="not-allowed">
            <p v-if="!userStore.isLogin">您还没有登录，请先使用管理员账号登录。</p>
            <p v-else>当前账号不是管理员，无权访问管理后台。</p>
            <el-button v-if="!userStore.isLogin" type="primary" @click="router.push('/login')">
              去登录
            </el-button>
          </div>
        </el-card>

        <template v-else>
          <!-- ========== 数据看板 ========== -->
          <template v-if="currentMenu === 'dashboard'">
            <el-card class="content-card" v-loading="isLoadingData">
              <template #header>
                <span class="card-title">数据看板</span>
              </template>
              <div class="stats-grid">
                <div class="stat-box">
                  <span class="stat-num">{{ dashboard.totalUsers ?? 0 }}</span>
                  <span class="stat-label">用户总数</span>
                </div>
                <div class="stat-box">
                  <span class="stat-num">{{ dashboard.totalProducts ?? 0 }}</span>
                  <span class="stat-label">商品总数</span>
                </div>
                <div class="stat-box">
                  <span class="stat-num">{{ dashboard.totalOrders ?? 0 }}</span>
                  <span class="stat-label">订单总数</span>
                </div>
                <div class="stat-box">
                  <span class="stat-num">{{ dashboard.todayOrders ?? 0 }}</span>
                  <span class="stat-label">今日订单</span>
                </div>
                <div class="stat-box">
                  <span class="stat-num warn-num">{{ dashboard.pendingProducts ?? 0 }}</span>
                  <span class="stat-label">待审核商品</span>
                </div>
                <div class="stat-box">
                  <span class="stat-num warn-num">{{ dashboard.pendingDisputes ?? 0 }}</span>
                  <span class="stat-label">待处理纠纷</span>
                </div>
              </div>
            </el-card>

            <el-card class="content-card" v-loading="isLoadingData">
              <template #header>
                <span class="card-title">最近订单（最新 10 条）</span>
              </template>
              <el-table v-if="recentOrders.length" :data="recentOrders" stripe style="width: 100%">
                <el-table-column prop="dingDanHao" label="订单号" min-width="200" />
                <el-table-column label="金额" width="120">
                  <template #default="{ row }">¥{{ row.zongJinE ?? '0.00' }}</template>
                </el-table-column>
                <el-table-column label="状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="orderStatusType(row.zhuangTai)" size="small">
                      {{ orderStatusText(row.zhuangTai) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="创建时间" width="180">
                  <template #default="{ row }">{{ formatTime(row.chuangJianShiJian) }}</template>
                </el-table-column>
              </el-table>
              <el-empty v-else description="暂无订单" />
            </el-card>
          </template>

          <!-- ========== 用户管理 ========== -->
          <template v-if="currentMenu === 'users'">
            <el-card class="content-card" v-loading="isLoadingData">
              <template #header>
                <span class="card-title">用户管理</span>
              </template>
              <el-table :data="users" stripe style="width: 100%">
                <el-table-column prop="id" label="ID" width="70" />
                <el-table-column prop="yongHuMing" label="用户名" min-width="120" />
                <el-table-column prop="shouJiHao" label="手机号" width="130" />
                <el-table-column label="角色" width="100">
                  <template #default="{ row }">
                    <el-tag :type="roleTagType(row.jueSe)" size="small">{{ roleText(row.jueSe) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="90">
                  <template #default="{ row }">
                    <el-tag :type="row.zhuangTai === 1 ? 'danger' : 'success'" size="small">
                      {{ row.zhuangTai === 1 ? '已封禁' : '正常' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="实名" width="80">
                  <template #default="{ row }">{{ row.shiFouShiMing === 1 ? '是' : '否' }}</template>
                </el-table-column>
                <el-table-column label="注册时间" width="170">
                  <template #default="{ row }">{{ formatTime(row.chuangJianShiJian) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="220" fixed="right">
                  <template #default="{ row }">
                    <el-button
                      v-if="row.zhuangTai !== 1"
                      size="small"
                      type="danger"
                      @click="banUser(row)"
                      >封禁</el-button
                    >
                    <el-button v-else size="small" type="success" @click="unbanUser(row)">解封</el-button>
                    <el-button size="small" @click="openRoleDialog(row)">改角色</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </template>

          <!-- ========== 商品管理 ========== -->
          <template v-if="currentMenu === 'products'">
            <el-card class="content-card" v-loading="isLoadingData">
              <template #header>
                <div class="card-header-row">
                  <span class="card-title">商品管理</span>
                  <div class="filter-row">
                    <el-input
                      v-model="productKeyword"
                      placeholder="按标题搜索"
                      clearable
                      style="width: 200px"
                      @keyup.enter="loadProducts"
                      @clear="loadProducts"
                    />
                    <el-button type="primary" plain @click="loadProducts">搜索</el-button>
                  </div>
                </div>
              </template>

              <el-radio-group v-model="productStatus" class="status-tabs" @change="loadProducts">
                <el-radio-button :value="-1">全部</el-radio-button>
                <el-radio-button :value="0">待审核</el-radio-button>
                <el-radio-button :value="1">已上架</el-radio-button>
                <el-radio-button :value="2">租出中</el-radio-button>
                <el-radio-button :value="3">已下架</el-radio-button>
              </el-radio-group>

              <div v-if="!isLoadingData && products.length === 0" class="empty-state">
                <el-empty description="没有符合条件的商品" />
              </div>
              <el-table v-else-if="!isLoadingData" :data="products" stripe style="width: 100%">
                <el-table-column prop="id" label="ID" width="70" />
                <el-table-column label="封面" width="110">
                  <template #default="{ row }">
                    <el-image
                      v-if="row.tuPianUrl || row.coverImageUrl"
                      :src="resolveImageUrl(row.coverImageUrl || row.tuPianUrl)"
                      :preview-src-list="[resolveImageUrl(row.coverImageUrl || row.tuPianUrl)]"
                      preview-teleported
                      fit="cover"
                      class="product-thumb"
                    />
                    <span v-else class="placeholder-text">无图</span>
                  </template>
                </el-table-column>
                <el-table-column label="标题" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">{{ row.biaoTi || row.mingCheng || '未命名商品' }}</template>
                </el-table-column>
                <el-table-column prop="maiJiaId" label="卖家ID" width="90" />
                <el-table-column label="日租金" width="110">
                  <template #default="{ row }">¥{{ row.riZuJiaGe ?? row.preRent ?? '0.00' }}/天</template>
                </el-table-column>
                <el-table-column prop="zuJieCiShu" label="租出次数" width="100" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="productStatusType(row.zhuangTai)" size="small">
                      {{ productStatusText(row.zhuangTai) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="260" fixed="right">
                  <template #default="{ row }">
                    <el-button
                      v-if="row.zhuangTai === 0"
                      size="small"
                      type="success"
                      @click="auditProduct(row, 1)"
                      >通过</el-button
                    >
                    <el-button
                      v-if="row.zhuangTai === 0"
                      size="small"
                      type="warning"
                      @click="auditProduct(row, 0)"
                      >拒绝</el-button
                    >
                    <el-button
                      v-if="row.zhuangTai === 1 || row.zhuangTai === 2"
                      size="small"
                      type="danger"
                      @click="forceOffline(row)"
                      >强制下架</el-button
                    >
                    <el-button
                      v-if="row.zhuangTai === 3"
                      size="small"
                      type="success"
                      @click="auditProduct(row, 1)"
                      >恢复上架</el-button
                    >
                    <el-button size="small" @click="goProduct(row.id)">查看</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </template>

          <!-- ========== 订单管理 ========== -->
          <template v-if="currentMenu === 'orders'">
            <el-card class="content-card" v-loading="isLoadingData">
              <template #header>
                <span class="card-title">全部订单</span>
              </template>
              <div v-if="!isLoadingData && orders.length === 0" class="empty-state">
                <el-empty description="暂无订单" />
              </div>
              <el-table v-else-if="!isLoadingData" :data="orders" stripe style="width: 100%">
                <el-table-column prop="dingDanHao" label="订单号" min-width="190" />
                <el-table-column prop="shangPinId" label="商品ID" width="90" />
                <el-table-column prop="maiJiaId" label="买家ID" width="90" />
                <el-table-column prop="maiJiaId2" label="卖家ID" width="90" />
                <el-table-column label="金额" width="110">
                  <template #default="{ row }">¥{{ row.zongJinE ?? '0.00' }}</template>
                </el-table-column>
                <el-table-column label="租期" width="90">
                  <template #default="{ row }">{{ row.zuJieTianShu || '-' }}天</template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="orderStatusType(row.zhuangTai)" size="small">
                      {{ orderStatusText(row.zhuangTai) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="创建时间" width="170">
                  <template #default="{ row }">{{ formatTime(row.chuangJianShiJian) }}</template>
                </el-table-column>
              </el-table>
            </el-card>
          </template>

          <!-- ========== 纠纷仲裁 ========== -->
          <template v-if="currentMenu === 'disputes'">
            <el-card class="content-card" v-loading="isLoadingData">
              <template #header>
                <span class="card-title">纠纷仲裁</span>
              </template>
              <div v-if="!isLoadingData && disputes.length === 0" class="empty-state">
                <el-empty description="暂无待处理纠纷" />
              </div>
              <el-table v-else-if="!isLoadingData" :data="disputes" stripe style="width: 100%">
                <el-table-column prop="dingDanHao" label="订单号" min-width="190" />
                <el-table-column prop="shangPinId" label="商品ID" width="90" />
                <el-table-column prop="maiJiaId" label="买家ID" width="90" />
                <el-table-column prop="maiJiaId2" label="卖家ID" width="90" />
                <el-table-column label="金额" width="110">
                  <template #default="{ row }">¥{{ row.zongJinE ?? '0.00' }}</template>
                </el-table-column>
                <el-table-column label="发起时间" width="170">
                  <template #default="{ row }">{{ formatTime(row.chuangJianShiJian) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="130" fixed="right">
                  <template #default="{ row }">
                    <el-button size="small" type="primary" @click="judgeDispute(row)">仲裁结案</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </template>

          <!-- ========== 风控日志 ========== -->
          <template v-if="currentMenu === 'risk-logs'">
            <el-card class="content-card" v-loading="isLoadingData">
              <template #header>
                <div class="card-header-row">
                  <span class="card-title">风控日志</span>
                  <div class="filter-row">
                    <el-input
                      v-model="riskUserId"
                      placeholder="用户ID"
                      clearable
                      style="width: 110px"
                      @change="loadData"
                    />
                    <el-input
                      v-model="riskType"
                      placeholder="风险类型"
                      clearable
                      style="width: 140px"
                      @change="loadData"
                    />
                  </div>
                </div>
              </template>
              <div v-if="!isLoadingData && riskLogs.length === 0" class="empty-state">
                <el-empty description="暂无风险事件" />
              </div>
              <el-table v-else-if="!isLoadingData" :data="riskLogs" stripe style="width: 100%">
                <el-table-column prop="id" label="ID" width="70" />
                <el-table-column prop="yongHuId" label="用户ID" width="90" />
                <el-table-column prop="fengXianLeiXing" label="风险类型" width="140" />
                <el-table-column prop="miaoShu" label="描述" min-width="200" show-overflow-tooltip />
                <el-table-column prop="ip" label="IP" width="140" />
                <el-table-column label="时间" width="170">
                  <template #default="{ row }">{{ formatTime(row.chuangJianShiJian) }}</template>
                </el-table-column>
              </el-table>
            </el-card>
          </template>

          <!-- ========== 操作日志 ========== -->
          <template v-if="currentMenu === 'operate-logs'">
            <el-card class="content-card" v-loading="isLoadingData">
              <template #header>
                <span class="card-title">管理员操作日志（最多 200 条）</span>
              </template>
              <div v-if="!isLoadingData && operateLogs.length === 0" class="empty-state">
                <el-empty description="暂无操作记录" />
              </div>
              <el-table v-else-if="!isLoadingData" :data="operateLogs" stripe style="width: 100%">
                <el-table-column prop="id" label="ID" width="70" />
                <el-table-column prop="guanLiYuanId" label="管理员ID" width="100" />
                <el-table-column prop="operation" label="操作" width="170" />
                <el-table-column prop="targetType" label="目标类型" width="110" />
                <el-table-column prop="targetId" label="目标ID" width="90" />
                <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
                <el-table-column label="时间" width="170">
                  <template #default="{ row }">{{ formatTime(row.time) }}</template>
                </el-table-column>
              </el-table>
            </el-card>
          </template>

          <!-- ========== 通知发布 ========== -->
          <template v-if="currentMenu === 'notice'">
            <el-card class="content-card">
              <template #header>
                <span class="card-title">发布全站通知</span>
              </template>
              <el-form label-width="80px" class="notice-form">
                <el-form-item label="标题">
                  <el-input v-model="noticeTitle" placeholder="通知标题" maxlength="50" show-word-limit />
                </el-form-item>
                <el-form-item label="内容">
                  <el-input
                    v-model="noticeContent"
                    type="textarea"
                    :rows="5"
                    placeholder="通知内容，将发送给全部用户"
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="isPublishing" @click="publishNotice">发布通知</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </template>

          <!-- ========== 轮播图管理 ========== -->
          <template v-if="currentMenu === 'lunbotu'">
            <el-card class="content-card" v-loading="isLoadingData">
              <template #header>
                <div class="card-header-row">
                  <span class="card-title">轮播图管理</span>
                  <el-button type="primary" @click="openLunbotuDialog()">新增轮播图</el-button>
                </div>
              </template>
              <div v-if="!isLoadingData && lunbotuList.length === 0" class="empty-state">
                <el-empty description="暂无轮播图，点右上角新增" />
              </div>
              <el-table v-else-if="!isLoadingData" :data="lunbotuList" stripe style="width: 100%">
                <el-table-column prop="id" label="ID" width="70" />
                <el-table-column label="图片" width="180">
                  <template #default="{ row }">
                    <el-image
                      v-if="row.tuPianUrl"
                      :src="resolveImageUrl(row.tuPianUrl)"
                      :preview-src-list="[resolveImageUrl(row.tuPianUrl)]"
                      preview-teleported
                      fit="cover"
                      class="lunbotu-thumb"
                    />
                    <span v-else class="placeholder-text">无图片</span>
                  </template>
                </el-table-column>
                <el-table-column prop="tieTle" label="标题" min-width="140" show-overflow-tooltip />
                <el-table-column prop="lianJieUrl" label="跳转链接" min-width="160" show-overflow-tooltip />
                <el-table-column prop="shunXu" label="排序" width="80" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.zhuangTai === 1 ? 'success' : 'info'" size="small">
                      {{ row.zhuangTai === 1 ? '启用中' : '已停用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="240" fixed="right">
                  <template #default="{ row }">
                    <el-button size="small" @click="openLunbotuDialog(row)">编辑</el-button>
                    <el-button v-if="row.zhuangTai === 1" size="small" type="warning" @click="toggleLunbotu(row, 0)"
                      >停用</el-button
                    >
                    <el-button v-else size="small" type="success" @click="toggleLunbotu(row, 1)">启用</el-button>
                    <el-button size="small" type="danger" @click="deleteLunbotu(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </template>
        </template>
      </div>
    </div>

    <!-- 修改角色弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="修改用户角色" width="360px">
      <p class="role-dialog-user">
        用户：<b>{{ roleTarget?.yongHuMing }}</b>（ID: {{ roleTarget?.id }}）
      </p>
      <el-radio-group v-model="roleDialogValue">
        <el-radio :value="0">普通用户</el-radio>
        <el-radio :value="1">卖家</el-radio>
        <el-radio :value="2">管理员</el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRoleChange">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新增 / 编辑轮播图弹窗 -->
    <el-dialog v-model="lunbotuDialogVisible" :title="lunbotuForm.id ? '编辑轮播图' : '新增轮播图'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="图片">
          <el-upload
            :show-file-list="false"
            accept="image/*"
            :http-request="handleLunbotuUpload"
          >
            <div v-if="lunbotuForm.tuPianUrl" class="lunbotu-upload-preview">
              <img :src="resolveImageUrl(lunbotuForm.tuPianUrl)" alt="轮播图预览" />
              <span class="preview-tip">点击更换图片</span>
            </div>
            <el-button v-else type="primary" plain>选择并上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="lunbotuForm.tieTle" placeholder="轮播图标题（可选）" maxlength="50" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="lunbotuForm.lianJieUrl" placeholder="点击轮播图跳转的地址（可选）" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="lunbotuForm.shunXu" :min="0" :max="999" />
          <span class="form-tip">数字越小越靠前</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="lunbotuForm.zhuangTai"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="lunbotuDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="isSavingLunbotu" @click="saveLunbotu">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminDingBuLan from './components/Guan_Li_Hou_Tai/Ding_Bu_Lan.vue'
import CeBianDaoHangLan from './components/Guan_Li_Hou_Tai/Ce_Bian_Dao_Hang_Lan.vue'
import { useUserStore } from './stores/user'
import { adminApi } from './api/admin'
import { resolveImageUrl } from './utils/imageUrl'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentMenu = ref(route.query.tab || 'dashboard')
const isLoadingData = ref(false)

// 管理员判定：jueSe === 2
const isAdmin = computed(() => userStore.isLogin && userStore.userInfo?.jueSe === 2)

// 各菜单数据
const dashboard = ref({})
const users = ref([])
const products = ref([])
const productStatus = ref(-1) // -1 全部 / 0 待审核 / 1 已上架 / 2 租出中 / 3 已下架
const productKeyword = ref('')
const orders = ref([])
const disputes = ref([])
const riskLogs = ref([])
const operateLogs = ref([])
const riskUserId = ref('')
const riskType = ref('')
const noticeTitle = ref('')
const noticeContent = ref('')
const isPublishing = ref(false)

// 角色弹窗
const roleDialogVisible = ref(false)
const roleTarget = ref(null)
const roleDialogValue = ref(0)

// 轮播图管理
const lunbotuList = ref([])
const lunbotuDialogVisible = ref(false)
const isSavingLunbotu = ref(false)
const lunbotuForm = ref({
  id: null,
  tuPianUrl: '',
  tieTle: '',
  lianJieUrl: '',
  shunXu: 0,
  zhuangTai: 1,
})

// 通用文案（与个人中心保持一致）
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

const roleText = (role) => ({ 0: '普通用户', 1: '卖家', 2: '管理员' })[role] ?? '未知'
const roleTagType = (role) => ({ 0: 'info', 1: 'primary', 2: 'danger' })[role] ?? 'info'

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

const recentOrders = computed(() => dashboard.value.recentOrders || [])

// 按菜单加载对应数据
const loadData = async () => {
  if (!isAdmin.value) return
  isLoadingData.value = true
  try {
    switch (currentMenu.value) {
      case 'dashboard': {
        dashboard.value = (await adminApi.getDashboard()) || {}
        break
      }
      case 'users': {
        users.value = toList(await adminApi.getUsers())
        break
      }
      case 'products': {
        await loadProducts()
        break
      }
      case 'orders': {
        orders.value = toList(await adminApi.getOrders())
        break
      }
      case 'disputes': {
        disputes.value = toList(await adminApi.getDisputes())
        break
      }
      case 'risk-logs': {
        const params = {}
        if (riskUserId.value) params.userId = riskUserId.value
        if (riskType.value) params.riskType = riskType.value
        riskLogs.value = toList(await adminApi.getRiskLogs(params))
        break
      }
      case 'operate-logs': {
        operateLogs.value = toList(await adminApi.getOperateLogs())
        break
      }
      case 'lunbotu': {
        lunbotuList.value = toList(await adminApi.getAllLunBoTu())
        break
      }
      default:
        break
    }
  } catch (err) {
    console.error('管理后台加载数据失败:', err)
    ElMessage.error('加载失败：' + (err?.message || '网络错误'))
  } finally {
    isLoadingData.value = false
  }
}

// ===== 用户操作 =====
const banUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确定封禁用户「${row.yongHuMing}」吗？`, '提示', { type: 'warning' })
    await adminApi.banUser(row.id, 1)
    ElMessage.success('已封禁')
    loadData()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') ElMessage.error(err?.message || '封禁失败')
  }
}

const unbanUser = async (row) => {
  try {
    await adminApi.banUser(row.id, 0)
    ElMessage.success('已解封')
    loadData()
  } catch (err) {
    ElMessage.error(err?.message || '解封失败')
  }
}

const openRoleDialog = (row) => {
  roleTarget.value = row
  roleDialogValue.value = row.jueSe ?? 0
  roleDialogVisible.value = true
}

const confirmRoleChange = async () => {
  try {
    await adminApi.updateRole(roleTarget.value.id, roleDialogValue.value)
    ElMessage.success('角色已更新')
    roleDialogVisible.value = false
    loadData()
  } catch (err) {
    ElMessage.error(err?.message || '修改角色失败')
  }
}

// ===== 商品管理 =====
/** 按状态 / 关键词加载商品（走后端 /api/admin/products/all） */
const loadProducts = async () => {
  isLoadingData.value = true
  try {
    const params = {}
    if (productStatus.value !== -1) params.zhuangTai = productStatus.value
    if (productKeyword.value.trim()) params.keyword = productKeyword.value.trim()
    products.value = toList(await adminApi.getAllProducts(params))
  } catch (err) {
    ElMessage.error(err?.message || '加载商品失败')
  } finally {
    isLoadingData.value = false
  }
}

/** 商品状态文案 / 标签色（与个人中心保持一致） */
const productStatusText = (status) => ({ 0: '待审核', 1: '已上架', 2: '租出中', 3: '已下架' })[status] ?? '未知'
const productStatusType = (status) =>
  ({ 0: 'warning', 1: 'success', 2: 'primary', 3: 'info' })[status] ?? 'info'

const forceOffline = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定强制下架商品「${row.biaoTi || row.mingCheng || row.id}」吗？下架后买家将无法看到该商品。`,
      '强制下架',
      { type: 'warning' },
    )
    await adminApi.offlineProduct(row.id)
    ElMessage.success('已强制下架')
    loadProducts()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') ElMessage.error(err?.message || '下架失败')
  }
}

// ===== 商品审核 =====
const auditProduct = async (row, result) => {
  const action = result === 1 ? (row.zhuangTai === 3 ? '恢复上架' : '通过') : '拒绝'
  const title = row.biaoTi || row.mingCheng || row.id
  try {
    await ElMessageBox.confirm(`确定${action}商品「${title}」吗？`, '提示', {
      type: result === 1 ? 'success' : 'warning',
    })
    await adminApi.auditProduct(row.id, result)
    ElMessage.success(`已${action}`)
    loadProducts()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') ElMessage.error(err?.message || `审核失败`)
  }
}

const goProduct = (id) => {
  router.push(`/product/${id}`)
}

// ===== 纠纷仲裁 =====
const judgeDispute = async (row) => {
  try {
    await ElMessageBox.confirm(
      '仲裁后订单将置为「已完成」，商品重新上架。确定结案吗？',
      '纠纷仲裁',
      { type: 'warning' },
    )
    await adminApi.judgeDispute(row.id)
    ElMessage.success('已结案')
    loadData()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') ElMessage.error(err?.message || '仲裁失败')
  }
}

// ===== 通知发布 =====
const publishNotice = async () => {
  if (!noticeTitle.value.trim() || !noticeContent.value.trim()) {
    ElMessage.warning('标题和内容不能为空')
    return
  }
  isPublishing.value = true
  try {
    await adminApi.publishNotice(noticeTitle.value.trim(), noticeContent.value.trim())
    ElMessage.success('通知已发布给全部用户')
    noticeTitle.value = ''
    noticeContent.value = ''
  } catch (err) {
    ElMessage.error(err?.message || '发布失败')
  } finally {
    isPublishing.value = false
  }
}

// ===== 轮播图管理 =====
const openLunbotuDialog = (row) => {
  lunbotuForm.value = row
    ? {
        id: row.id,
        tuPianUrl: row.tuPianUrl || '',
        tieTle: row.tieTle || '',
        lianJieUrl: row.lianJieUrl || '',
        shunXu: row.shunXu ?? 0,
        zhuangTai: row.zhuangTai ?? 1,
      }
    : { id: null, tuPianUrl: '', tieTle: '', lianJieUrl: '', shunXu: 0, zhuangTai: 1 }
  lunbotuDialogVisible.value = true
}

/** el-upload 自定义上传：先传图拿 URL，再回填表单 */
const handleLunbotuUpload = async ({ file }) => {
  try {
    const res = await adminApi.uploadImage(file)
    const url = res?.tuPianUrl || res?.url || res?.coverImageUrl
    if (!url) throw new Error('上传接口未返回图片地址')
    lunbotuForm.value.tuPianUrl = url
    ElMessage.success('图片上传成功')
  } catch (err) {
    ElMessage.error(err?.message || '图片上传失败')
  }
}

const saveLunbotu = async () => {
  if (!lunbotuForm.value.tuPianUrl) {
    ElMessage.warning('请先上传轮播图图片')
    return
  }
  isSavingLunbotu.value = true
  try {
    const payload = {
      tuPianUrl: lunbotuForm.value.tuPianUrl,
      tieTle: lunbotuForm.value.tieTle.trim(),
      lianJieUrl: lunbotuForm.value.lianJieUrl.trim(),
      shunXu: lunbotuForm.value.shunXu,
      zhuangTai: lunbotuForm.value.zhuangTai,
    }
    if (lunbotuForm.value.id) {
      await adminApi.updateLunBoTu(lunbotuForm.value.id, payload)
      ElMessage.success('轮播图已更新')
    } else {
      await adminApi.createLunBoTu(payload)
      ElMessage.success('轮播图已新增，刷新首页即可看到')
    }
    lunbotuDialogVisible.value = false
    loadData()
  } catch (err) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    isSavingLunbotu.value = false
  }
}

const toggleLunbotu = async (row, status) => {
  try {
    await adminApi.updateLunBoTu(row.id, {
      tuPianUrl: row.tuPianUrl,
      tieTle: row.tieTle || '',
      lianJieUrl: row.lianJieUrl || '',
      shunXu: row.shunXu ?? 0,
      zhuangTai: status,
    })
    ElMessage.success(status === 1 ? '已启用' : '已停用')
    loadData()
  } catch (err) {
    ElMessage.error(err?.message || '操作失败')
  }
}

const deleteLunbotu = async (row) => {
  try {
    await ElMessageBox.confirm('删除后首页将不再显示该轮播图，磁盘图片也会被清理。确定删除吗？', '删除轮播图', {
      type: 'warning',
      confirmButtonText: '删除',
    })
    await adminApi.deleteLunBoTu(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') ElMessage.error(err?.message || '删除失败')
  }
}

// 菜单切换（同步 URL query，与个人中心同一套路由习惯）
const handleMenuChange = (menu) => {
  currentMenu.value = menu
  router.replace({ query: { tab: menu } })
  loadData()
}

watch(
  () => route.query.tab,
  (newTab) => {
    const tab = newTab || 'dashboard'
    if (currentMenu.value !== tab) {
      currentMenu.value = tab
      loadData()
    }
  },
)

// 登录/用户信息就绪后自动加载
watch(isAdmin, (val) => {
  if (val) loadData()
})

onMounted(async () => {
  if (userStore.isLogin && !userStore.userInfo) {
    await userStore.restoreUserInfo()
  }
  loadData()
})
</script>

<style scoped>
/* 布局与配色对齐个人中心（Ge_Ren_Zhong_Xin.vue） */
.admin-page {
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

.filter-row {
  display: flex;
  gap: 10px;
}

.status-tabs {
  margin-bottom: 16px;
}

.product-thumb {
  width: 84px;
  height: 56px;
  border-radius: 6px;
  background: #f5f5f5;
}

.not-allowed {
  text-align: center;
  color: #666;
  padding: 40px;
  font-size: 16px;
}

.not-allowed p {
  margin: 0 0 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px;
  padding: 12px 0;
}

.stat-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 12px;
  background: #f8f9fa;
  border-radius: 12px;
}

.stat-num {
  font-size: 32px;
  font-weight: 700;
  color: #00b1fe;
}

.warn-num {
  color: #ff9f43;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}

.empty-state {
  padding: 40px 0;
}

/* 轮播图缩略图与上传预览 */
.lunbotu-thumb {
  width: 150px;
  height: 56px;
  border-radius: 6px;
  background: #f5f5f5;
}

.lunbotu-upload-preview {
  position: relative;
  width: 300px;
  height: 112px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.lunbotu-upload-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.preview-tip {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 3px 0;
  text-align: center;
  font-size: 12px;
  color: #fff;
  background: rgba(0, 0, 0, 0.45);
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #999;
}

.placeholder-text {
  font-size: 12px;
  color: #bbb;
}

.notice-form {
  max-width: 640px;
  padding-top: 12px;
}

.role-dialog-user {
  margin: 0 0 14px;
  color: #333;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1024px) {
  .content-area {
    padding: 120px 20px 20px 210px;
  }
}

@media (max-width: 768px) {
  .content-area {
    padding: 105px 16px 16px 200px;
  }
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>
