<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addArticle,
  deleteArticles,
  listArticles,
  updateArticle,
  type SpecialArticle,
} from '@/api/special'
import FgCoverUpload from '@/components/FgCoverUpload.vue'

const loading = ref(false)
const tableData = ref<SpecialArticle[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const query = reactive({
  title: '',
  category: '',
  status: '' as number | '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增资讯')
const form = reactive<SpecialArticle>({
  title: '',
  summary: '',
  content: '',
  coverUrl: '',
  category: 'policy',
  status: 0,
})

const categories = [
  { label: '政策解读', value: 'policy' },
  { label: '行业资讯', value: 'news' },
  { label: '家长指南', value: 'guide' },
]

const categoryLabelMap = Object.fromEntries(categories.map(item => [item.value, item.label]))

const statusMap: Record<number, { label: string, type: 'info' | 'success' | 'warning' }> = {
  0: { label: '草稿', type: 'info' },
  1: { label: '已发布', type: 'success' },
  2: { label: '已下架', type: 'warning' },
}

async function fetchList() {
  loading.value = true
  try {
    const res = await listArticles({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      title: query.title || undefined,
      category: query.category || undefined,
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
  query.category = ''
  query.status = ''
  handleQuery()
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    title: '',
    summary: '',
    content: '',
    coverUrl: '',
    category: 'policy',
    status: 0,
  })
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增资讯'
  dialogVisible.value = true
}

function handleEdit(row: SpecialArticle) {
  Object.assign(form, { ...row })
  dialogTitle.value = '编辑资讯'
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.title) {
    ElMessage.warning('请输入标题')
    return
  }
  if (form.id) {
    await updateArticle(form)
    ElMessage.success('更新成功')
  }
  else {
    await addArticle(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row: SpecialArticle) {
  await ElMessageBox.confirm('确认删除该资讯？', '提示', { type: 'warning' })
  await deleteArticles([row.id!])
  ElMessage.success('删除成功')
  fetchList()
}

function handlePageChange(page: number) {
  pageNum.value = page
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div>
    <div class="page-toolbar">
      <el-button type="primary" @click="handleAdd">
        新增
      </el-button>
    </div>

    <div class="search-card">
      <el-input v-model="query.title" clearable placeholder="标题" style="width: 220px" @keyup.enter="handleQuery" />
      <el-select v-model="query.category" clearable placeholder="分类" style="width: 140px">
        <el-option v-for="c in categories" :key="c.value" :label="c.label" :value="c.value" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="状态" style="width: 140px">
        <el-option label="草稿" :value="0" />
        <el-option label="已发布" :value="1" />
        <el-option label="已下架" :value="2" />
      </el-select>
      <el-button type="primary" @click="handleQuery">
        查询
      </el-button>
      <el-button @click="handleReset">
        重置
      </el-button>
    </div>

    <div class="table-card">
      <el-empty v-if="!loading && tableData.length === 0" description="暂无数据" />
      <template v-else>
        <el-table v-loading="loading" :border="false" :data="tableData">
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
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="分类" width="110">
          <template #default="{ row }">
            {{ categoryLabelMap[row.category] || row.category || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status ?? 0]?.type || 'info'" effect="light">
              {{ statusMap[row.status ?? 0]?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170" />
        <el-table-column prop="viewCount" label="浏览量" width="90" />
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
          @current-change="handlePageChange"
          @size-change="handleQuery"
        />
      </template>
    </div>
  </div>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
    <el-form label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="c in categories" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="封面">
          <FgCoverUpload v-model="form.coverUrl" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="支持 HTML，如 &lt;p&gt;段落&lt;/p&gt;"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">
              草稿
            </el-radio>
            <el-radio :value="1">
              已发布
            </el-radio>
            <el-radio :value="2">
              已下架
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
