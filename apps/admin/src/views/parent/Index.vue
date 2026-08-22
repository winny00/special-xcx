<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  getParent,
  listParents,
  type SpecialParent,
  type SpecialParentDetail,
} from '@/api/special'

const router = useRouter()
const loading = ref(false)
const tableData = ref<SpecialParent[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const query = reactive({
  keyword: '',
})

const drawerVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<SpecialParentDetail | null>(null)

const statusMap: Record<number, string> = {
  0: '待处理',
  1: '已联系',
  2: '已完成',
  3: '已取消',
}

const statusTagType: Record<number, 'info' | 'success' | 'warning' | 'primary'> = {
  0: 'info',
  1: 'primary',
  2: 'success',
  3: 'warning',
}

function parentId(row: SpecialParent) {
  return String(row.userId)
}

async function fetchList() {
  loading.value = true
  try {
    const res = await listParents({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: query.keyword || undefined,
    })
    tableData.value = res.rows
    total.value = res.total
  }
  finally {
    loading.value = false
  }
}

function handleQuery() {
  pageNum.value = 1
  fetchList()
}

function handleReset() {
  query.keyword = ''
  handleQuery()
}

function handlePageChange(page: number) {
  pageNum.value = page
  fetchList()
}

async function handleView(row: SpecialParent) {
  drawerVisible.value = true
  detail.value = null
  detailLoading.value = true
  try {
    detail.value = await getParent(parentId(row))
  }
  finally {
    detailLoading.value = false
  }
}

function goAppointments() {
  if (!detail.value)
    return
  drawerVisible.value = false
  router.push({ path: '/appointment', query: { userId: String(detail.value.userId) } })
}

onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-card">
      <el-input
        v-model="query.keyword"
        clearable
        placeholder="昵称 / 手机号"
        style="width: 240px"
        @keyup.enter="handleQuery"
      />
      <el-button type="primary" @click="handleQuery">
        查询
      </el-button>
      <el-button @click="handleReset">
        重置
      </el-button>
    </div>

    <div class="table-card">
      <el-empty v-if="!loading && tableData.length === 0" description="暂无家长用户" />
      <template v-else>
        <el-table v-loading="loading" :border="false" :data="tableData">
          <el-table-column prop="nickName" label="昵称" min-width="140" show-overflow-tooltip />
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column prop="createTime" label="注册时间" width="180" />
          <el-table-column prop="appointmentCount" label="预约次数" width="110" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleQuery"
        />
      </template>
    </div>
  </div>

  <el-drawer v-model="drawerVisible" title="家长详情" size="520px" destroy-on-close>
    <div v-loading="detailLoading">
      <template v-if="detail">
        <div class="parent-profile">
          <el-avatar :size="56" :src="detail.avatar || undefined">
            {{ (detail.nickName || '家').slice(0, 1) }}
          </el-avatar>
          <div>
            <div class="parent-name">
              {{ detail.nickName || '—' }}
            </div>
            <div class="parent-meta">
              注册时间 {{ detail.createTime || '—' }}
            </div>
            <div class="parent-phone">
              {{ detail.phone || '未绑定手机' }}
            </div>
          </div>
        </div>

        <div class="drawer-head">
          <h3>最近预约</h3>
          <el-button link type="primary" @click="goAppointments">
            查看全部预约
          </el-button>
        </div>
        <el-empty v-if="!detail.appointments?.length" description="暂无预约" />
        <el-table v-else :data="detail.appointments" :border="false" size="small">
          <el-table-column prop="resourceTitle" label="资源" min-width="140" show-overflow-tooltip />
          <el-table-column prop="contactName" label="联系人" width="80" />
          <el-table-column label="状态" width="88">
            <template #default="{ row }">
              <el-tag :type="statusTagType[row.appointStatus ?? 0]" effect="light">
                {{ statusMap[row.appointStatus ?? 0] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="160" />
        </el-table>
      </template>
    </div>
  </el-drawer>
</template>

<style scoped>
.parent-profile {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  background: var(--fg-primary-soft);
  border-radius: 12px;
}

.parent-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--fg-ink, #1c2b28);
}

.parent-meta,
.parent-phone {
  margin-top: 4px;
  font-size: 14px;
  color: var(--fg-muted);
}

.parent-phone {
  color: var(--fg-ink, #1c2b28);
}

.drawer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.drawer-head h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}
</style>
