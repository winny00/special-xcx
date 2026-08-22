<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

defineProps<{
  collapsed: boolean
}>()

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<template>
  <div class="fg-sidebar">
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
      <el-menu-item index="/teacher">
        <i class="menu-svg" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" /></svg>
        </i>
        <template #title>老师档案</template>
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
      <el-menu-item index="/parent">
        <i class="menu-svg" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" /></svg>
        </i>
        <template #title>家长管理</template>
      </el-menu-item>
      <el-menu-item index="/audit">
        <i class="menu-svg" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" /></svg>
        </i>
        <template #title>审核中心</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<style scoped>
.fg-sidebar {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px 16px;
  border-bottom: 1px solid var(--fg-border);
  background: linear-gradient(180deg, var(--fg-primary-soft) 0%, var(--fg-surface) 100%);
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
  background: var(--fg-primary);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}

.brand-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--fg-primary);
}

.brand-desc {
  margin-top: 2px;
  font-size: 12px;
  color: var(--fg-muted);
}

.side-menu {
  flex: 1;
  width: 100% !important;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
  border-right: none;
  padding: 8px;
  box-sizing: border-box;
}

.side-menu.el-menu--collapse {
  width: 100% !important;
  padding: 8px 4px;
}

.side-menu :deep(.el-menu-item.is-active) {
  position: relative;
  background: var(--fg-primary-soft);
  color: var(--fg-primary);
}

.side-menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 2px;
  background: var(--fg-primary);
}

.menu-svg {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  margin-right: 8px;
  color: var(--fg-primary);
  font-style: normal;
  flex-shrink: 0;
}

.menu-svg svg {
  width: 18px;
  height: 18px;
}
</style>
