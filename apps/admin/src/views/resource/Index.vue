<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RESOURCE_TYPES } from '@/router'
import {
  addResource,
  deleteResources,
  getResource,
  listResources,
  updateResource,
  type SpecialResource,
} from '@/api/special'
import FgCoverUpload from '@/components/FgCoverUpload.vue'

const route = useRoute()

const fixedType = computed(() => {
  const type = route.params.type as string
  return RESOURCE_TYPES.includes(type as typeof RESOURCE_TYPES[number]) ? type : 'course'
})

const loading = ref(false)
const tableData = ref<SpecialResource[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const query = reactive({
  title: '',
  resourceType: fixedType.value,
  status: '' as number | '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增资源')
const form = reactive<SpecialResource>({
  title: '',
  resourceType: fixedType.value,
  category: '',
  summary: '',
  coverUrl: '',
  status: 0,
})

const resourceTypes = [
  { label: '课程', value: 'course' },
  { label: '工具', value: 'tool' },
  { label: '老师', value: 'teacher' },
  { label: '评估', value: 'assessment' },
]

const typeLabelMap = Object.fromEntries(resourceTypes.map(item => [item.value, item.label]))

function syncFixedType() {
  query.resourceType = fixedType.value
  form.resourceType = fixedType.value
}

async function fetchList() {
  loading.value = true
  try {
    const res = await listResources({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      title: query.title || undefined,
      resourceType: fixedType.value,
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
  query.title = ''
  query.status = ''
  syncFixedType()
  handleQuery()
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    title: '',
    resourceType: fixedType.value,
    category: '',
    summary: '',
    content: '',
    coverUrl: '',
    providerName: '',
    contactPhone: '',
    region: '',
    status: 0,
  })
}

function handleAdd() {
  resetForm()
  dialogTitle.value = `新增${typeLabelMap[fixedType.value] || '资源'}`
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
  form.resourceType = fixedType.value
  if (form.id) {
    await updateResource(form)
    ElMessage.success('更新成功')
  }
  else {
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

watch(fixedType, () => {
  syncFixedType()
  pageNum.value = 1
  fetchList()
})

watch(
  () => route.query.editId,
  async (editId) => {
    if (!editId) {
      return
    }
    const id = Number(editId)
    const row = tableData.value.find(item => item.id === id)
    if (row) {
      handleEdit(row)
      return
    }
    try {
      const detail = await getResource(id)
      handleEdit(detail)
    }
    catch {
      ElMessage.warning('未找到该资源')
    }
  },
)

onMounted(() => {
  syncFixedType()
  fetchList()
})
</script>

<template>
  <div>
    <div class="page-toolbar">
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>

    <div class="search-card">
      <el-input v-model="query.title" clearable placeholder="标题" style="width: 220px" @keyup.enter="handleQuery" />
      <el-select v-model="query.status" clearable placeholder="状态" style="width: 140px">
        <el-option label="草稿" :value="0" />
        <el-option label="已发布" :value="1" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
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
        <el-table-column prop="title" label="标题" min-width="160" />
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
      <el-form-item label="标题" required>
        <el-input v-model="form.title" />
      </el-form-item>
      <el-form-item label="分类">
        <el-input v-model="form.category" />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="封面">
        <FgCoverUpload v-model="form.coverUrl" />
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
