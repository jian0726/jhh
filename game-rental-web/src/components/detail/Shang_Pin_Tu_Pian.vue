<template>
  <div class="product-image-panel">
    <div
      class="image-container"
      :class="{ clickable: !!resolvedUrl }"
    >
      <!-- 正常图片（支持点击预览） -->
      <el-image
        v-if="resolvedUrl && !imgError"
        :src="resolvedUrl"
        alt="商品图片"
        class="product-image"
        fit="contain"
        :preview-src-list="[resolvedUrl]"
        preview-teleported
        @error="imgError = true"
      />
      <!-- 加载失败占位 -->
      <div v-else-if="resolvedUrl && imgError" class="image-placeholder">
        <span class="placeholder-text">图片加载失败</span>
      </div>
      <!-- 无图片占位 -->
      <div v-else class="image-placeholder">
        <el-icon :size="48" color="#ccc"><Picture /></el-icon>
        <span class="placeholder-text">暂无图片</span>
      </div>

      <!-- 查看大图提示 -->
      <div v-if="resolvedUrl" class="preview-hint">
        <el-icon><ZoomIn /></el-icon>
        点击查看大图
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Picture } from '@element-plus/icons-vue'
import { resolveImageUrl } from '@/utils/request'

const props = defineProps({
  coverImageUrl: {
    type: String,
    default: ''
  }
})

const resolvedUrl = computed(() => resolveImageUrl(props.coverImageUrl))

const imgError = ref(false)

const openPreview = () => {
  // el-image 自带预览功能
}
</script>

<style scoped>
.product-image-panel {
  flex-shrink: 0;
  width: 400px;
}

.image-container {
  position: relative;
  width: 100%;
  height: 400px;
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.image-container.clickable {
  cursor: pointer;
}

.image-container.clickable:hover .product-image {
  transform: scale(1.03);
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: #fafafa;
  transition: transform 0.3s ease;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background-color: #fafafa;
}

.placeholder-text {
  color: #999;
  font-size: 14px;
}

.preview-hint {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border-radius: 16px;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.25s;
}

.image-container:hover .preview-hint {
  opacity: 1;
}

@media (max-width: 1024px) {
  .product-image-panel {
    width: 100%;
  }

  .image-container {
    height: 300px;
  }
}
</style>
