<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addResource,
  deleteResources,
  listResources,
  updateResource,
  type SpecialResource,
} from '@/api/special'

const loading = ref(false)
const tableData = ref<SpecialResource[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const query = reactive({
  title: '',
  resourceType: '',
  status: '' as number | '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增资源')
const form = reactive<SpecialResource>({
  title: '',
  resourceType: 'course',
  category: '',
  summary: '',
  status: 0,
})

const resourceTypes = [
  { label: '课程', value: 'course' },
  { label: '工具', value: 'tool' },
  { label: '老师', value: 'teacher' },
  { label: '机构', value: 'org' },
  { label: '评估', value: 'assessment' },
]

const typeLabelMap = Object.fromEntries(resourceTypes.map(item => [item.value, item.label]))

async function fetchList() {
  loading.value = true
  try {
    const res = await listResources({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      title: query.title || undefined,
      resourceType: query.resourceType || undefined,
      status: query.status === '' ? undefined : query.status,
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
  query.title = ''
  query.resourceType = ''
  query.status = ''
  handleQuery()
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    title: '',
    resourceType: 'course',
    category: '',
    summary: '',
    content: '',
    providerName: '',
    contactPhone: '',
    region: '',
    status: 0,
  })
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增资源'
  dialogVisible.value = true
}

function handleEdit(row: SpecialResource) {
  Object.assign(form, { ...row })
  dialogTitle.value = '编辑资源'
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.title) {
    ElMessage.warning('请输入标题')
    return
  }
  if (form.id) {
    await updateResource(form)
    ElMessage.success('更新成功')
  } else {
    await addResource(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row: SpecialResource) {
  await ElMessageBox.confirm('确认删除该资源？', '提示', { type: 'warning' })
  await deleteResources([row.id!])
  ElMessage.success('删除成功')
  fetchList()
}

function handlePageChange(page: number) {
  pageNum.value = page
  fetchList()
}

onMounted(fetchList)
</script>

<template>
  <div>
    <div class="workbench-head">
      <h2>资源管理</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>

    <div class="workbench-filter">
      <el-input v-model="query.title" clearable placeholder="标题" style="width: 220px" @keyup.enter="handleQuery" />
      <el-select v-model="query.resourceType" clearable placeholder="类型" style="width: 140px">
        <el-option v-for="t in resourceTypes" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="状态" style="width: 140px">
        <el-option label="草稿" :value="0" />
        <el-option label="已发布" :value="1" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="workbench-card">
      <el-table v-loading="loading" :data="tableData">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            {{ typeLabelMap[row.resourceType] || row.resourceType }}
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="providerName" label="提供方" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '已发布' : '草稿' }}
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
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
    <el-form label-width="90px">
      <el-form-item label="标题" required>
        <el-input v-model="form.title" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.resourceType" style="width: 100%">
          <el-option v-for="t in resourceTypes" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类">
        <el-input v-model="form.category" />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="提供方">
        <el-input v-model="form.providerName" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.contactPhone" />
      </el-form-item>
      <el-form-item label="地区">
        <el-input v-model="form.region" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="0">草稿</el-radio>
          <el-radio :value="1">已发布</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>
