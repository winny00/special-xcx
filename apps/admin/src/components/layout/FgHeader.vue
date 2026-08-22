<script setup lang="ts">
import FgBreadcrumb from './FgBreadcrumb.vue'
import FgUserDropdown from './FgUserDropdown.vue'

defineProps<{
  collapsed: boolean
}>()

const emit = defineEmits<{
  'toggle-collapse': []
  logout: []
}>()

function onLogout() {
  emit('logout')
}
</script>

<template>
  <el-header class="fg-header" height="50px">
    <div class="fg-header__left">
      <button
        type="button"
        class="fg-header__collapse"
        :aria-label="collapsed ? '展开侧栏' : '收起侧栏'"
        :aria-expanded="!collapsed"
        @click="emit('toggle-collapse')"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="3" width="18" height="18" rx="2" />
          <path d="M9 3v18" />
          <path v-if="collapsed" d="m14 9 3 3-3 3" />
          <path v-else d="m16 15-3-3 3-3" />
        </svg>
      </button>
    </div>
    <div class="fg-header__center">
      <div class="fg-header__crumbs">
        <FgBreadcrumb />
      </div>
    </div>
    <div class="fg-header__right">
      <FgUserDropdown @logout="onLogout" />
    </div>
  </el-header>
</template>

<style scoped>
.fg-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 20px;
  background: var(--fg-surface);
  border-bottom: 1px solid var(--fg-border);
}

.fg-header__left {
  flex-shrink: 0;
}

.fg-header__center {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.fg-header__crumbs {
  min-width: 0;
}

.fg-header__right {
  flex-shrink: 0;
}

.fg-header__collapse {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  padding: 0;
  border: 1px solid var(--fg-border);
  border-radius: var(--fg-radius-sm);
  background: var(--fg-surface);
  color: var(--fg-primary);
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}

.fg-header__collapse svg {
  width: 18px;
  height: 18px;
}

.fg-header__collapse:hover {
  background: var(--fg-primary-soft);
  border-color: var(--el-color-primary-light-7);
}
</style>
