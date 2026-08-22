<script lang="ts" setup>
import type { IMobileProfile } from '@/api/me'
import { getMyProfile } from '@/api/me'
import { switchCurrentRole } from '@/api/login'
import { storeToRefs } from 'pinia'
import { APPOINTMENTS_PAGE, BIND_PHONE_PAGE, LOGIN_PAGE, TEACHER_PROFILE_PAGE } from '@/router/config'
import { useUserStore } from '@/store'
import { useTokenStore } from '@/store/token'
import { canSwitchIdentity, isPhoneBound, isTeacherRole, readCachedRole, roleTagLabel, writeCachedRole } from '@/utils/current-role'
import type { SpecialRoleKey } from '@/utils/current-role'

definePage({
  style: {
    navigationBarTitleText: '我的',
  },
})

const userStore = useUserStore()
const tokenStore = useTokenStore()
const { userInfo } = storeToRefs(userStore)

const profile = ref<IMobileProfile | null>(null)
const currentRole = ref(readCachedRole())

const displayName = computed(() => {
  if (!tokenStore.hasLogin)
    return '未登录'
  return profile.value?.nickname || userInfo.value.nickname || userInfo.value.username || '已登录用户'
})

const displayHint = computed(() => {
  if (!tokenStore.hasLogin)
    return '登录后可预约咨询、查看个人信息'
  if (profile.value?.phone)
    return profile.value.phone
  return userInfo.value.username ? `账号 ${userInfo.value.username}` : '欢迎回来'
})

const avatarUrl = computed(() => profile.value?.avatar || userInfo.value.avatar)

const roleLabel = computed(() => {
  if (!tokenStore.hasLogin)
    return ''
  return roleTagLabel(profile.value?.currentRole || currentRole.value)
})

const ownedRoles = computed(() => profile.value?.roles || userInfo.value.roles || [])

const showSwitch = computed(() => tokenStore.hasLogin && canSwitchIdentity(ownedRoles.value))

const isTeacher = computed(() => tokenStore.hasLogin && isTeacherRole(profile.value?.currentRole || currentRole.value))

const phoneBound = computed(() => isPhoneBound(profile.value?.phoneBound ?? userInfo.value.phoneBound))

async function loadProfile() {
  if (!tokenStore.hasLogin) {
    profile.value = null
    currentRole.value = readCachedRole()
    return
  }
  try {
    profile.value = await getMyProfile()
    currentRole.value = (profile.value.currentRole as SpecialRoleKey | undefined) || readCachedRole()
  }
  catch (e) {
    console.error('加载资料失败', e)
  }
}

function handleLogin() {
  uni.navigateTo({ url: LOGIN_PAGE })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        useTokenStore().logout()
        profile.value = null
        currentRole.value = ''
        uni.showToast({ title: '退出登录成功', icon: 'success' })
      }
    },
  })
}

function requirePhoneThen(url: string) {
  if (!tokenStore.hasLogin) {
    handleLogin()
    return
  }
  if (!phoneBound.value) {
    uni.navigateTo({
      url: `${BIND_PHONE_PAGE}?redirect=${encodeURIComponent(url)}`,
    })
    return
  }
  uni.navigateTo({ url })
}

function goAppointments() {
  requirePhoneThen(APPOINTMENTS_PAGE)
}

function goTeacherProfile() {
  requirePhoneThen(TEACHER_PROFILE_PAGE)
}

function goAbout() {
  uni.navigateTo({ url: '/pages/about/about' })
}

function handleSwitchIdentity() {
  uni.showActionSheet({
    itemList: ['家长', '老师'],
    success: async (res) => {
      const roleKey: SpecialRoleKey = res.tapIndex === 1 ? 'special_teacher' : 'special_parent'
      if (roleKey === (profile.value?.currentRole || currentRole.value)) {
        uni.switchTab({ url: '/pages/index/index' })
        return
      }
      try {
        await switchCurrentRole(roleKey)
        writeCachedRole(roleKey)
        currentRole.value = roleKey
        uni.switchTab({ url: '/pages/index/index' })
      }
      catch (error) {
        console.error('切换身份失败', error)
      }
    },
  })
}

onShow(() => {
  currentRole.value = readCachedRole()
  loadProfile()
})
</script>

<template>
  <view class="min-h-screen bg-[#F4F7F6] pb-safe">
    <fg-profile-card
      :display-name="displayName"
      :hint="displayHint"
      :logged-in="tokenStore.hasLogin"
      :avatar-url="avatarUrl"
      :role-label="roleLabel"
      @login="handleLogin"
    />

    <view class="menu-card fg-surface-card mx-3 -mt-2 overflow-hidden">
      <template v-if="isTeacher">
        <view class="menu-item fg-tap-active" @click="goTeacherProfile">
          <view class="flex items-center">
            <text class="i-carbon-user menu-icon" />
            <text class="text-base text-[#1C2B28]">老师资料</text>
          </view>
          <text class="i-carbon-chevron-right text-muted" />
        </view>
        <view class="menu-divider" />
        <view class="menu-item fg-tap-active" @click="goAppointments">
          <view class="flex items-center">
            <text class="i-carbon-calendar menu-icon" />
            <text class="text-base text-[#1C2B28]">收到的预约</text>
          </view>
          <text class="i-carbon-chevron-right text-muted" />
        </view>
      </template>
      <template v-else>
        <view class="menu-item fg-tap-active" @click="goAppointments">
          <view class="flex items-center">
            <text class="i-carbon-calendar menu-icon" />
            <text class="text-base text-[#1C2B28]">我的预约</text>
          </view>
          <text class="i-carbon-chevron-right text-muted" />
        </view>
      </template>
      <view v-if="showSwitch" class="menu-divider" />
      <view v-if="showSwitch" class="menu-item fg-tap-active" @click="handleSwitchIdentity">
        <view class="flex items-center">
          <text class="i-carbon-renew menu-icon" />
          <text class="text-base text-[#1C2B28]">切换身份</text>
        </view>
        <text class="i-carbon-chevron-right text-muted" />
      </view>
      <view class="menu-divider" />
      <view class="menu-item fg-tap-active" @click="goAbout">
        <view class="flex items-center">
          <text class="i-carbon-information menu-icon" />
          <text class="text-base text-[#1C2B28]">关于</text>
        </view>
        <text class="i-carbon-chevron-right text-muted" />
      </view>
    </view>

    <view v-if="tokenStore.hasLogin" class="mx-3 mt-6">
      <view class="logout-btn fg-surface-card fg-tap-active" @click="handleLogout">
        退出登录
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.menu-card {
  margin-top: -8px;
}
.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 52px;
  padding: 0 16px;
}
.menu-icon {
  margin-right: 12px;
  color: var(--color-primary);
  font-size: 18px;
}
.menu-divider {
  height: 1px;
  margin: 0 16px;
  background: var(--color-canvas);
}
.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 14px;
  text-align: center;
  font-size: 16px;
  color: #c45656;
}
</style>
