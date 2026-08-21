<script lang="ts" setup>
import type { ISpecialArticle, ISpecialResource } from '@/api/types/special'
import { ARTICLE_CATEGORY_MAP } from '@/api/types/special'
import { RESOURCE_CATEGORIES } from '@/api/types/special'
import { getArticleList, getResourceList } from '@/api/special'
import { useCapsuleNav } from '@/hooks/useCapsuleNav'
import { openResourceList } from '@/utils/resource-nav'

definePage({
  type: 'home',
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '特教资源平台',
  },
})

const { headerPaddingStyle, capsuleRowStyle } = useCapsuleNav()

const loading = ref(true)
const resources = ref<ISpecialResource[]>([])
const articles = ref<ISpecialArticle[]>([])
const articlesLoading = ref(true)
const activeCategory = ref('')
const keyword = ref('')

const shortcuts = [
  { label: '课程', icon: 'i-carbon-education', action: 'course' },
  { label: '老师', icon: 'i-carbon-user', action: 'teacher' },
  { label: '找机构', icon: 'i-carbon-building', action: 'org' },
  { label: '预约咨询', icon: 'i-carbon-calendar', action: 'appoint' },
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
  if (action === 'appoint') {
    goResourceList()
    return
  }
  goResourceList({ resourceType: action })
}

function goDetail(id: string | number) {
  uni.navigateTo({ url: `/pages/resource/detail?id=${id}` })
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

onLoad(() => {
  loadResources()
  loadArticles()
})
</script>

<template>
  <view class="min-h-screen bg-[#F4F7F6] pb-safe">
    <view class="fg-hero-gradient px-4 pb-4" :style="headerPaddingStyle">
      <view :style="capsuleRowStyle">
        <view class="text-lg font-semibold text-[#1C2B28]">
          你好，需要什么帮助？
        </view>
        <view class="mt-1 text-sm text-muted">
          为特需家庭对接可靠课程、老师与机构
        </view>
      </view>
      <view class="fg-surface-card mt-4 flex min-h-11 items-center px-4 fg-tap-active" @click="goSearch">
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

    <view class="mx-3 -mt-1 flex fg-surface-card px-2 py-4">
      <view
        v-for="item in shortcuts"
        :key="item.action"
        class="shortcut-item fg-tap-active"
        @click="goShortcut(item.action)"
      >
        <view class="shortcut-icon">
          <text :class="item.icon" />
        </view>
        <text class="text-xs text-[#1C2B28]">{{ item.label }}</text>
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
  </view>
</template>

<style scoped lang="scss">
.shortcut-item {
  display: inline-flex;
  width: 25%;
  flex-direction: column;
  align-items: center;
  min-height: 44px;
}
.shortcut-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  margin-bottom: 6px;
  border-radius: 50%;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-size: 20px;
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
</style>
