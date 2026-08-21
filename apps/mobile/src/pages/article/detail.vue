<script lang="ts" setup>
import type { ISpecialArticle } from '@/api/types/special'
import { ARTICLE_CATEGORY_MAP } from '@/api/types/special'
import { getArticleDetail } from '@/api/special'

definePage({
  style: {
    navigationBarTitleText: '资讯详情',
  },
})

const article = ref<ISpecialArticle | null>(null)
const loading = ref(true)
const articleId = ref('')

const categoryLabel = computed(() => {
  if (!article.value?.category)
    return ''
  return ARTICLE_CATEGORY_MAP[article.value.category] || article.value.category
})

const publishDate = computed(() => {
  const value = article.value?.publishTime || article.value?.createTime
  return value ? value.slice(0, 10) : ''
})

async function loadDetail() {
  if (!articleId.value) {
    loading.value = false
    article.value = null
    return
  }
  loading.value = true
  try {
    article.value = await getArticleDetail(articleId.value)
  }
  catch (e) {
    console.error(e)
    article.value = null
  }
  finally {
    loading.value = false
  }
}

function goBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
    return
  }
  uni.navigateTo({ url: '/pages/article/list' })
}

onLoad((query) => {
  articleId.value = String(query?.id || '')
  loadDetail()
})
</script>

<template>
  <view class="detail-page min-h-screen bg-[#F4F7F6]">
    <fg-skeleton-block v-if="loading" :rows="1" />
    <fg-empty-state
      v-else-if="!article"
      title="暂无该资讯"
      description="资讯可能已下架，返回列表看看其他内容"
      action-text="返回列表"
      @action="goBack"
    />
    <template v-else>
      <image
        v-if="article.coverUrl"
        :src="article.coverUrl"
        mode="aspectFill"
        class="detail-cover w-full"
      />
      <view class="fg-surface-card mx-3 mt-3 p-4">
        <view v-if="categoryLabel" class="category-tag">
          {{ categoryLabel }}
        </view>
        <view class="mt-2 text-xl font-semibold leading-snug text-[#1C2B28]">
          {{ article.title }}
        </view>
        <view class="mt-3 flex items-center justify-between text-xs text-muted">
          <text v-if="publishDate">{{ publishDate }}</text>
          <text>{{ article.viewCount || 0 }} 次浏览</text>
        </view>
        <view v-if="article.summary" class="mt-4 text-base leading-relaxed text-[#1C2B28]">
          {{ article.summary }}
        </view>
      </view>

      <view class="fg-surface-card mx-3 mt-3 mb-6 p-4">
        <view class="section-title">
          正文
        </view>
        <rich-text
          v-if="article.content"
          class="article-content text-sm leading-relaxed text-[#1C2B28]"
          :nodes="article.content"
        />
        <view v-else class="text-sm text-muted">
          暂无正文内容
        </view>
      </view>
    </template>
  </view>
</template>

<style scoped lang="scss">
.detail-cover {
  height: 180px;
}
.category-tag {
  display: inline-flex;
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-size: 12px;
}
.section-title {
  margin-bottom: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-ink);
}
.article-content :deep(p) {
  margin-bottom: 12px;
}
</style>
