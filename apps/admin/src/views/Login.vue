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

interface LoginVo {
  access_token?: string
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
    const redirect = (route.query.redirect as string) || '/resource'
    router.push(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-brand">
      <h1>特教资源平台</h1>
      <ul>
        <li>发布与维护特教课程、老师与评估资源</li>
        <li>审核机构与学校入驻信息</li>
        <li>跟进家庭预约咨询申请</li>
      </ul>
    </div>
    <div class="login-form">
      <div class="form-card">
        <h2>工作台登录</h2>
        <p class="form-hint">使用管理员账号进入后台</p>
        <el-form @submit.prevent="handleLogin">
          <el-form-item>
            <el-input v-model="form.username" size="large" placeholder="用户名" autocomplete="username" />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="form.password"
              size="large"
              type="password"
              show-password
              placeholder="密码"
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-button type="primary" size="large" class="submit-btn" native-type="submit" :loading="loading">
            登录
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
}
.login-brand {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 64px 72px;
  color: #fff;
  background: linear-gradient(160deg, #1b7f6b 0%, #166656 100%);
}
.login-brand h1 {
  margin: 0 0 32px;
  font-size: 36px;
  font-weight: 600;
}
.login-brand ul {
  margin: 0;
  padding: 0;
  list-style: none;
}
.login-brand li {
  position: relative;
  margin-bottom: 14px;
  padding-left: 16px;
  font-size: 15px;
  line-height: 1.6;
  opacity: 0.92;
}
.login-brand li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 10px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #e7f4f0;
}
.login-form {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f4f7f6;
}
.form-card {
  width: 400px;
  padding: 40px 36px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(28, 43, 40, 0.06);
}
.form-card h2 {
  margin: 0;
  font-size: 22px;
  color: #1c2b28;
}
.form-hint {
  margin: 8px 0 28px;
  font-size: 13px;
  color: #4F635F;
}
.submit-btn {
  width: 100%;
}
@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }
  .login-brand {
    display: none;
  }
}
</style>
