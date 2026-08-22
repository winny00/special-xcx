<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  auditOrganizations,
  auditResources,
  auditTeachers,
  listOrganizations,
  listResources,
  listTeachers,
  type SpecialOrganization,
  type SpecialResource,
  type SpecialTeacher,
} from '@/api/special'

const router = useRouter()
const activeTab = ref('org')
const orgLoading = ref(false)
const resourceLoading = ref(false)
const teacherLoading = ref(false)
const orgList = ref<SpecialOrganization[]>([])
const resourceList = ref<SpecialResource[]>([])
const teacherList = ref<SpecialTeacher[]>([])
const orgSelection = ref<SpecialOrganization[]>([])
const resourceSelection = ref<SpecialResource[]>([])
const teacherSelection = ref<SpecialTeacher[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('审核')
const pendingStatus = ref(1)
const remark = ref('')
const pendingAction = ref<'org' | 'resource' | 'teacher'>('org')

async function loadOrgPending() {
  orgLoading.value = true
  try {
    const res = await listOrganizations({ pageNum: 1, pageSize: 50, auditStatus: 0 })
    orgList.value = res.rows
  }
  finally {
    orgLoading.value = false
  }
}

async function loadResourceDraft() {
  resourceLoading.value = true
  try {
    const res = await listResources({ pageNum: 1, pageSize: 50, status: 0 })
    resourceList.value = res.rows.filter(row => row.resourceType !== 'org')
  }
  finally {
    resourceLoading.value = false
  }
}

async function loadTeacherPending() {
  teacherLoading.value = true
  try {
    const res = await listTeachers({ pageNum: 1, pageSize: 50, status: 0 })
    teacherList.value = res.rows
  }
  finally {
    teacherLoading.value = false
  }
}

function handleTabChange(name: string | number) {
  if (name === 'org')
    loadOrgPending()
  else if (name === 'resource')
    loadResourceDraft()
  else
    loadTeacherPending()
}

function goOrgEdit(row: SpecialOrganization) {
  router.push({ path: '/organization', query: { editId: String(row.id) } })
}

function goResourceEdit(row: SpecialResource) {
  router.push({ path: `/resource/${row.resourceType || 'course'}`, query: { editId: String(row.id) } })
}

function goTeacherEdit(row: SpecialTeacher) {
  router.push({ path: '/teacher', query: { editId: String(row.id) } })
}

function openAudit(kind: 'org' | 'resource' | 'teacher', status: number) {
  const selected = kind === 'org'
    ? orgSelection.value
    : kind === 'resource'
      ? resourceSelection.value
      : teacherSelection.value
  if (selected.length === 0) {
    ElMessage.warning('请先勾选要审核的记录')
    return
  }
  pendingAction.value = kind
  pendingStatus.value = status
  remark.value = ''
  dialogTitle.value = status === 2
    ? (kind === 'resource' ? '批量下架' : '批量拒绝')
    : (kind === 'resource' ? '批量发布' : '批量通过')
  dialogVisible.value = true
}

async function submitAudit() {
  if (pendingStatus.value === 2 && !remark.value.trim()) {
    ElMessage.warning('拒绝时必须填写审核备注')
    return
  }
  const ids = (pendingAction.value === 'org'
    ? orgSelection.value.map(row => String(row.id))
    : pendingAction.value === 'resource'
      ? resourceSelection.value.map(row => String(row.id))
      : teacherSelection.value.map(row => String(row.id)))
  const payload = { ids, status: pendingStatus.value, remark: remark.value.trim() || undefined }
  if (pendingAction.value === 'org')
    await auditOrganizations(payload)
  else if (pendingAction.value === 'resource')
    await auditResources(payload)
  else
    await auditTeachers(payload)
  ElMessage.success('审核已提交')
  dialogVisible.value = false
  handleTabChange(activeTab.value)
}

onMounted(() => {
  loadOrgPending()
  loadResourceDraft()
  loadTeacherPending()
})
</script>

<template>
  <div>
    <div class="table-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="机构待审" name="org">
          <div class="audit-toolbar">
            <el-button type="primary" @click="openAudit('org', 1)">
              批量通过
            </el-button>
            <el-button @click="openAudit('org', 2)">
              批量拒绝
            </el-button>
          </div>
          <el-empty v-if="!orgLoading && orgList.length === 0" description="暂无待审机构" />
          <el-table
            v-else
            v-loading="orgLoading"
            :border="false"
            :data="orgList"
            @selection-change="(rows: SpecialOrganization[]) => orgSelection = rows"
          >
            <el-table-column type="selection" width="48" />
            <el-table-column prop="name" label="机构名称" min-width="160" />
            <el-table-column prop="orgType" label="类型" width="100" />
            <el-table-column prop="region" label="地区" width="120" />
            <el-table-column prop="contactPhone" label="联系电话" width="130" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="goOrgEdit(row)">
                  去处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="资源草稿" name="resource">
          <div class="audit-toolbar">
            <el-button type="primary" @click="openAudit('resource', 1)">
              批量发布
            </el-button>
            <el-button @click="openAudit('resource', 2)">
              批量下架
            </el-button>
          </div>
          <el-empty v-if="!resourceLoading && resourceList.length === 0" description="暂无资源草稿" />
          <el-table
            v-else
            v-loading="resourceLoading"
            :border="false"
            :data="resourceList"
            @selection-change="(rows: SpecialResource[]) => resourceSelection = rows"
          >
            <el-table-column type="selection" width="48" />
            <el-table-column prop="title" label="标题" min-width="160" />
            <el-table-column prop="resourceType" label="类型" width="100" />
            <el-table-column prop="providerName" label="提供方" width="120" />
            <el-table-column label="状态" width="90">
              <template #default>
                <el-tag type="info" effect="light">
                  草稿
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="goResourceEdit(row)">
                  去处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="老师待审" name="teacher">
          <div class="audit-toolbar">
            <el-button type="primary" @click="openAudit('teacher', 1)">
              批量通过
            </el-button>
            <el-button @click="openAudit('teacher', 2)">
              批量拒绝
            </el-button>
          </div>
          <el-empty v-if="!teacherLoading && teacherList.length === 0" description="暂无待审老师" />
          <el-table
            v-else
            v-loading="teacherLoading"
            :border="false"
            :data="teacherList"
            @selection-change="(rows: SpecialTeacher[]) => teacherSelection = rows"
          >
            <el-table-column type="selection" width="48" />
            <el-table-column prop="name" label="姓名" min-width="120" />
            <el-table-column prop="title" label="头衔" width="140" />
            <el-table-column prop="specialties" label="擅长" min-width="160" show-overflow-tooltip />
            <el-table-column label="状态" width="90">
              <template #default>
                <el-tag type="info" effect="light">
                  待审
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="goTeacherEdit(row)">
                  去处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-input
        v-model="remark"
        type="textarea"
        :rows="4"
        :placeholder="pendingStatus === 2 ? '拒绝原因（必填）' : '审核备注（可选）'"
        maxlength="500"
        show-word-limit
      />
      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="submitAudit">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.audit-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
</style>
