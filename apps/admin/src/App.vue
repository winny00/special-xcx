<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const SIDEBAR_KEY = 'admin-sidebar-collapsed'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const collapsed = ref(false)

const isLoginPage = computed(() => route.path === '/login')

const asideWidth = computed(() => (collapsed.value ? '64px' : '220px'))

const activeMenu = computed(() => route.path)

const pageTitle = computed(() => {
  if (route.meta.title && typeof route.meta.title === 'string') {
    return route.meta.title
  }
  const titles: Record<string, string> = {
    '/dashboard': '数据概览',
    '/organization': '机构管理',
    '/article': '资讯管理',
    '/appointment': '预约管理',
    '/audit': '审核中心',
  }
  if (route.path.startsWith('/resource/')) {
    const map: Record<string, string> = {
      course: '课程管理',
      tool: '工具管理',
      teacher: '老师资源',
      assessment: '评估管理',
    }
    const type = route.params.type as string
    return map[type] || '资源管理'
  }
  return titles[route.path] || '特教管理后台'
})

function toggleSidebar() {
  collapsed.value = !collapsed.value
  localStorage.setItem(SIDEBAR_KEY, collapsed.value ? '1' : '0')
}

function handleLogout() {
  auth.logout()
  router.push('/login')
}

onMounted(() => {
  collapsed.value = localStorage.getItem(SIDEBAR_KEY) === '1'
})
</script>

<template>
  <div v-if="isLoginPage">
    <router-view />
  </div>
  <el-container v-else class="layout">
    <el-aside :width="asideWidth" class="aside">
      <div class="brand" :class="{ 'brand--collapsed': collapsed }">
        <div class="brand-mark">
          特
        </div>
        <div v-if="!collapsed" class="brand-text">
          <div class="brand-name">
            特教管理
          </div>
          <div class="brand-desc">
            资源对接工作台
          </div>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        router
        class="side-menu"
      >
        <el-menu-item index="/dashboard">
          <i class="menu-svg" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z" /></svg>
          </i>
          <template #title>数据概览</template>
        </el-menu-item>
        <el-sub-menu index="resource-group">
          <template #title>
            <i class="menu-svg" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" /><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" /></svg>
            </i>
            <span>资源对接</span>
          </template>
          <el-menu-item index="/resource/course">课程管理</el-menu-item>
          <el-menu-item index="/resource/tool">工具管理</el-menu-item>
          <el-menu-item index="/resource/teacher">老师资源</el-menu-item>
          <el-menu-item index="/resource/assessment">评估管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/organization">
          <i class="menu-svg" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 21h18" /><path d="M5 21V7l8-4v18" /><path d="M19 21V11l-6-4" /></svg>
          </i>
          <template #title>机构管理</template>
        </el-menu-item>
        <el-menu-item index="/article">
          <i class="menu-svg" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16v16H4zM8 8h8M8 12h8M8 16h5" /></svg>
          </i>
          <template #title>资讯管理</template>
        </el-menu-item>
        <el-menu-item index="/appointment">
          <i class="menu-svg" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" /><path d="M16 2v4M8 2v4M3 10h18" /></svg>
          </i>
          <template #title>预约管理</template>
        </el-menu-item>
        <el-menu-item index="/audit">
          <i class="menu-svg" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" /></svg>
          </i>
          <template #title>审核中心</template>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-tooltip
          content="展开"
          placement="right"
          :disabled="!collapsed"
        >
          <button
            type="button"
            class="sidebar-toggle"
            :class="{ 'sidebar-toggle--collapsed': collapsed }"
            :aria-label="collapsed ? '展开' : '收起'"
            :aria-expanded="!collapsed"
            @click="toggleSidebar"
          >
            <span class="sidebar-toggle-icon" aria-hidden="true">
              <svg v-if="collapsed" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2" />
                <path d="M9 3v18" />
                <path d="m14 9 3 3-3 3" />
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2" />
                <path d="M9 3v18" />
                <path d="m16 15-3-3 3-3" />
              </svg>
            </span>
            <span v-if="!collapsed" class="sidebar-toggle-label">收起</span>
          </button>
        </el-tooltip>
      </div>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="header-title">{{ pageTitle }}</span>
        </div>
        <div class="header-user">
          <span class="user-name">管理员</span>
          <el-button type="primary" link @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
  min-height: 100vh;
}
.aside {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-right: 1px solid #e7f4f0;
  transition: width 0.2s ease;
  overflow: hidden;
}
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px 16px;
  border-bottom: 1px solid #e7f4f0;
  background: linear-gradient(180deg, #e7f4f0 0%, #fff 100%);
}
.brand--collapsed {
  justify-content: center;
  padding: 20px 8px 16px;
}
.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #1b7f6b;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}
.brand-name {
  font-size: 16px;
  font-weight: 600;
  color: #1b7f6b;
}
.brand-desc {
  margin-top: 2px;
  font-size: 12px;
  color: #4f635f;
}
.side-menu {
  flex: 1;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
  border-right: none;
  padding: 8px;
}
.menu-svg {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  margin-right: 8px;
  color: #1b7f6b;
  font-style: normal;
  flex-shrink: 0;
}
.menu-svg svg {
  width: 18px;
  height: 18px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e7f4f0;
}
.sidebar-footer {
  flex-shrink: 0;
  margin-top: auto;
  padding: 8px;
  border-top: 1px solid #e7f4f0;
  background: #f4f7f6;
}
.sidebar-footer :deep(.el-tooltip__trigger) {
  display: block;
  width: 100%;
}
.sidebar-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  min-height: 44px;
  padding: 0 12px;
  border: 1px solid #e7f4f0;
  border-radius: 8px;
  background: #fff;
  color: #4f635f;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}
.sidebar-toggle:hover {
  background: #e7f4f0;
  border-color: #c0d9d3;
}
.sidebar-toggle--collapsed {
  justify-content: center;
  width: 44px;
  height: 44px;
  margin: 0 auto;
  padding: 0;
}
.sidebar-toggle-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  color: #1b7f6b;
}
.sidebar-toggle-icon svg {
  width: 18px;
  height: 18px;
}
.sidebar-toggle-label {
  color: #4f635f;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1c2b28;
}
.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-name {
  font-size: 14px;
  color: #4f635f;
}
.main {
  background: #f4f7f6;
  padding: 20px 24px 32px;
}
</style>

