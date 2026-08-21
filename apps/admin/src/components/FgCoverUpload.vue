<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, type UploadFile } from 'element-plus'
import { uploadOss } from '@/api/oss'

defineProps<{
  modelValue?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const uploading = ref(false)

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const MAX_SIZE = 2 * 1024 * 1024

function beforeUpload(file: File): boolean {
  if (!ALLOWED_TYPES.includes(file.type)) {
    ElMessage.warning('仅支持 JPG、PNG、WebP 格式')
    return false
  }
  if (file.size > MAX_SIZE) {
    ElMessage.warning('图片大小不能超过 2MB')
    return false
  }
  return true
}

async function handleChange(uploadFile: UploadFile) {
  const file = uploadFile.raw
  if (!file || uploading.value) return
  if (!beforeUpload(file)) return

  uploading.value = true
  try {
    const result = await uploadOss(file)
    emit('update:modelValue', result.url)
    ElMessage.success('封面上传成功')
  } catch (err) {
    ElMessage.error(err instanceof Error ? err.message : '上传失败')
  } finally {
    uploading.value = false
  }
}

function handleClear() {
  emit('update:modelValue', '')
}
</script>

<template>
  <div class="fg-cover-upload">
    <template v-if="modelValue">
      <div class="fg-cover-upload__preview">
        <img :src="modelValue" alt="封面预览" class="fg-cover-upload__image" />
        <div class="fg-cover-upload__actions">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            accept="image/jpeg,image/png,image/webp"
            :disabled="uploading"
            :on-change="handleChange"
          >
            <el-button size="small" :loading="uploading">更换</el-button>
          </el-upload>
          <el-button size="small" :disabled="uploading" @click="handleClear">清除</el-button>
        </div>
      </div>
    </template>
    <template v-else>
      <el-upload
        class="fg-cover-upload__dropzone"
        :auto-upload="false"
        :show-file-list="false"
        accept="image/*"
        :disabled="uploading"
        :before-upload="beforeUpload"
        :on-change="handleChange"
      >
        <div class="fg-cover-upload__placeholder">
          <el-button type="primary" plain :loading="uploading">上传封面</el-button>
          <span class="fg-cover-upload__sub">JPG / PNG / WebP，≤ 2MB</span>
        </div>
      </el-upload>
    </template>
  </div>
</template>

<style scoped>
.fg-cover-upload {
  display: inline-block;
}

.fg-cover-upload__dropzone :deep(.el-upload) {
  display: block;
}

.fg-cover-upload__placeholder {
  width: 200px;
  min-height: 120px;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  box-sizing: border-box;
}

.fg-cover-upload__placeholder:focus-within {
  outline: 3px solid var(--el-color-primary);
  outline-offset: 2px;
}

.fg-cover-upload__actions :deep(.el-button:focus-visible) {
  outline: 3px solid var(--el-color-primary);
  outline-offset: 2px;
}

.fg-cover-upload__sub {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.fg-cover-upload__preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fg-cover-upload__image {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid var(--el-border-color);
}

.fg-cover-upload__actions {
  display: flex;
  gap: 8px;
}
</style>
