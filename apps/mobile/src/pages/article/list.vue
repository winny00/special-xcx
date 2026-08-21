<script lang="ts" setup>
import type { ISpecialArticle } from '@/api/types/special'
import { ARTICLE_CATEGORIES, ARTICLE_CATEGORY_MAP } from '@/api/types/special'
import { getArticleList } from '@/api/special'

definePage({
  style: {
    navigationBarTitleText: '政策资讯',
  },
})

const loading = ref(true)
const articles = ref<ISpecialArticle[]>([])
const activeCategory = ref('')
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)
const loadingMore = ref(false)

async function loadArticles(reset = false) {
  if (reset) {
    pageNum.value = 1
    loading.value = true
  }
  else {
    loadingMore.value = true
  }
  try {
    const res = await getArticleList({
      pageNum: pageNum.value,
      pageSize,
      category: activeCategory.value || undefined,
    })
    const rows = res.rows || []
    articles.value = reset ? rows : [...articles.value, ...rows]
    total.value = res.total || 0
  }
  catch (e) {
    console.error('加载资讯失败', e)
    if (reset)
      articles.value = []
  }
  finally {
    loading.value = false
    loadingMore.value = false
  }
}

function selectCategory(cat: string) {
  activeCategory.value = activeCategory.value === cat ? '' : cat
  loadArticles(true)
}

function goDetail(id: string | number) {
  uni.navigateTo({ url: `/pages/article/detail?id=${String(id)}` })
}

function loadMore() {
  if (loadingMore.value || articles.value.length >= total.value)
    return
  pageNum.value += 1
  loadArticles(false)
}

function formatDate(value?: string) {
  if (!value)
    return ''
  return value.slice(0, 10)
}

onLoad(() => {
  loadArticles(true)
})
</script>

<template>
  <view class="min-h-screen bg-[#F4F7F6] pb-safe">
    <view class="px-3 pt-3">
      <scroll-view scroll-x class="whitespace-nowrap">
        <view
          class="category-pill fg-tap-active"
          :class="activeCategory === '' ? 'category-pill--active' : 'category-pill--idle'"
          @click="selectCategory('')"
        >
          全部
        </view>
        <view
          v-for="cat in ARTICLE_CATEGORIES"
          :key="cat"
          class="category-pill fg-tap-active"
          :class="activeCategory === cat ? 'category-pill--active' : 'category-pill--idle'"
          @click="selectCategory(cat)"
        >
          {{ ARTICLE_CATEGORY_MAP[cat] }}
        </view>
      </scroll-view>
    </view>

    <view class="mx-3 mt-4">
      <fg-skeleton-block v-if="loading" :rows="3" />
      <fg-empty-state
        v-else-if="articles.length === 0"
        title="暂无资讯"
        description="稍后再来看看，或切换其他分类"
      />
      <view v-else class="flex flex-col gap-3">
        <view
          v-for="item in articles"
          :key="String(item.id)"
          class="article-card fg-surface-card fg-tap-active overflow-hidden"
          @click="goDetail(item.id)"
        >
          <image
            v-if="item.coverUrl"
            :src="item.coverUrl"
            mode="aspectFill"
            class="article-cover w-full"
          />
          <view class="p-4">
            <view class="flex items-start justify-between gap-2">
              <text class="flex-1 text-base font-semibold leading-snug text-[#1C2B28]">
                {{ item.title }}
              </text>
              <text v-if="item.category" class="category-tag shrink-0">
                {{ ARTICLE_CATEGORY_MAP[item.category] || item.category }}
              </text>
            </view>
            <text v-if="item.summary" class="mt-2 line-clamp-2 text-sm leading-relaxed text-muted">
              {{ item.summary }}
            </text>
            <view class="mt-3 flex items-center justify-between text-xs text-muted">
              <text>{{ formatDate(item.publishTime || item.createTime) }}</text>
              <text>{{ item.viewCount || 0 }} 次浏览</text>
            </view>
          </view>
        </view>

        <view
          v-if="articles.length < total"
          class="load-more fg-tap-active"
          @click="loadMore"
        >
          {{ loadingMore ? '加载中…' : '加载更多' }}
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
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
.article-cover {
  height: 140px;
}
.category-tag {
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-size: 11px;
}
.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  margin-bottom: 16px;
  font-size: 14px;
  color: var(--color-primary);
}
</style>
