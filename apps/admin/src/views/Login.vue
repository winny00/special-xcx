<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request, { CLIENT_ID } from '@/api/request'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
})

const theme = ref<'light' | 'dark'>(
  (localStorage.getItem('admin-login-theme') as 'light' | 'dark') || 'light',
)

interface LoginVo {
  access_token?: string
}

function toggleTheme() {
  theme.value = theme.value === 'light' ? 'dark' : 'light'
  localStorage.setItem('admin-login-theme', theme.value)
}

function handleReset() {
  form.username = ''
  form.password = ''
}

async function handleLogin() {
  if (loading.value) {
    return
  }
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const data = await request.post<LoginVo>('/auth/login', {
      clientId: CLIENT_ID,
      grantType: 'password',
      username: form.username,
      password: form.password,
    })
    const token = data.access_token
    if (!token) {
      ElMessage.error('登录失败：未返回 token')
      return
    }
    auth.saveToken(token)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page" :class="theme">
    <button type="button" class="theme-toggle" aria-label="切换主题" @click="toggleTheme">
      <svg v-if="theme === 'light'" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path
          d="M12 3v2M12 19v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M3 12h2M19 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"
          stroke="currentColor"
          stroke-width="1.8"
          stroke-linecap="round"
        />
        <circle cx="12" cy="12" r="4" stroke="currentColor" stroke-width="1.8" />
      </svg>
      <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path
          d="M21 14.5A8.5 8.5 0 0 1 9.5 3 7 7 0 1 0 21 14.5Z"
          stroke="currentColor"
          stroke-width="1.8"
          stroke-linejoin="round"
        />
      </svg>
    </button>

    <div class="login-illustration">
      <img src="/login-illustration.svg" alt="" width="480" height="360" />
    </div>

    <div class="login-panel">
      <div class="login-card">
        <div class="login-brand">
          <div class="brand-mark">特</div>
          <h1>特教管理后台</h1>
        </div>

        <el-form @submit.prevent="handleLogin">
          <el-form-item>
            <el-input v-model="form.username" placeholder="用户名" autocomplete="username">
              <template #prefix>
                <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="1.8" />
                  <path d="M5 20c0-3.314 3.134-6 7-6s7 2.686 7 6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="密码"
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <rect x="5" y="11" width="14" height="10" rx="2" stroke="currentColor" stroke-width="1.8" />
                  <path d="M8 11V8a4 4 0 0 1 8 0v3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <div class="login-actions">
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" native-type="submit" :loading="loading">登录</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: stretch;
  background: var(--fg-canvas, #f4f7f6);
  background-image:
    radial-gradient(circle at 8% 12%, rgba(27, 127, 107, 0.08) 0%, transparent 42%),
    radial-gradient(circle at 92% 88%, rgba(184, 212, 232, 0.35) 0%, transparent 40%);
  color: var(--fg-ink, #1c2b28);
}

.login-page.dark {
  background: #1a2422;
  background-image:
    radial-gradient(circle at 8% 12%, rgba(27, 127, 107, 0.15) 0%, transparent 42%),
    radial-gradient(circle at 92% 88%, rgba(184, 212, 232, 0.08) 0%, transparent 40%);
  color: #e7f4f0;
}

.theme-toggle {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  padding: 0;
  border: 1px solid var(--fg-border, #e7f4f0);
  border-radius: 999px;
  background: var(--fg-surface, #fff);
  color: var(--fg-muted, #4f635f);
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.theme-toggle svg {
  width: 20px;
  height: 20px;
}

.theme-toggle:hover {
  border-color: var(--fg-primary, #1b7f6b);
  color: var(--fg-primary, #1b7f6b);
}

.theme-toggle:focus-visible {
  outline: 3px solid var(--fg-primary, #1b7f6b);
  outline-offset: 2px;
}

.login-page.dark .theme-toggle {
  background: #24302d;
  border-color: #2f403c;
  color: #c0d9d3;
}

.login-illustration {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 32px;
}

.login-illustration img {
  max-width: 100%;
  height: auto;
}

.login-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 32px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 40px 36px;
  background: var(--fg-surface, #fff);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(28, 43, 40, 0.06);
}

.login-page.dark .login-card {
  background: #24302d;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
}

.login-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 32px;
}

.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: var(--fg-primary-soft, #e7f4f0);
  color: var(--fg-primary, #1b7f6b);
  font-size: 20px;
  font-weight: 600;
}

.login-page.dark .brand-mark {
  background: rgba(27, 127, 107, 0.25);
  color: #8dbbb1;
}

.login-brand h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  line-height: 1.3;
}

.input-icon {
  width: 18px;
  height: 18px;
  color: var(--fg-muted, #4f635f);
}

.login-page.dark .input-icon {
  color: #8dbbb1;
}

.login-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

.login-actions .el-button--primary {
  min-width: 96px;
}

@media (max-width: 900px) {
  .login-illustration {
    display: none;
  }

  .login-panel {
    flex: 1;
  }
}

@media (prefers-reduced-motion: reduce) {
  .theme-toggle {
    transition: none;
  }
}
</style>
