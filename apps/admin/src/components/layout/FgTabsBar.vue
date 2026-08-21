<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useTabsStore } from '@/stores/tabs'

const route = useRoute()
const router = useRouter()
const tabsStore = useTabsStore()

function onTabClick(path: string) {
  router.push(path)
}

function onCloseTab(path: string, event: MouseEvent) {
  event.stopPropagation()
  const next = tabsStore.closeTab(path)
  if (route.path === path)
    router.push(next)
}
</script>

<template>
  <div class="fg-tabs-bar" role="tablist" aria-label="页面标签">
    <div class="fg-tabs-bar__scroll">
      <button
        v-for="tab in tabsStore.tabs"
        :key="tab.path"
        type="button"
        role="tab"
        class="fg-tab"
        :class="{ 'fg-tab--active': tabsStore.activePath === tab.path }"
        :aria-selected="tabsStore.activePath === tab.path"
        @click="onTabClick(tab.path)"
      >
        <span class="fg-tab__title">{{ tab.title }}</span>
        <span
          v-if="tab.closable"
          class="fg-tab__close"
          role="button"
          tabindex="-1"
          aria-label="关闭标签"
          @click="onCloseTab(tab.path, $event)"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6 6 18M6 6l12 12" />
          </svg>
        </span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.fg-tabs-bar {
  flex-shrink: 0;
  background: var(--fg-surface);
  border-bottom: 1px solid var(--fg-border);
}

.fg-tabs-bar__scroll {
  display: flex;
  align-items: stretch;
  gap: 0;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: thin;
}

.fg-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  min-height: 40px;
  padding: 0 16px;
  border: none;
  border-bottom: 2px solid transparent;
  background: transparent;
  color: var(--fg-muted);
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease, background-color 0.2s ease;
}

.fg-tab:hover {
  color: var(--fg-ink);
  background: var(--fg-canvas);
}

.fg-tab--active {
  color: var(--fg-primary);
  border-bottom: 2px solid var(--fg-primary);
  background: var(--fg-surface);
}

.fg-tab__title {
  white-space: nowrap;
}

.fg-tab__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 4px;
  color: var(--fg-muted);
  transition: color 0.2s ease, background-color 0.2s ease;
}

.fg-tab__close svg {
  width: 12px;
  height: 12px;
}

.fg-tab__close:hover {
  color: #e53935;
  background: rgba(229, 57, 53, 0.08);
}
</style>
