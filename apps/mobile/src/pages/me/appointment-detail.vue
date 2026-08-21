<script lang="ts" setup>
import type { IMyAppointment } from '@/api/me'
import { APPOINTMENT_STATUS_MAP, getMyAppointmentDetail } from '@/api/me'

definePage({
  style: {
    navigationBarTitleText: '预约详情',
  },
})

const appointment = ref<IMyAppointment | null>(null)
const loading = ref(true)
const appointmentId = ref('')

const statusMeta = computed(() => {
  const status = appointment.value?.appointStatus ?? 0
  return APPOINTMENT_STATUS_MAP[status] || APPOINTMENT_STATUS_MAP[0]
})

async function loadDetail() {
  if (!appointmentId.value) {
    loading.value = false
    appointment.value = null
    return
  }
  loading.value = true
  try {
    appointment.value = await getMyAppointmentDetail(appointmentId.value)
  }
  catch (e) {
    console.error(e)
    appointment.value = null
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
  uni.navigateTo({ url: '/pages/me/appointments' })
}

function formatDate(value?: string) {
  return value ? value.slice(0, 16).replace('T', ' ') : '—'
}

onLoad((query) => {
  appointmentId.value = String(query?.id || '')
  loadDetail()
})
</script>

<template>
  <view class="detail-page min-h-screen bg-[#F4F7F6]">
    <fg-skeleton-block v-if="loading" :rows="1" />
    <fg-empty-state
      v-else-if="!appointment"
      title="暂无该预约"
      description="预约可能不存在或无权查看"
      action-text="返回列表"
      @action="goBack"
    />
    <template v-else>
      <view class="fg-surface-card mx-3 mt-3 p-4">
        <view class="flex items-start justify-between gap-2">
          <view class="min-w-0 flex-1">
            <view class="text-xs text-muted">
              预约资源
            </view>
            <view class="mt-1 text-xl font-semibold leading-snug text-[#1C2B28]">
              {{ appointment.resourceTitle || '咨询预约' }}
            </view>
          </view>
          <text
            class="status-tag"
            :class="`status-tag--${statusMeta.tone}`"
          >
            {{ statusMeta.label }}
          </text>
        </view>
      </view>

      <view class="fg-surface-card mx-3 mt-3 p-4">
        <view class="section-title">
          联系信息
        </view>
        <view class="info-row">
          <text class="info-label">联系人</text>
          <text class="info-value">{{ appointment.contactName || '—' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">联系电话</text>
          <text class="info-value">{{ appointment.contactPhone || '—' }}</text>
        </view>
        <view v-if="appointment.childAge" class="info-row">
          <text class="info-label">儿童年龄</text>
          <text class="info-value">{{ appointment.childAge }}</text>
        </view>
      </view>

      <view v-if="appointment.remark" class="fg-surface-card mx-3 mt-3 p-4">
        <view class="section-title">
          需求说明
        </view>
        <view class="text-sm leading-relaxed text-[#1C2B28]">
          {{ appointment.remark }}
        </view>
      </view>

      <view v-if="appointment.handlerRemark" class="fg-surface-card mx-3 mt-3 p-4">
        <view class="section-title">
          处理备注
        </view>
        <view class="text-sm leading-relaxed text-muted">
          {{ appointment.handlerRemark }}
        </view>
      </view>

      <view class="fg-surface-card mx-3 mt-3 mb-6 p-4">
        <view class="info-row">
          <text class="info-label">提交时间</text>
          <text class="info-value">{{ formatDate(appointment.createTime) }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">更新时间</text>
          <text class="info-value">{{ formatDate(appointment.updateTime) }}</text>
        </view>
      </view>
    </template>
  </view>
</template>

<style scoped lang="scss">
.section-title {
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-ink);
}
.info-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid var(--color-canvas);
  font-size: 14px;
}
.info-row:last-child {
  border-bottom: none;
}
.info-label {
  color: var(--color-muted);
}
.info-value {
  color: var(--color-ink);
  text-align: right;
}
.status-tag {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  white-space: nowrap;
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
</style>
