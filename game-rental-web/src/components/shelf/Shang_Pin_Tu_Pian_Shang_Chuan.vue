<template>
  <!--
    商品图片上传组件
    用于卖家上传商品封面图片

    功能说明：
    - 支持点击上传区域选择图片文件
    - 图片预览：上传后显示缩略图
    - 删除功能：提供删除按钮移除已上传图片
    - 文件验证：检查文件类型和大小
    - 温馨提示：显示上传要求和注意事项
  -->
  <div class="image-uploader">
    <!-- 隐藏的文件输入框，通过ref触发点击 -->
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      class="file-input"
      @change="handleFileSelect"
    />
    
    <!-- 上传区域：点击触发文件选择 -->
    <div 
      class="image-upload-box"
      @click="triggerFileInput"
    >
      <template v-if="selectedImage">
        <!-- 已上传图片：显示预览和删除按钮 -->
        <img :src="selectedImage" alt="预览图片" class="preview-image" />
        <el-button 
          class="remove-btn" 
          icon="el-icon-delete" 
          circle 
          @click.stop="removeImage"
        />
      </template>
      <template v-else>
        <!-- 未上传图片：显示上传提示 -->
        <div class="upload-hint">
          <el-icon class="upload-icon" size="48"><Upload /></el-icon>
          <span class="upload-text">点击上传图片</span>
          <span class="upload-subtext">支持 JPG、PNG 格式，最大 10MB</span>
        </div>
      </template>
    </div>

    <!-- 温馨提示：显示上传要求和注意事项 -->
    <el-alert 
      title="温馨提示" 
      type="warning" 
      :closable="false"
      class="tips-alert"
    >
      <p>为了避免打手多用东西纠缠不清一定要放仓库和K/D界面，资产界面图片，藏品界面图片，收藏室图片，如有卡邮件，卡交易行一定要上传截图（如无截图则视为没有卡邮件，卡交易行）藏品界面如无截图视为没有任何物品卡邮件，卡交易行，藏品如果这里放不下可以在出租后群内补充。</p>
    </el-alert>
  </div>
</template>

<script setup>
/**
 * Shang_Pin_Tu_Pian_Shang_Chuan.vue - 商品图片上传组件
 *
 * 该组件用于卖家上传商品封面图片，包含：
 * - 文件选择：通过隐藏的input元素触发系统文件选择
 * - 图片预览：使用FileReader读取图片并显示预览
 * - 文件验证：检查文件类型（仅图片）和大小（最大10MB）
 * - 删除功能：移除已上传的图片并清空预览
 *
 * 交互功能：
 * - 图片变化时触发 image-change 事件
 * - 传递图片URL和File对象给父组件
 */
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { productApi } from '../../api/product'

/**
 * 文件输入框的DOM引用
 * 用于程序化触发文件选择
 */
const fileInput = ref(null)

/**
 * 已选择图片的预览URL
 * 使用base64格式显示预览
 */
const selectedImage = ref(null)

/**
 * 已选择的文件对象
 * 用于上传到服务器
 */
const selectedFile = ref(null)
const serverUrl = ref(null)
const uploading = ref(false)

/**
 * 定义组件事件
 *
 * @event image-change - 图片变化时触发
 *                       传递 { image: URL或null, file: File对象或null }
 */
const emit = defineEmits(['image-change'])

/**
 * 触发文件输入框点击事件
 * 通过ref调用原生click方法
 */
const triggerFileInput = () => {
  if (fileInput.value) {
    fileInput.value.click()
  }
}

/**
 * 处理文件选择事件
 * 验证文件类型和大小，读取图片并触发预览
 *
 * @param {Event} event - 文件选择事件对象
 */
const handleFileSelect = async (event) => {
  const file = event.target.files[0]
  if (file) {
    // 验证文件类型：仅允许图片文件
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件')
      event.target.value = ''
      return
    }

    // 验证文件大小：最大10MB
    if (file.size > 10 * 1024 * 1024) {
      ElMessage.error('图片大小不能超过10MB')
      event.target.value = ''
      return
    }

    selectedFile.value = file
    const reader = new FileReader()
    reader.onload = (e) => {
      selectedImage.value = e.target.result
      emit('image-change', { image: selectedImage.value, file: selectedFile.value, url: serverUrl.value, uploading: uploading.value })
    }
    reader.readAsDataURL(file)

    uploading.value = true
    emit('image-change', { image: selectedImage.value, file, url: null, uploading: true })
    try {
      const res = await productApi.uploadImage(file)
      if (!res || !res.coverImageUrl) throw new Error('上传失败')
      serverUrl.value = res.coverImageUrl
      emit('image-change', { image: selectedImage.value, file, url: serverUrl.value, uploading: false })
    } catch (err) {
      ElMessage.error('图片上传失败，请重新上传')
      removeImage()
    } finally {
      uploading.value = false
    }
  }
}

/**
 * 移除已选择的图片
 * 清空预览和文件对象，通知父组件
 */
const removeImage = () => {
  selectedImage.value = null
  selectedFile.value = null
  serverUrl.value = null
  uploading.value = false
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  emit('image-change', { image: null, file: null })
}

/**
 * 监听文件对象变化
 * 确保每次文件变化都触发事件
 */
watch(selectedFile, (newFile) => {
  emit('image-change', { image: selectedImage.value, file: newFile, url: serverUrl.value, uploading: uploading.value })
})
</script>

<style scoped>
.image-uploader {
  margin-bottom: 24px;
}

.file-input {
  display: none;
}

.image-upload-box {
  width: 100%;
  height: 300px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  background-color: #fafafa;
}

.image-upload-box:hover {
  border-color: #00B1FE;
  background-color: #f0f9ff;
}

.upload-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

.upload-icon {
  margin-bottom: 12px;
  color: #ccc;
}

.upload-text {
  font-size: 18px;
  color: #666;
  margin-bottom: 4px;
}

.upload-subtext {
  font-size: 14px;
  color: #999;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: #000;
}

.remove-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  background-color: rgba(0, 0, 0, 0.6);
  color: #fff;
}

.remove-btn:hover {
  background-color: rgba(0, 0, 0, 0.8);
}

.tips-alert {
  margin-top: 24px;
  font-size: 15px;
  line-height: 1.6;
}

@media (max-width: 1024px) {
  .image-upload-box {
    height: 250px;
  }
}

@media (max-width: 768px) {
  .image-upload-box {
    height: 200px;
  }
}
</style>