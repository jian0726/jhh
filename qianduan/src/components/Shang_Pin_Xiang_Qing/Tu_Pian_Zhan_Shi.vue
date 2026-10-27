<template>
  <div class="image-gallery">
    <div class="main-image">
      <img
        v-if="displayImage"
        :src="displayImage"
        alt="商品图片"
        class="gallery-img"
        @error="handleImageError"
      />
      <div v-else class="image-placeholder">暂无商品图片</div>
    </div>
  </div>
</template>

<script setup>
/**
 * Tu_Pian_Zhan_Shi.vue - 商品图片展示
 *
 * 图片来源于后端：由父组件把商品数据里的图片地址（coverImageUrl / tuPianUrl / images）
 * 传进来，这里统一解析后渲染。前端工程内不再内置业务图片。
 */
import { ref, watch, onMounted } from 'vue'
import { collectImageUrls } from '../../utils/imageUrl'

const props = defineProps({
  /** 图片来源：可以是地址数组，或商品数据对象 */
  images: {
    type: [Array, Object],
    default: () => [],
  },
})

const displayImage = ref('')

const resolveList = () => {
  if (Array.isArray(props.images)) {
    return collectImageUrls({ images: props.images })
  }
  return collectImageUrls(props.images)
}

const pick = () => {
  const list = resolveList()
  displayImage.value = list.length > 0 ? list[0] : ''
}

onMounted(pick)
watch(() => props.images, pick, { deep: true })

const handleImageError = () => {
  displayImage.value = ''
}
</script>

<style scoped>
.image-gallery {
  width: 460px;
  flex-shrink: 0;
  align-self: center;
}

.main-image {
  width: 800px;
  height: 520px;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #e5e6eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gallery-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  color: #9aa0a6;
  font-size: 14px;
  user-select: none;
}
</style>
