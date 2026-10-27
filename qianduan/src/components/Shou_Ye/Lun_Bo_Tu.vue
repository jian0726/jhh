<template>
  <div class="carousel-container">
    <el-carousel
      ref="carouselRef"
      :interval="interval"
      type="card"
      :height="height"
      :autoplay="autoplay"
      :indicator-position="false"
      trigger="click"
      :pause-on-hover="false"
      @change="handleCarouselChange"
    >
      <el-carousel-item
        v-for="(item, index) in items"
        :key="item.id || index"
      >
        <div class="carousel-item-content">
          <img
            v-if="resolveImageUrl(item.tuPianUrl || item.imageUrl)"
            :src="resolveImageUrl(item.tuPianUrl || item.imageUrl)"
            :alt="item.title || `轮播图 ${index + 1}`"
            class="carousel-image"
            loading="lazy"
            @error="handleImageError(index)"
            @click="handleCarouselClick(index)"
          />
          <h3 v-else class="carousel-title">{{ item.title || index + 1 }}</h3>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { carouselApi } from '../../api/carousel';
import { resolveImageUrl } from '../../utils/imageUrl';

// 定义 props，允许外部传入数据
// 后端接入时可传入：[{ id: 1, imageUrl: 'xxx', title: 'xxx', sortOrder: 1 }, ...]
const props = defineProps({
  // 轮播数据数组，支持外部传入，后端管理时可动态添加/删除
  carouselData: {
    type: Array,
    default: () => []
  },
  // 自动播放间隔（毫秒）
  interval: {
    type: Number,
    default: 4000
  },
  // 是否自动播放
  autoplay: {
    type: Boolean,
    default: true
  },
  // 轮播高度
  height: {
    type: String,
    default: '505px'
  }
});

// 从API获取的轮播数据
const apiCarouselData = ref([]);

// 计算最终轮播数据：优先使用外部传入数据，否则使用API数据
// 支持按 shunXu/sortOrder 字段排序，便于后端管理顺序
const items = computed(() => {
  const data = props.carouselData.length > 0 ? props.carouselData : apiCarouselData.value;
  return [...data].sort((a, b) => {
    const orderA = a.shunXu ?? a.sortOrder ?? a.id ?? 0;
    const orderB = b.shunXu ?? b.sortOrder ?? b.id ?? 0;
    return orderA - orderB;
  });
});

// 定义 emit 事件
const emit = defineEmits(['change']);

// 当前索引
const currentIndex = ref(0);
// 定时器引用
let timer = null;
// 轮播图组件引用
const carouselRef = ref(null);

// 处理轮播图切换事件
const handleCarouselChange = (index) => {
  currentIndex.value = index;
  emit('change', index);
};

// 处理轮播图点击事件
const handleCarouselClick = (index) => {
  currentIndex.value = index;
  emit('change', index);
};

// 切换到下一张
const next = () => {
  if (items.value.length === 0) return;
  currentIndex.value = (currentIndex.value + 1) % items.value.length;
};

// 图片加载错误处理
const handleImageError = (index) => {
  console.warn(`轮播图 ${index + 1} 加载失败`);
};

// 重置定时器
const resetTimer = () => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
  if (props.autoplay && items.value.length > 1) {
    timer = setInterval(next, props.interval);
  }
};

// 监听数据变化，自动调整轮播
watch(() => items.value.length, (newLength) => {
  // 当数据变化时，确保索引在有效范围内
  if (currentIndex.value >= newLength) {
    currentIndex.value = newLength > 0 ? 0 : -1;
  }
  // 重新设置定时器
  nextTick(() => {
    resetTimer();
  });
});

// 监听自动播放属性变化
watch(() => props.autoplay, (newVal) => {
  if (newVal) {
    resetTimer();
  } else {
    if (timer) {
      clearInterval(timer);
      timer = null;
    }
  }
});

// 监听间隔时间变化
watch(() => props.interval, () => {
  if (props.autoplay) {
    resetTimer();
  }
});

// 生命周期钩子：组件挂载后获取轮播数据并开始自动播放
onMounted(async () => {
  // 如果外部没有传入数据，则从API获取
  if (props.carouselData.length === 0) {
    try {
      const response = await carouselApi.getCarouselItems();
      const data = response.data || response;
      apiCarouselData.value = data;
    } catch (error) {
      console.error('获取轮播图数据失败:', error);
    }
  }

  if (props.autoplay && items.value.length > 1) {
    timer = setInterval(next, props.interval);
  }
});

// 生命周期钩子：组件卸载前清除定时器，防止内存泄漏
onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
});
</script>

<style scoped>
.carousel-container {
  position: relative;
  width: 100%;
}

.carousel-item-content {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  cursor: pointer;
}

/* 移除轮播图卡片的默认背景 */
:deep(.el-carousel__item) {
  background-color: transparent !important;
}

/* 移除轮播图容器的默认背景 */
:deep(.el-carousel) {
  background-color: transparent !important;
}

/* 移除卡片模式下左右两侧卡片的背景 */
:deep(.el-carousel__item--card) {
  background-color: transparent !important;
}

/* 移除卡片模式下左右两侧卡片的阴影效果 */
:deep(.el-carousel__item--card.is-active) {
  background-color: transparent !important;
}

/* 移除卡片模式下的悬停模糊效果 */
:deep(.el-carousel__item--card):hover {
  filter: none !important;
}

/* 移除所有轮播图卡片的背景 */
:deep(.el-carousel__mask) {
  background-color: transparent !important;
}

/* 彻底移除底部指示器容器 */
:deep(.el-carousel__indicators) {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  pointer-events: none !important;
  height: 0 !important;
  overflow: hidden !important;
}

/* 彻底移除底部指示器 */
:deep(.el-carousel__indicator) {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  pointer-events: none !important;
}

</style>
