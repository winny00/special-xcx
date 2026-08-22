<script lang="ts" setup>
import type { ISpecialTeacher } from '@/api/types/special'
import { getMyTeacherProfile, updateMyTeacherProfile } from '@/api/me'
import { BIND_PHONE_PAGE, LOGIN_PAGE } from '@/router/config'
import { useUserStore } from '@/store'
import { useTokenStore } from '@/store/token'
import { isPhoneBound } from '@/utils/current-role'

definePage({
  style: {
    navigationBarTitleText: '老师资料',
  },
})

const STATUS_LABEL: Record<number, string> = {
  0: '待审',
  1: '已通过',
  2: '已拒绝',
}

const tokenStore = useTokenStore()
const userStore = useUserStore()
const loading = ref(true)
const saving = ref(false)
const profile = ref<ISpecialTeacher | null>(null)
const name = ref('')
const title = ref('')
const specialties = ref('')
const qualification = ref('')
const intro = ref('')

function toast(message: string) {
  uni.showToast({ title: message, icon: 'none' })
}

async function loadProfile() {
  if (!tokenStore.hasLogin) {
    loading.value = false
    uni.navigateTo({ url: LOGIN_PAGE })
    return
  }
  if (!isPhoneBound(userStore.userInfo.phoneBound)) {
    loading.value = false
    uni.redirectTo({
      url: `${BIND_PHONE_PAGE}?redirect=${encodeURIComponent('/pages/me/teacher-profile')}`,
    })
    return
  }
  loading.value = true
  try {
    const row = await getMyTeacherProfile()
    profile.value = row
    name.value = row?.name || ''
    title.value = row?.title || ''
    specialties.value = row?.specialties || ''
    qualification.value = row?.qualification || ''
    intro.value = row?.intro || ''
  }
  catch (e) {
    console.error(e)
    profile.value = null
  }
  finally {
    loading.value = false
  }
}

async function saveProfile() {
  const trimmed = name.value.trim()
  if (!trimmed) {
    toast('请填写姓名')
    return
  }
  if (saving.value) {
    return
  }
  saving.value = true
  try {
    await updateMyTeacherProfile({
      name: trimmed,
      title: title.value.trim(),
      specialties: specialties.value.trim(),
      qualification: qualification.value.trim(),
      intro: intro.value.trim(),
    })
    uni.showToast({ title: '已保存', icon: 'success' })
    loadProfile()
  }
  catch (e) {
    console.error(e)
  }
  finally {
    saving.value = false
  }
}

onShow(() => {
  loadProfile()
})
</script>

<template>
  <view class="min-h-screen bg-[#F4F7F6] pb-safe">
    <fg-skeleton-block v-if="loading" :rows="3" />
    <fg-empty-state
      v-else-if="!profile"
      title="暂无老师档案"
      description="请联系管理员为当前账号补建老师档案"
    />
    <view v-else class="mx-3 mt-3 fg-surface-card px-4 py-5">
      <view class="mb-4 flex items-center justify-between">
        <text class="text-base font-semibold text-[#1C2B28]">审核状态</text>
        <text class="status-tag">{{ STATUS_LABEL[profile.status ?? 0] || '待审' }}</text>
      </view>
      <view class="mb-4">
        <text class="mb-2 block text-sm text-muted">姓名</text>
        <input
          v-model="name"
          class="h-11 w-full rounded-lg bg-[#F4F7F6] px-3 text-base text-[#1C2B28]"
          placeholder="请输入姓名"
        >
      </view>
      <view class="mb-4">
        <text class="mb-2 block text-sm text-muted">职称</text>
        <input
          v-model="title"
          class="h-11 w-full rounded-lg bg-[#F4F7F6] px-3 text-base text-[#1C2B28]"
          placeholder="请输入职称"
        >
      </view>
      <view class="mb-4">
        <text class="mb-2 block text-sm text-muted">擅长领域</text>
        <input
          v-model="specialties"
          class="h-11 w-full rounded-lg bg-[#F4F7F6] px-3 text-base text-[#1C2B28]"
          placeholder="例如感统、语言"
        >
      </view>
      <view class="mb-4">
        <text class="mb-2 block text-sm text-muted">资质说明</text>
        <textarea
          v-model="qualification"
          class="min-h-24 w-full rounded-lg bg-[#F4F7F6] px-3 py-3 text-base text-[#1C2B28]"
          placeholder="请输入资质说明"
        />
      </view>
      <view class="mb-6">
        <text class="mb-2 block text-sm text-muted">简介</text>
        <textarea
          v-model="intro"
          class="min-h-24 w-full rounded-lg bg-[#F4F7F6] px-3 py-3 text-base text-[#1C2B28]"
          placeholder="请输入简介"
        />
      </view>
      <wd-button block type="primary" :disabled="saving" @click="saveProfile">
        保存
      </wd-button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.status-tag {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: var(--color-primary-soft);
  font-size: 12px;
  color: var(--color-primary);
}
</style>
