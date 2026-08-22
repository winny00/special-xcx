<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listAccounts,
  resetAccountPassword,
  updateAccountRoles,
  type SpecialAccount,
} from '@/api/special'
import { ApiError } from '@/api/request'

const router = useRouter()
const loading = ref(false)
const tableData = ref<SpecialAccount[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const query = reactive({
  keyword: '',
})

const roleDialogVisible = ref(false)
const roleSaving = ref(false)
const roleForm = reactive({
  userId: '',
  parent: false,
  teacher: false,
})

const statusMap: Record<string, { label: string, type: 'success' | 'info' }> = {
  0: { label: '正常', type: 'success' },
  1: { label: '停用', type: 'info' },
}

function accountId(row: SpecialAccount) {
  return String(row.userId)
}

function roleLabel(role: string) {
  if (role === 'special_parent')
    return '家长'
  if (role === 'special_teacher')
    return '老师'
  return role
}

async function fetchList() {
  loading.value = true
  try {
    const res = await listAccounts({
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

function openRoles(row: SpecialAccount) {
  roleForm.userId = accountId(row)
  roleForm.parent = (row.roles || []).includes('special_parent')
  roleForm.teacher = (row.roles || []).includes('special_teacher')
  roleDialogVisible.value = true
}

async function saveRoles() {
  if (!roleForm.parent && !roleForm.teacher) {
    ElMessage.warning('至少保留一个角色')
    return
  }
  roleSaving.value = true
  try {
    await updateAccountRoles(roleForm.userId, {
      parent: roleForm.parent,
      teacher: roleForm.teacher,
    })
    ElMessage.success('角色已更新')
    roleDialogVisible.value = false
    fetchList()
  }
  catch (error) {
    if (error instanceof ApiError && error.message === '请先补全老师档案') {
      const phone = typeof error.data === 'object' && error.data && 'phone' in error.data
        ? String((error.data as { phone?: string }).phone || '')
        : ''
      roleDialogVisible.value = false
      router.push({ path: '/teacher', query: phone ? { phone } : {} })
    }
  }
  finally {
    roleSaving.value = false
  }
}

async function handleResetPassword(row: SpecialAccount) {
  const { value } = await ElMessageBox.prompt('请输入新密码', '重置密码', {
    inputType: 'password',
    inputPlaceholder: '至少 6 位',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValidator: (val: string) => {
      if (!val || val.trim().length < 6)
        return '请填写至少 6 位密码'
      return true
    },
  })
  await resetAccountPassword(accountId(row), String(value).trim())
  ElMessage.success('密码已重置')
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
      <el-empty v-if="!loading && tableData.length === 0" description="暂无用户账号" />
      <template v-else>
        <el-table v-loading="loading" :border="false" :data="tableData">
          <el-table-column prop="nickname" label="昵称" min-width="140" show-overflow-tooltip />
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column label="角色" min-width="160">
            <template #default="{ row }">
              <el-tag
                v-for="role in (row.roles || [])"
                :key="role"
                effect="light"
                style="margin-right: 6px"
              >
                {{ roleLabel(role) }}
              </el-tag>
              <span v-if="!(row.roles || []).length">—</span>
            </template>
          </el-table-column>
          <el-table-column label="账号状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusMap[row.status ?? '0']?.type || 'info'" effect="light">
                {{ statusMap[row.status ?? '0']?.label || '未知' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openRoles(row)">
                角色
              </el-button>
              <el-button link type="primary" @click="handleResetPassword(row)">
                重置密码
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchList"
          @size-change="handleQuery"
        />
      </template>
    </div>
  </div>

  <el-dialog v-model="roleDialogVisible" title="用户角色" width="420px" destroy-on-close>
    <p class="role-hint">
      至少保留家长或老师其中一个角色。勾选老师前请先补全老师档案。
    </p>
    <el-checkbox v-model="roleForm.parent">
      家长
    </el-checkbox>
    <el-checkbox v-model="roleForm.teacher">
      老师
    </el-checkbox>
    <template #footer>
      <el-button @click="roleDialogVisible = false">
        取消
      </el-button>
      <el-button type="primary" :loading="roleSaving" @click="saveRoles">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.role-hint {
  margin: 0 0 16px;
  font-size: 14px;
  line-height: 1.5;
  color: var(--fg-muted);
}
</style>
