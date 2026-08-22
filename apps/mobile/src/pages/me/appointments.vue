<script lang="ts" setup>
import type { IMyAppointment } from '@/api/me'
import { APPOINTMENT_STATUS_MAP, getMyAppointments } from '@/api/me'
import { LOGIN_PAGE } from '@/router/config'
import { useTokenStore } from '@/store/token'
import { isTeacherRole, readCachedRole } from '@/utils/current-role'

definePage({
  style: {
    navigationBarTitleText: '我的预约',
  },
})

const tokenStore = useTokenStore()
const loading = ref(true)
const loadingMore = ref(false)
const appointments = ref<IMyAppointment[]>([])
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)

async function loadList(reset = false) {
  if (!tokenStore.hasLogin) {
    loading.value = false
    return
  }
  if (reset) {
    pageNum.value = 1
    loading.value = true
  }
  else {
    loadingMore.value = true
  }
  try {
    const res = await getMyAppointments({ pageNum: pageNum.value, pageSize })
    const rows = res.rows || []
    appointments.value = reset ? rows : [...appointments.value, ...rows]
    total.value = res.total || 0
  }
  catch (e) {
    console.error(e)
    if (reset)
      appointments.value = []
  }
  finally {
    loading.value = false
    loadingMore.value = false
  }
}

function goLogin() {
  uni.navigateTo({ url: LOGIN_PAGE })
}

function goResourceList() {
  uni.switchTab({ url: '/pages/resource/list' })
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/me/appointment-detail?id=${id}` })
}

function loadMore() {
  if (loadingMore.value || appointments.value.length >= total.value)
    return
  pageNum.value += 1
  loadList(false)
}

function formatDate(value?: string) {
  return value ? value.slice(0, 16).replace('T', ' ') : ''
}

onShow(() => {
  uni.setNavigationBarTitle({
    title: isTeacherRole(readCachedRole()) ? '收到的预约' : '我的预约',
  })
  if (!tokenStore.hasLogin) {
    loading.value = false
    return
  }
  loadList(true)
})
</script>

<template>
  <view class="min-h-screen bg-[#F4F7F6] pb-safe">
    <fg-empty-state
      v-if="!tokenStore.hasLogin"
      title="登录后查看预约"
      description="微信登录后可查看您提交的咨询预约及处理进度"
      action-text="去登录"
      @action="goLogin"
    />

    <view v-else class="mx-3 mt-3">
      <fg-skeleton-block v-if="loading" :rows="3" />
      <fg-empty-state
        v-else-if="appointments.length === 0"
        title="暂无预约记录"
        description="浏览资源并提交预约后，可在此查看处理进度"
        action-text="浏览资源"
        @action="goResourceList"
      />
      <view v-else class="flex flex-col gap-3">
        <view
          v-for="item in appointments"
          :key="String(item.id)"
          class="appointment-card fg-surface-card fg-tap-active p-4"
          @click="goDetail(item.id)"
        >
          <view class="flex items-start justify-between gap-2">
            <text class="flex-1 text-base font-semibold text-[#1C2B28]">
              {{ item.resourceTitle || '咨询预约' }}
            </text>
            <text
              class="status-tag"
              :class="`status-tag--${APPOINTMENT_STATUS_MAP[item.appointStatus ?? 0]?.tone || 'info'}`"
            >
              {{ APPOINTMENT_STATUS_MAP[item.appointStatus ?? 0]?.label || '待处理' }}
            </text>
          </view>
          <view class="mt-2 text-sm text-muted">
            联系人：{{ item.contactName || '—' }}
          </view>
          <view class="mt-3 flex items-center justify-between text-xs text-muted">
            <text>{{ formatDate(item.createTime) }}</text>
            <text class="i-carbon-chevron-right" />
          </view>
        </view>

        <view
          v-if="appointments.length < total"
          class="load-more fg-tap-active"
          @click="loadMore"
        >
          {{ loadingMore ? '加载中…' : '加载更多' }}
        </view>
      </view>
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
  font-size: 12px;
}
.status-tag--info {
  background: #eef2f1;
  color: var(--color-muted);
}
.status-tag--primary {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}
.status-tag--success {
  background: #e7f4f0;
  color: var(--color-primary);
}
.status-tag--warning {
  background: #fff4e5;
  color: #b26a00;
}
.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  margin-bottom: 16px;
  font-size: 14px;
  color: var(--color-primary);
}
</style>
