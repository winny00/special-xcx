<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  listAppointments,
  updateAppointment,
  type SpecialAppointment,
} from '@/api/special'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const tableData = ref<SpecialAppointment[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const query = reactive({
  contactName: '',
  appointStatus: '' as number | '',
  userId: '',
})

function syncUserIdFromRoute() {
  const uid = route.query.userId
  query.userId = typeof uid === 'string' ? uid : ''
}

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

const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '已联系', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 },
]

async function fetchList() {
  loading.value = true
  try {
    const res = await listAppointments({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      contactName: query.contactName || undefined,
      appointStatus: query.appointStatus === '' ? undefined : query.appointStatus,
      userId: query.userId || undefined,
    })
    tableData.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  pageNum.value = 1
  fetchList()
}

function handleReset() {
  query.contactName = ''
  query.appointStatus = ''
  query.userId = ''
  if (route.query.userId) {
    router.replace({ path: '/appointment' })
    return
  }
  handleQuery()
}

async function handleStatusChange(row: SpecialAppointment, appointStatus: number) {
  await updateAppointment({ id: row.id, appointStatus })
  ElMessage.success('状态已更新')
  fetchList()
}

function handlePageChange(page: number) {
  pageNum.value = page
  fetchList()
}

watch(() => route.query.userId, () => {
  syncUserIdFromRoute()
  pageNum.value = 1
  fetchList()
})

onMounted(() => {
  syncUserIdFromRoute()
  fetchList()
})
</script>

<template>
  <div>
    <div class="search-card">
      <el-tag v-if="query.userId" type="success" effect="light" closable @close="handleReset">
        已按家长筛选
      </el-tag>
      <el-input v-model="query.contactName" clearable placeholder="联系人" style="width: 220px" @keyup.enter="handleQuery" />
      <el-select v-model="query.appointStatus" clearable placeholder="状态" style="width: 140px">
        <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <el-empty v-if="!loading && tableData.length === 0" description="暂无数据" />
      <template v-else>
        <el-table v-loading="loading" :border="false" :data="tableData">
        <el-table-column prop="id" label="ID" min-width="180" show-overflow-tooltip />
        <el-table-column prop="resourceTitle" label="资源" min-width="160" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="childAge" label="儿童年龄" width="90" />
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column prop="appointStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType[row.appointStatus ?? 0]" effect="light">
              {{ statusMap[row.appointStatus ?? 0] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(cmd: number) => handleStatusChange(row, cmd)">
              <el-button link type="primary">更新状态</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="opt in statusOptions"
                    :key="opt.value"
                    :command="opt.value"
                    :disabled="row.appointStatus === opt.value"
                  >
                    {{ opt.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
</template>
