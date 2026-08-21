<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getDashboardStats, type DashboardStats } from '@/api/special'

const loading = ref(true)
const stats = ref<DashboardStats | null>(null)

const typeLabels: Record<string, string> = {
  course: '课程',
  tool: '工具',
  teacher: '老师',
  assessment: '评估',
}

const statCards = computed(() => {
  if (!stats.value)
    return []
  const s = stats.value
  return [
    { label: '资源总数', value: s.resourceTotal, hint: '课程 / 工具 / 老师 / 评估', icon: 'resource' },
    { label: '资源草稿', value: s.resourceDraftCount, hint: '待发布', icon: 'draft' },
    { label: '机构待审', value: s.orgAuditPending, hint: '入驻审核', icon: 'audit' },
    { label: '预约待处理', value: s.appointmentPending, hint: `今日新增 ${s.appointmentToday}`, icon: 'appoint' },
  ]
})

const typeTotal = computed(() => {
  if (!stats.value?.resourceByType)
    return 0
  return Object.values(stats.value.resourceByType).reduce((a, b) => a + b, 0)
})

async function loadStats() {
  loading.value = true
  try {
    stats.value = await getDashboardStats()
  }
  finally {
    loading.value = false
  }
}

onMounted(loadStats)
</script>

<template>
  <div v-loading="loading">
    <div v-if="stats" class="dashboard-grid">
      <div v-for="card in statCards" :key="card.label" class="stat-card">
        <div class="stat-card__head">
          <span class="stat-icon" :class="`stat-icon--${card.icon}`" aria-hidden="true" />
          <span class="stat-label">{{ card.label }}</span>
        </div>
        <div class="stat-value">{{ card.value }}</div>
        <div class="stat-hint">{{ card.hint }}</div>
      </div>
    </div>

    <div v-if="stats" class="workbench-card type-breakdown">
      <h3 class="section-title">资源类型分布</h3>
      <div class="type-list">
        <div v-for="(count, type) in stats.resourceByType" :key="type" class="type-row">
          <div class="type-row__main">
            <span class="type-name">{{ typeLabels[type] || type }}</span>
            <span class="type-count">{{ count }}</span>
          </div>
          <div class="type-bar">
            <div
              class="type-bar__fill"
              :style="{ width: typeTotal ? `${Math.round((count / typeTotal) * 100)}%` : '0%' }"
            />
          </div>
        </div>
      </div>
      <div class="quick-links">
        <router-link to="/resource/course">课程管理</router-link>
        <router-link to="/resource/tool">工具管理</router-link>
        <router-link to="/resource/teacher">老师资源</router-link>
        <router-link to="/resource/assessment">评估管理</router-link>
        <router-link to="/organization">机构管理</router-link>
        <router-link to="/appointment">预约管理</router-link>
        <router-link to="/audit">审核中心</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px 20px;
  box-shadow: 0 2px 12px rgba(28, 43, 40, 0.06);
  border: 1px solid #e7f4f0;
  transition: box-shadow 0.2s ease;

  &:hover {
    box-shadow: 0 4px 16px rgba(28, 43, 40, 0.1);
  }
}
.stat-card__head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.stat-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #e7f4f0;
}
.stat-icon--resource { background: linear-gradient(135deg, #e7f4f0, #c0d9d3); }
.stat-icon--draft { background: linear-gradient(135deg, #f4f7f6, #e7f4f0); }
.stat-icon--audit { background: linear-gradient(135deg, #e7f4f0, #d4e6e2); }
.stat-icon--appoint { background: linear-gradient(135deg, #d4e6e2, #e7f4f0); }
.stat-label {
  font-size: 13px;
  color: #4f635f;
}
.stat-value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 600;
  color: #1c2b28;
}
.stat-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #4f635f;
}
.type-breakdown {
  margin-top: 4px;
}
.section-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: #1c2b28;
}
.type-list {
  display: grid;
  gap: 14px;
}
.type-row__main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.type-name {
  color: #1c2b28;
}
.type-count {
  font-weight: 600;
  color: #1b7f6b;
}
.type-bar {
  height: 6px;
  border-radius: 999px;
  background: #f4f7f6;
  overflow: hidden;
}
.type-bar__fill {
  height: 100%;
  border-radius: 999px;
  background: #1b7f6b;
  transition: width 0.3s ease;
}
.quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e7f4f0;
}
.quick-links a {
  font-size: 13px;
  color: #1b7f6b;
  text-decoration: none;
  line-height: 1.5;
}
.quick-links a:hover {
  color: #156b5a;
  text-decoration: underline;
}
.quick-links a:focus-visible {
  outline: 3px solid #1b7f6b;
  outline-offset: 2px;
  border-radius: 2px;
}
</style>
