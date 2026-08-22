<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addTeacher,
  deleteTeachers,
  listOrganizations,
  listTeachers,
  updateTeacher,
  type SpecialOrganization,
  type SpecialTeacher,
} from '@/api/special'
import FgCoverUpload from '@/components/FgCoverUpload.vue'
import { buildTeacherPayload } from '@/utils/teacher-payload'

const route = useRoute()
const loading = ref(false)
const tableData = ref<SpecialTeacher[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const query = reactive({
  name: '',
  status: '' as number | '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增老师')
const orgOptions = ref<{ id: string, name: string }[]>([])
const form = reactive<SpecialTeacher>({
  name: '',
  title: '',
  specialties: '',
  qualification: '',
  avatarUrl: '',
  certImageUrl: '',
  orgId: '',
  intro: '',
  status: 0,
  phone: '',
  initPassword: '',
  userId: '',
})

function orgOptionId(org: SpecialOrganization) {
  return org.id == null ? '' : String(org.id)
}

function mergeOrgOption(id: string, name?: string) {
  if (!id || orgOptions.value.some(item => item.id === id))
    return
  orgOptions.value = [...orgOptions.value, { id, name: name || `机构 ${id}` }]
}

async function fetchOrgOptions() {
  try {
    const res = await listOrganizations({
      pageNum: 1,
      pageSize: 200,
      auditStatus: 1,
      status: 1,
    })
    orgOptions.value = res.rows
      .map(org => ({ id: orgOptionId(org), name: org.name }))
      .filter(item => item.id)
  }
  catch {
    orgOptions.value = []
  }
}

const statusMap: Record<number, { label: string, type: 'info' | 'success' | 'warning' }> = {
  0: { label: '待审', type: 'info' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'warning' },
}

function rowId(row: SpecialTeacher) {
  return String(row.id)
}

async function fetchList() {
  loading.value = true
  try {
    const res = await listTeachers({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      name: query.name || undefined,
      status: query.status === '' ? undefined : query.status,
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
  query.name = ''
  query.status = ''
  handleQuery()
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    userId: '',
    name: '',
    title: '',
    specialties: '',
    qualification: '',
    avatarUrl: '',
    certImageUrl: '',
    orgId: '',
    intro: '',
    status: 0,
    phone: '',
    initPassword: '',
  })
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增老师'
  dialogVisible.value = true
}

function handleEdit(row: SpecialTeacher) {
  const orgId = row.orgId ? String(row.orgId) : ''
  Object.assign(form, {
    ...row,
    id: rowId(row),
    orgId,
    userId: row.userId ? String(row.userId) : '',
    phone: row.phone || '',
    initPassword: '',
  })
  if (orgId)
    mergeOrgOption(orgId)
  dialogTitle.value = '编辑老师'
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.name) {
    ElMessage.warning('请输入姓名')
    return
  }
  if (!form.id && !form.phone?.trim()) {
    ElMessage.warning('请填写手机号')
    return
  }
  let payload: SpecialTeacher
  try {
    payload = buildTeacherPayload(form)
  }
  catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '请选择已有机构')
    return
  }
  if (form.id) {
    await updateTeacher(payload)
    ElMessage.success('更新成功')
  }
  else {
    await addTeacher(payload)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row: SpecialTeacher) {
  await ElMessageBox.confirm('确认删除该老师档案？', '提示', { type: 'warning' })
  await deleteTeachers([rowId(row)])
  ElMessage.success('删除成功')
  fetchList()
}

watch(() => route.query.editId, (editId) => {
  if (!editId)
    return
  const row = tableData.value.find(item => String(item.id) === String(editId))
  if (row)
    handleEdit(row)
})

watch(() => route.query.phone, (phone) => {
  if (!phone || form.id)
    return
  resetForm()
  form.phone = String(phone)
  dialogTitle.value = '新增老师'
  dialogVisible.value = true
})

onMounted(async () => {
  await Promise.all([fetchList(), fetchOrgOptions()])
  const editId = route.query.editId
  if (editId) {
    const row = tableData.value.find(item => String(item.id) === String(editId))
    if (row)
      handleEdit(row)
  }
  else if (route.query.phone) {
    resetForm()
    form.phone = String(route.query.phone)
    dialogTitle.value = '新增老师'
    dialogVisible.value = true
  }
})
</script>

<template>
  <div>
    <div class="search-card">
      <el-input v-model="query.name" clearable placeholder="姓名" style="width: 200px" @keyup.enter="handleQuery" />
      <el-select v-model="query.status" clearable placeholder="状态" style="width: 140px">
        <el-option label="待审" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-button type="primary" @click="handleQuery">
        查询
      </el-button>
      <el-button @click="handleReset">
        重置
      </el-button>
      <div class="search-card__actions">
        <el-button type="primary" @click="handleAdd">
          新增
        </el-button>
      </div>
    </div>

    <div class="table-card">
      <el-empty v-if="!loading && tableData.length === 0" description="暂无老师档案" />
      <template v-else>
        <el-table v-loading="loading" :border="false" :data="tableData">
          <el-table-column label="头像" width="80">
            <template #default="{ row }">
              <el-image
                v-if="row.avatarUrl"
                :src="row.avatarUrl"
                fit="cover"
                style="width: 48px; height: 48px; border-radius: 50%"
                :preview-src-list="[row.avatarUrl]"
                preview-teleported
              />
              <span v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" min-width="120" />
          <el-table-column prop="title" label="头衔" width="140" />
          <el-table-column prop="specialties" label="擅长" min-width="160" show-overflow-tooltip />
          <el-table-column label="账号" width="110">
            <template #default="{ row }">
              <el-tag :type="row.userId ? 'success' : 'info'" effect="light">
                {{ row.userId ? '已绑账号' : '未绑' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusMap[row.status ?? 0]?.type || 'info'" effect="light">
                {{ statusMap[row.status ?? 0]?.label || '未知' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditRemark" label="审核备注" min-width="140" show-overflow-tooltip />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">
                删除
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

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
    <el-form label-width="90px">
      <el-form-item label="姓名" required>
        <el-input v-model="form.name" maxlength="100" />
      </el-form-item>
      <el-form-item label="手机号" :required="!form.id">
        <el-input v-model="form.phone" maxlength="11" placeholder="11 位手机号，作为登录账号" />
      </el-form-item>
      <el-form-item v-if="!form.id" label="初始密码" required>
        <el-input v-model="form.initPassword" type="password" show-password maxlength="32" placeholder="新建账号时必填" />
      </el-form-item>
      <el-form-item v-else-if="!form.userId" label="初始密码">
        <el-input v-model="form.initPassword" type="password" show-password maxlength="32" placeholder="补绑新账号时填写" />
      </el-form-item>
      <el-form-item label="头衔">
        <el-input v-model="form.title" maxlength="100" placeholder="如 语言干预师" />
      </el-form-item>
      <el-form-item label="擅长">
        <el-input v-model="form.specialties" maxlength="500" placeholder="逗号分隔，如 感统,语言" />
      </el-form-item>
      <el-form-item label="资质说明">
        <el-input v-model="form.qualification" type="textarea" :rows="2" maxlength="500" />
      </el-form-item>
      <el-form-item label="头像">
        <FgCoverUpload v-model="form.avatarUrl" />
      </el-form-item>
      <el-form-item label="证书图">
        <FgCoverUpload v-model="form.certImageUrl" />
      </el-form-item>
      <el-form-item label="所属机构">
        <el-select
          v-model="form.orgId"
          clearable
          filterable
          placeholder="选择已入驻机构（可选）"
          style="width: 100%"
        >
          <el-option
            v-for="org in orgOptions"
            :key="org.id"
            :label="org.name"
            :value="org.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="简介">
        <el-input v-model="form.intro" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="0">
            待审
          </el-radio>
          <el-radio :value="1">
            已通过
          </el-radio>
          <el-radio :value="2">
            已拒绝
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">
        取消
      </el-button>
      <el-button type="primary" @click="handleSubmit">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>
