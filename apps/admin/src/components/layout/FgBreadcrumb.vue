<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { resolveRouteMeta } from '@/utils/route-meta'

const emit = defineEmits<{
  refresh: []
}>()

const route = useRoute()
const router = useRouter()
const crumbs = computed(() => resolveRouteMeta(route).breadcrumb)

function onRefresh(event: MouseEvent) {
  event.stopPropagation()
  emit('refresh')
}
</script>

<template>
  <el-breadcrumb separator="/" class="fg-breadcrumb">
    <el-breadcrumb-item class="fg-breadcrumb__home" @click="router.push('/dashboard')">
      <span class="fg-crumb">
        首页
        <button
          type="button"
          class="fg-crumb__refresh"
          aria-label="刷新当前页"
          title="刷新当前页"
          @click="onRefresh"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M21 12a9 9 0 1 1-3-6.7" />
            <path d="M21 3v6h-6" />
          </svg>
        </button>
      </span>
    </el-breadcrumb-item>
    <el-breadcrumb-item v-for="(item, i) in crumbs" :key="i">
      <span class="fg-crumb">
        {{ item }}
        <button
          type="button"
          class="fg-crumb__refresh"
          aria-label="刷新当前页"
          title="刷新当前页"
          @click="onRefresh"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M21 12a9 9 0 1 1-3-6.7" />
            <path d="M21 3v6h-6" />
          </svg>
        </button>
      </span>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<style scoped>
.fg-breadcrumb__home :deep(.el-breadcrumb__inner) {
  cursor: pointer;
}

.fg-crumb {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.fg-crumb__refresh {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  padding: 0;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: var(--fg-muted);
  cursor: pointer;
}

.fg-crumb__refresh svg {
  width: 14px;
  height: 14px;
}

.fg-crumb__refresh:hover,
.fg-crumb__refresh:focus-visible {
  color: var(--fg-primary);
  background: var(--fg-primary-soft);
}
</style>
