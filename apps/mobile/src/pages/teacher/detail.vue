<script lang="ts" setup>
import type { ISpecialTeacher } from '@/api/types/special'
import { getTeacherDetail } from '@/api/special'
import { BIND_PHONE_PAGE, LOGIN_PAGE } from '@/router/config'
import { useUserStore } from '@/store'
import { useTokenStore } from '@/store/token'
import { isPhoneBound } from '@/utils/current-role'

definePage({
  style: {
    navigationBarTitleText: '老师详情',
  },
})

const tokenStore = useTokenStore()
const userStore = useUserStore()
const teacher = ref<ISpecialTeacher | null>(null)
const loading = ref(true)
const teacherId = ref('')

async function loadDetail() {
  if (!teacherId.value) {
    loading.value = false
    teacher.value = null
    return
  }
  loading.value = true
  try {
    teacher.value = await getTeacherDetail(teacherId.value)
  }
  catch (e) {
    console.error(e)
    teacher.value = null
  }
  finally {
    loading.value = false
  }
}

function goBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
    return
  }
  uni.navigateTo({ url: '/pages/teacher/list' })
}

function goAppointment() {
  if (!teacher.value) {
    return
  }
  if (!tokenStore.hasLogin) {
    uni.navigateTo({ url: LOGIN_PAGE })
    return
  }
  const title = encodeURIComponent(teacher.value.name || '')
  const tid = teacher.value.id
  let url = `/pages/resource/appointment?title=${title}&teacherId=${tid}`
  if (teacher.value.resourceId) {
    url += `&resourceId=${teacher.value.resourceId}`
  }
  if (!isPhoneBound(userStore.userInfo.phoneBound)) {
    uni.navigateTo({
      url: `${BIND_PHONE_PAGE}?redirect=${encodeURIComponent(url)}`,
    })
    return
  }
  uni.navigateTo({ url })
}

onLoad((query) => {
  teacherId.value = String(query?.id || '')
  loadDetail()
})
</script>

<template>
  <view class="detail-page min-h-screen bg-[#F4F7F6]">
    <fg-skeleton-block v-if="loading" :rows="1" />
    <fg-empty-state
      v-else-if="!teacher"
      title="暂无该老师"
      description="老师可能尚未通过审核"
      action-text="返回列表"
      @action="goBack"
    />
    <template v-else>
      <image
        v-if="teacher.avatarUrl"
        :src="teacher.avatarUrl"
        mode="aspectFill"
        class="detail-cover w-full"
      />
      <view class="fg-surface-card mx-3 mt-3 p-4">
        <view class="text-xl font-semibold leading-snug text-[#1C2B28]">
          {{ teacher.name }}
        </view>
        <view v-if="teacher.title" class="mt-2 text-sm text-muted">
          {{ teacher.title }}
        </view>
        <view v-if="teacher.specialties" class="mt-3 text-sm text-[#1B7F6B]">
          {{ teacher.specialties }}
        </view>
        <view v-if="teacher.qualification" class="mt-4 text-base leading-relaxed text-[#1C2B28]">
          {{ teacher.qualification }}
        </view>
        <view v-if="teacher.intro" class="mt-4 text-base leading-relaxed text-[#1C2B28]">
          {{ teacher.intro }}
        </view>
      </view>
      <image
        v-if="teacher.certImageUrl"
        :src="teacher.certImageUrl"
        mode="widthFix"
        class="mx-3 mt-3 w-auto"
        style="width: calc(100% - 24px); border-radius: 12px"
      />
      <view class="mx-3 mt-4 mb-6">
        <wd-button block type="primary" @click="goAppointment">
          预约咨询
        </wd-button>
      </view>
    </template>
  </view>
</template>

<style scoped lang="scss">
.detail-cover {
  height: 200px;
}
</style>
