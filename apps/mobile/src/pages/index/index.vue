<script lang="ts" setup>
import type { ISpecialArticle, ISpecialResource } from '@/api/types/special'
import { ARTICLE_CATEGORY_MAP } from '@/api/types/special'
import { RESOURCE_CATEGORIES } from '@/api/types/special'
import { getArticleList, getResourceList } from '@/api/special'
import { getMyAppointments, getMyTeacherProfile } from '@/api/me'
import { useCapsuleNav } from '@/hooks/useCapsuleNav'
import { APPOINTMENTS_PAGE, BIND_PHONE_PAGE, LOGIN_PAGE } from '@/router/config'
import { useUserStore } from '@/store'
import { useTokenStore } from '@/store/token'
import { isPhoneBound, isTeacherRole, readCachedRole } from '@/utils/current-role'
import { openResourceList } from '@/utils/resource-nav'

definePage({
  type: 'home',
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '特教资源平台',
  },
})

const { headerPaddingStyle, capsuleRowStyle } = useCapsuleNav()
const tokenStore = useTokenStore()
const userStore = useUserStore()

const loading = ref(true)
const resources = ref<ISpecialResource[]>([])
const articles = ref<ISpecialArticle[]>([])
const articlesLoading = ref(true)
const activeCategory = ref('')
const keyword = ref('')
const currentRole = ref(readCachedRole())
const teacherName = ref('')
const pendingCount = ref(0)
const teacherLoading = ref(false)

const isTeacherHome = computed(() => tokenStore.hasLogin && isTeacherRole(currentRole.value))

const shortcuts = [
  { label: '课程', image: '/static/shortcuts/course.png', action: 'course' },
  { label: '老师', image: '/static/shortcuts/teacher.png', action: 'teacher' },
  { label: '找机构', image: '/static/shortcuts/org.png', action: 'org' },
  { label: '预约咨询', image: '/static/shortcuts/appoint.png', action: 'appoint' },
]

async function loadResources() {
  loading.value = true
  try {
    const res = await getResourceList({
      pageNum: 1,
      pageSize: 10,
      category: activeCategory.value || undefined,
    })
    resources.value = res.rows || []
  }
  catch (e) {
    console.error('加载资源失败', e)
    resources.value = []
  }
  finally {
    loading.value = false
  }
}

async function loadArticles() {
  articlesLoading.value = true
  try {
    const res = await getArticleList({ pageNum: 1, pageSize: 5 })
    articles.value = res.rows || []
  }
  catch (e) {
    console.error('加载资讯失败', e)
    articles.value = []
  }
  finally {
    articlesLoading.value = false
  }
}

function selectCategory(cat: string) {
  activeCategory.value = activeCategory.value === cat ? '' : cat
  loadResources()
}

function goResourceList(query: Record<string, string> = {}) {
  openResourceList({
    keyword: query.keyword,
    resourceType: query.resourceType,
  })
}

function goSearch() {
  goResourceList({ keyword: keyword.value.trim() })
}

function goShortcut(action: string) {
  if (action === 'org') {
    uni.switchTab({ url: '/pages/organization/list' })
    return
  }
  if (action === 'teacher') {
    uni.navigateTo({ url: '/pages/teacher/list' })
    return
  }
  if (action === 'appoint') {
    goResourceList()
    return
  }
  goResourceList({ resourceType: action })
}

function goDetail(id: string | number) {
  uni.navigateTo({ url: `/pages/resource/detail?id=${String(id)}` })
}

function goArticleList() {
  uni.navigateTo({ url: '/pages/article/list' })
}

function goArticleDetail(id: string | number) {
  uni.navigateTo({ url: `/pages/article/detail?id=${String(id)}` })
}

function formatArticleDate(value?: string) {
  return value ? value.slice(0, 10) : ''
}

async function loadTeacherHome() {
  teacherLoading.value = true
  teacherName.value = userStore.userInfo.nickname || userStore.userInfo.username || '老师'
  pendingCount.value = 0
  try {
    const profile = await getMyTeacherProfile()
    if (profile?.name) {
      teacherName.value = profile.name
    }
  }
  catch (e) {
    console.error('加载老师资料失败', e)
  }
  try {
    const res = await getMyAppointments({ pageNum: 1, pageSize: 50 })
    pendingCount.value = (res.rows || []).filter(item => (item.appointStatus ?? 0) === 0).length
  }
  catch (e) {
    console.error('加载预约失败', e)
  }
  finally {
    teacherLoading.value = false
  }
}

function goTeacherAppointments() {
  if (!tokenStore.hasLogin) {
    uni.navigateTo({ url: LOGIN_PAGE })
    return
  }
  if (!isPhoneBound(userStore.userInfo.phoneBound)) {
    uni.navigateTo({
      url: `${BIND_PHONE_PAGE}?redirect=${encodeURIComponent(APPOINTMENTS_PAGE)}`,
    })
    return
  }
  uni.navigateTo({ url: APPOINTMENTS_PAGE })
}

onShow(() => {
  currentRole.value = readCachedRole()
  if (isTeacherHome.value) {
    loadTeacherHome()
    return
  }
  if (resources.value.length === 0) {
    loadResources()
  }
  if (articles.value.length === 0) {
    loadArticles()
  }
})
</script>

<template>
  <view class="min-h-screen bg-[#F4F7F6] pb-safe">
    <view class="fg-hero-gradient px-4 pb-4" :style="headerPaddingStyle">
      <view :style="capsuleRowStyle">
        <view class="text-lg font-semibold text-[#1C2B28]">
          {{ isTeacherHome ? `你好，${teacherName || '老师'}` : '你好，需要什么帮助？' }}
        </view>
        <view class="mt-1 text-sm text-muted">
          {{ isTeacherHome ? '查看待处理预约，管理自己的老师资料' : '为特需家庭对接可靠课程、老师与机构' }}
        </view>
      </view>
      <view
        v-if="!isTeacherHome"
        class="fg-surface-card mt-4 flex min-h-11 items-center px-4 fg-tap-active"
        @click="goSearch"
      >
        <text class="i-carbon-search mr-2 text-muted" />
        <input
          v-model="keyword"
          class="h-6 flex-1 text-base text-[#1C2B28]"
          placeholder="搜索课程、老师、机构"
          confirm-type="search"
          @confirm="goSearch"
          @click.stop
        >
      </view>
    </view>

    <view v-if="isTeacherHome" class="mx-3 mt-3">
      <fg-skeleton-block v-if="teacherLoading" :rows="1" />
      <view v-else class="teacher-card fg-surface-card p-4">
        <view class="text-base font-semibold text-[#1C2B28]">
          {{ teacherName || '老师' }}
        </view>
        <view class="mt-2 text-base text-muted">
          待处理预约 {{ pendingCount }} 条
        </view>
        <view
          class="teacher-cta fg-tap-active mt-4"
          @click="goTeacherAppointments"
        >
          查看收到的预约
        </view>
      </view>
    </view>

    <template v-else>

    <view class="shortcut-row mx-3 -mt-1 fg-surface-card px-2 py-4">
      <view
        v-for="item in shortcuts"
        :key="item.action"
        class="shortcut-item fg-tap-active"
        @click="goShortcut(item.action)"
      >
        <image
          class="shortcut-icon"
          :src="item.image"
          mode="aspectFit"
        />
        <text class="shortcut-label">{{ item.label }}</text>
      </view>
    </view>

    <view class="mt-4 px-3">
      <scroll-view scroll-x class="whitespace-nowrap">
        <view
          class="category-pill fg-tap-active"
          :class="activeCategory === '' ? 'category-pill--active' : 'category-pill--idle'"
          @click="selectCategory('')"
        >
          全部领域
        </view>
        <view
          v-for="cat in RESOURCE_CATEGORIES"
          :key="cat"
          class="category-pill fg-tap-active"
          :class="activeCategory === cat ? 'category-pill--active' : 'category-pill--idle'"
          @click="selectCategory(cat)"
        >
          {{ cat }}
        </view>
      </scroll-view>
    </view>

    <view class="mx-3 mt-4">
      <view class="mb-3 flex items-center justify-between">
        <text class="text-base font-semibold text-[#1C2B28]">政策资讯</text>
        <text class="more-link fg-tap-active" @click="goArticleList">查看更多</text>
      </view>

      <fg-skeleton-block v-if="articlesLoading" :rows="2" />
      <fg-empty-state
        v-else-if="articles.length === 0"
        title="暂无政策资讯"
        description="平台正在整理政策与家长指南"
        action-text="浏览资讯列表"
        @action="goArticleList"
      />
      <view v-else class="flex flex-col gap-3">
        <view
          v-for="item in articles"
          :key="String(item.id)"
          class="article-row fg-surface-card fg-tap-active flex gap-3 p-3"
          @click="goArticleDetail(item.id)"
        >
          <image
            v-if="item.coverUrl"
            :src="item.coverUrl"
            mode="aspectFill"
            class="article-thumb shrink-0"
          />
          <view v-else class="article-thumb article-thumb--placeholder shrink-0">
            <text class="i-carbon-document text-lg" />
          </view>
          <view class="min-w-0 flex-1">
            <text class="line-clamp-2 text-sm font-medium leading-snug text-[#1C2B28]">
              {{ item.title }}
            </text>
            <view class="mt-2 flex items-center gap-2 text-xs text-muted">
              <text v-if="item.category">{{ ARTICLE_CATEGORY_MAP[item.category] || item.category }}</text>
              <text>{{ formatArticleDate(item.publishTime || item.createTime) }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="mx-3 mt-4">
      <view class="mb-3 flex items-center justify-between">
        <text class="text-base font-semibold text-[#1C2B28]">推荐资源</text>
        <text class="more-link fg-tap-active" @click="goResourceList()">查看更多</text>
      </view>

      <fg-skeleton-block v-if="loading" :rows="2" />
      <fg-empty-state
        v-else-if="resources.length === 0"
        title="暂无推荐资源"
        description="可以先浏览全部资源，或换一个干预领域试试"
        action-text="浏览全部资源"
        @action="goResourceList()"
      />
      <view v-else class="flex flex-col gap-3">
        <view v-for="item in resources" :key="String(item.id)" @click="goDetail(item.id)">
          <fg-resource-card :item="item" />
        </view>
      </view>
    </view>
    </template>
  </view>
</template>

<style scoped lang="scss">
.shortcut-row {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  box-sizing: border-box;
}
.shortcut-item {
  display: flex;
  flex: 1 1 0;
  width: 25%;
  min-width: 0;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  text-align: center;
  min-height: 44px;
  box-sizing: border-box;
}
.shortcut-icon {
  width: 56px;
  height: 56px;
  margin-bottom: 6px;
  flex-shrink: 0;
}
.shortcut-label {
  display: block;
  width: 100%;
  min-height: 18px;
  font-size: 13px;
  line-height: 18px;
  text-align: center;
  color: #1c2b28;
}
.category-pill {
  display: inline-flex;
  align-items: center;
  min-height: 44px;
  margin-right: 8px;
  padding: 0 14px;
  border-radius: 999px;
  font-size: 14px;
}
.category-pill--active {
  background: var(--color-primary);
  color: #fff;
}
.category-pill--idle {
  background: var(--color-surface);
  color: var(--color-muted);
  border: 1px solid var(--color-primary-soft);
}
.more-link {
  display: inline-flex;
  min-height: 44px;
  align-items: center;
  font-size: 14px;
  color: var(--color-primary);
}
.article-thumb {
  width: 72px;
  height: 72px;
  border-radius: 12px;
}
.article-thumb--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary-soft);
  color: var(--color-primary);
}
.teacher-cta {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  border-radius: 12px;
  background: var(--color-primary);
  font-size: 16px;
  color: #fff;
}
</style>
