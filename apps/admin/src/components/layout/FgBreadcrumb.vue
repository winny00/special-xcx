<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { resolveRouteMeta } from '@/utils/route-meta'

const route = useRoute()
const router = useRouter()
const crumbs = computed(() => resolveRouteMeta(route).breadcrumb)
</script>

<template>
  <el-breadcrumb separator="/" class="fg-breadcrumb">
    <el-breadcrumb-item class="fg-breadcrumb__home" @click="router.push('/dashboard')">
      首页
    </el-breadcrumb-item>
    <el-breadcrumb-item v-for="(item, i) in crumbs" :key="i">
      {{ item }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<style scoped>
.fg-breadcrumb__home :deep(.el-breadcrumb__inner) {
  cursor: pointer;
}
</style>
