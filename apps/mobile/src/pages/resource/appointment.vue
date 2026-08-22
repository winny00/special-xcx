<script lang="ts" setup>
import { createAppointment } from '@/api/special'
import { BIND_PHONE_PAGE, LOGIN_PAGE } from '@/router/config'
import { useUserStore } from '@/store'
import { useTokenStore } from '@/store/token'
import { isPhoneBound } from '@/utils/current-role'

definePage({
  style: {
    navigationBarTitleText: '预约咨询',
  },
})

const tokenStore = useTokenStore()
const userStore = useUserStore()
const resourceId = ref('')
const teacherId = ref('')
const resourceTitle = ref('')
const contactName = ref('')
const contactPhone = ref('')
const childAge = ref('')
const remark = ref('')
const submitting = ref(false)

function currentPageUrl() {
  const parts: string[] = []
  if (resourceTitle.value) {
    parts.push(`title=${encodeURIComponent(resourceTitle.value)}`)
  }
  if (teacherId.value) {
    parts.push(`teacherId=${teacherId.value}`)
  }
  if (resourceId.value) {
    parts.push(`resourceId=${resourceId.value}`)
  }
  return parts.length ? `/pages/resource/appointment?${parts.join('&')}` : '/pages/resource/appointment'
}

function requirePhoneBound() {
  if (!tokenStore.hasLogin) {
    uni.navigateTo({ url: LOGIN_PAGE })
    return false
  }
  if (!isPhoneBound(userStore.userInfo.phoneBound)) {
    uni.navigateTo({
      url: `${BIND_PHONE_PAGE}?redirect=${encodeURIComponent(currentPageUrl())}`,
    })
    return false
  }
  return true
}

async function submit() {
  if (!requirePhoneBound()) {
    return
  }
  if (!contactName.value.trim()) {
    uni.showToast({ title: '请填写联系人', icon: 'none' })
    return
  }
  if (!contactPhone.value.trim()) {
    uni.showToast({ title: '请填写联系电话', icon: 'none' })
    return
  }
  if (!resourceId.value && !teacherId.value) {
    uni.showToast({ title: '请选择资源或老师', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await createAppointment({
      ...(resourceId.value ? { resourceId: resourceId.value } : {}),
      ...(teacherId.value ? { teacherId: teacherId.value } : {}),
      resourceTitle: resourceTitle.value,
      contactName: contactName.value,
      contactPhone: contactPhone.value,
      childAge: childAge.value,
      remark: remark.value,
    })
    uni.showToast({ title: '提交成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  }
  catch (e) {
    console.error(e)
  }
  finally {
    submitting.value = false
  }
}

onLoad((query) => {
  resourceId.value = String(query?.resourceId || '')
  teacherId.value = String(query?.teacherId || '')
  resourceTitle.value = decodeURIComponent((query?.title as string) || '')
  requirePhoneBound()
})
</script>

<template>
  <view class="min-h-screen bg-[#F4F7F6] p-3 pb-safe">
    <view class="mb-3 rounded-[12px] bg-white px-4 py-3">
      <view class="text-xs text-muted">
        预约资源
      </view>
      <view class="mt-1 text-base font-medium text-[#1C2B28]">
        {{ resourceTitle || '特教资源' }}
      </view>
    </view>

    <view class="mb-3 rounded-[12px] bg-white px-4 py-4">
      <view class="mb-3 text-sm font-medium text-[#1C2B28]">
        联系人
      </view>
      <view class="mb-3">
        <text class="mb-1.5 block text-xs text-muted">姓名</text>
        <input
          v-model="contactName"
          class="h-11 w-full rounded-lg bg-[#F4F7F6] px-3 text-base text-[#1C2B28]"
          placeholder="请输入您的姓名"
        >
      </view>
      <view>
        <text class="mb-1.5 block text-xs text-muted">手机号</text>
        <input
          v-model="contactPhone"
          class="h-11 w-full rounded-lg bg-[#F4F7F6] px-3 text-base text-[#1C2B28]"
          type="number"
          placeholder="请输入手机号"
        >
      </view>
    </view>

    <view class="mb-3 rounded-[12px] bg-white px-4 py-4">
      <view class="mb-3 text-sm font-medium text-[#1C2B28]">
        孩子情况
      </view>
      <view>
        <text class="mb-1.5 block text-xs text-muted">年龄</text>
        <input
          v-model="childAge"
          class="h-11 w-full rounded-lg bg-[#F4F7F6] px-3 text-base text-[#1C2B28]"
          placeholder="如：5岁"
        >
      </view>
    </view>

    <view class="mb-4 rounded-[12px] bg-white px-4 py-4">
      <view class="mb-3 text-sm font-medium text-[#1C2B28]">
        需求
      </view>
      <textarea
        v-model="remark"
        class="min-h-24 w-full rounded-lg bg-[#F4F7F6] p-3 text-sm text-[#1C2B28]"
        placeholder="请描述您的具体需求..."
        maxlength="500"
      />
    </view>

    <wd-button block type="primary" :loading="submitting" @click="submit">
      提交预约
    </wd-button>

    <view class="mt-3 text-center text-xs text-muted">
      提交后工作人员将在 1-2 个工作日内与您联系
    </view>
  </view>
</template>
