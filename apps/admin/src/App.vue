<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const isLoginPage = computed(() => route.path === '/login')

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/resource': '资源管理',
    '/organization': '机构管理',
    '/appointment': '预约管理',
  }
  return titles[route.path] || '特教管理后台'
})

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div v-if="isLoginPage">
    <router-view />
  </div>
  <el-container v-else class="layout">
    <el-aside width="220px" class="aside">
      <div class="brand">
        <div class="brand-name">特教管理</div>
        <div class="brand-desc">资源对接工作台</div>
      </div>
      <el-menu
        :default-active="route.path"
        router
        class="side-menu"
      >
        <el-menu-item index="/resource">资源管理</el-menu-item>
        <el-menu-item index="/organization">机构管理</el-menu-item>
        <el-menu-item index="/appointment">预约管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="header-title">{{ pageTitle }}</span>
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
  background: #fff;
  border-right: 1px solid #e7f4f0;
}
.brand {
  padding: 24px 20px 16px;
}
.brand-name {
  font-size: 18px;
  font-weight: 600;
  color: #1b7f6b;
}
.brand-desc {
  margin-top: 4px;
  font-size: 12px;
  color: #4F635F;
}
.side-menu {
  border-right: none;
  padding: 0 12px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e7f4f0;
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
  color: #4F635F;
}
.main {
  background: #f4f7f6;
  padding: 20px 24px 32px;
}
</style>

<style>
:root {
  --el-color-primary: #1b7f6b;
  --el-color-primary-light-3: #4d9a8a;
  --el-color-primary-light-5: #8dbbb1;
  --el-color-primary-light-7: #c0d9d3;
  --el-color-primary-light-8: #d4e6e2;
  --el-color-primary-light-9: #e7f4f0;
  --el-color-primary-dark-2: #166656;
}
:focus-visible {
  outline: 3px solid var(--el-color-primary);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
html,
body,
#app {
  height: 100%;
}
body {
  margin: 0;
  color: #1c2b28;
  background: #f4f7f6;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}
.workbench-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.workbench-head h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1c2b28;
}
.workbench-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
.workbench-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 16px 8px;
}
.workbench-card .el-pagination {
  margin: 16px 0 8px;
  justify-content: flex-end;
}
.el-aside .el-menu-item {
  border-radius: 8px;
  margin-bottom: 4px;
}
.el-aside .el-menu-item.is-active {
  background: #e7f4f0;
  color: #1b7f6b;
}
</style>
