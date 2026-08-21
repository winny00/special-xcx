<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import FgSidebar from '@/components/layout/FgSidebar.vue'
import FgHeader from '@/components/layout/FgHeader.vue'
import FgTabsBar from '@/components/layout/FgTabsBar.vue'
import { useTabsStore } from '@/stores/tabs'
import { useAuthStore } from '@/store/auth'

const SIDEBAR_KEY = 'admin-sidebar-collapsed'
const collapsed = ref(localStorage.getItem(SIDEBAR_KEY) === '1')
const asideWidth = computed(() => (collapsed.value ? '64px' : '220px'))
const route = useRoute()
const router = useRouter()
const tabsStore = useTabsStore()
const auth = useAuthStore()

function toggleSidebar() {
  collapsed.value = !collapsed.value
  localStorage.setItem(SIDEBAR_KEY, collapsed.value ? '1' : '0')
}

function handleLogout() {
  auth.logout()
  tabsStore.resetTabs()
  router.push('/login')
}

router.afterEach((to) => {
  tabsStore.syncRoute(to)
})

onMounted(() => {
  tabsStore.syncRoute(route)
})
</script>

<template>
  <el-container class="layout">
    <el-aside
      :width="asideWidth"
      class="aside"
      :class="{ 'aside--collapsed': collapsed }"
      :style="{ width: asideWidth, flex: `0 0 ${asideWidth}` }"
    >
      <FgSidebar :collapsed="collapsed" />
    </el-aside>
    <el-container class="layout-main" direction="vertical">
      <FgHeader :collapsed="collapsed" @toggle-collapse="toggleSidebar" @logout="handleLogout" />
      <FgTabsBar />
      <el-main class="main">
        <router-view v-slot="{ Component, route: r }">
          <keep-alive :max="10">
            <component :is="Component" :key="r.path" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
  min-height: 100vh;
  overflow: hidden;
}

.layout :deep(.el-container) {
  min-width: 0;
}

.aside {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-width: 0;
  max-width: 220px;
  background: var(--fg-surface);
  border-right: 1px solid var(--fg-border);
  transition: width 0.2s ease, flex-basis 0.2s ease;
  overflow: hidden;
}

.aside--collapsed {
  max-width: 64px;
}

.layout-main {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.main {
  background: var(--fg-canvas);
  padding: 20px 24px 32px;
}
</style>
