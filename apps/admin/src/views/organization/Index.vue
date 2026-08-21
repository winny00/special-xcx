<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addOrganization,
  deleteOrganizations,
  getOrganization,
  listOrganizations,
  updateOrganization,
  type SpecialOrganization,
} from '@/api/special'
import FgCoverUpload from '@/components/FgCoverUpload.vue'

const route = useRoute()

const loading = ref(false)
const tableData = ref<SpecialOrganization[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const query = reactive({
  name: '',
  orgType: '',
  auditStatus: '' as number | '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增机构')
const form = reactive<SpecialOrganization>({
  name: '',
  orgType: 'school',
  coverUrl: '',
  status: 1,
  auditStatus: 0,
})

const orgTypes = [
  { label: '学校', value: 'school' },
  { label: '康复中心', value: 'rehab' },
  { label: '培训机构', value: 'training' },
  { label: '其他', value: 'other' },
]

const typeLabelMap = Object.fromEntries(orgTypes.map(item => [item.value, item.label]))

const auditStatusMap: Record<number, string> = {
  0: '待审核',
  1: '已通过',
  2: '已拒绝',
}

async function fetchList() {
  loading.value = true
  try {
    const res = await listOrganizations({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      name: query.name || undefined,
      orgType: query.orgType || undefined,
      auditStatus: query.auditStatus === '' ? undefined : query.auditStatus,
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
  query.name = ''
  query.orgType = ''
  query.auditStatus = ''
  handleQuery()
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    name: '',
    orgType: 'school',
    licenseNo: '',
    address: '',
    region: '',
    contactName: '',
    contactPhone: '',
    description: '',
    coverUrl: '',
    auditStatus: 0,
    status: 1,
  })
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增机构'
  dialogVisible.value = true
}

function handleEdit(row: SpecialOrganization) {
  Object.assign(form, { ...row })
  dialogTitle.value = '编辑机构'
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.name) {
    ElMessage.warning('请输入机构名称')
    return
  }
  if (form.id) {
    await updateOrganization(form)
    ElMessage.success('更新成功')
  } else {
    await addOrganization(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row: SpecialOrganization) {
  await ElMessageBox.confirm('确认删除该机构？', '提示', { type: 'warning' })
  await deleteOrganizations([row.id!])
  ElMessage.success('删除成功')
  fetchList()
}

function handlePageChange(page: number) {
  pageNum.value = page
  fetchList()
}

async function openEditById(editId: string | number) {
  const id = Number(editId)
  if (!id) {
    return
  }
  const row = tableData.value.find(item => item.id === id)
  if (row) {
    handleEdit(row)
    return
  }
  try {
    const detail = await getOrganization(id)
    handleEdit(detail)
  }
  catch {
    ElMessage.warning('未找到该机构')
  }
}

watch(
  () => route.query.editId,
  (editId) => {
    if (editId) {
      openEditById(String(editId))
    }
  },
)

onMounted(async () => {
  await fetchList()
  if (route.query.editId) {
    await openEditById(String(route.query.editId))
  }
})
</script>

<template>
  <div>
    <div class="search-card">
      <el-input v-model="query.name" clearable placeholder="机构名称" style="width: 220px" @keyup.enter="handleQuery" />
      <el-select v-model="query.orgType" clearable placeholder="类型" style="width: 140px">
        <el-option v-for="t in orgTypes" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-select v-model="query.auditStatus" clearable placeholder="审核状态" style="width: 140px">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="search-card__actions">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>

    <div class="table-card">
      <el-empty v-if="!loading && tableData.length === 0" description="暂无数据" />
      <template v-else>
        <el-table v-loading="loading" :border="false" :data="tableData">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.coverUrl"
              :src="row.coverUrl"
              fit="cover"
              style="width: 64px; height: 64px; border-radius: 8px"
              :preview-src-list="[row.coverUrl]"
              preview-teleported
            />
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="机构名称" min-width="160" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            {{ typeLabelMap[row.orgType] || row.orgType }}
          </template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="region" label="地区" width="120" />
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 2 ? 'danger' : 'info'" effect="light">
              {{ auditStatusMap[row.auditStatus ?? 0] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
    <el-form label-width="90px">
      <el-form-item label="机构名称" required>
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="机构类型">
        <el-select v-model="form.orgType" style="width: 100%">
          <el-option v-for="t in orgTypes" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="许可证号">
        <el-input v-model="form.licenseNo" />
      </el-form-item>
      <el-form-item label="地址">
        <el-input v-model="form.address" />
      </el-form-item>
      <el-form-item label="地区">
        <el-input v-model="form.region" />
      </el-form-item>
      <el-form-item label="联系人">
        <el-input v-model="form.contactName" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.contactPhone" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="封面">
        <FgCoverUpload v-model="form.coverUrl" />
      </el-form-item>
      <el-form-item label="审核状态">
        <el-radio-group v-model="form.auditStatus">
          <el-radio :value="0">待审核</el-radio>
          <el-radio :value="1">已通过</el-radio>
          <el-radio :value="2">已拒绝</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="0">停用</el-radio>
          <el-radio :value="1">正常</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>
