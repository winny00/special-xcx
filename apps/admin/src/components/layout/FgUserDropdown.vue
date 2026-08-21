<script setup lang="ts">
const emit = defineEmits<{
  logout: []
}>()

function onCommand(command: string) {
  if (command === 'logout')
    emit('logout')
}
</script>

<template>
  <el-dropdown trigger="click" @command="onCommand">
    <span class="user-trigger" role="button" tabindex="0" aria-label="用户菜单">
      <span class="user-avatar" aria-hidden="true">管</span>
      <span class="user-name">管理员</span>
      <i class="user-chevron" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="m6 9 6 6 6-6" />
        </svg>
      </i>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="logout">
          退出登录
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped>
.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 44px;
  padding: 0 4px;
  cursor: pointer;
  outline: none;
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--fg-primary-soft);
  color: var(--fg-primary);
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-name {
  font-size: 14px;
  color: var(--fg-muted);
}

.user-chevron {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  color: var(--fg-muted);
}

.user-chevron svg {
  width: 16px;
  height: 16px;
}
</style>
