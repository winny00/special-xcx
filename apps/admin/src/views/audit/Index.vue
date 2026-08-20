<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  listOrganizations,
  listResources,
  type SpecialOrganization,
  type SpecialResource,
} from '@/api/special'

const router = useRouter()
const activeTab = ref('org')
const orgLoading = ref(false)
const resourceLoading = ref(false)
const orgList = ref<SpecialOrganization[]>([])
const resourceList = ref<SpecialResource[]>([])

async function loadOrgPending() {
  orgLoading.value = true
  try {
    const res = await listOrganizations({
      pageNum: 1,
      pageSize: 20,
      auditStatus: 0,
    })
    orgList.value = res.rows
  }
  finally {
    orgLoading.value = false
  }
}

async function loadResourceDraft() {
  resourceLoading.value = true
  try {
    const res = await listResources({
      pageNum: 1,
      pageSize: 20,
      status: 0,
    })
    resourceList.value = res.rows.filter(row => row.resourceType !== 'org')
  }
  finally {
    resourceLoading.value = false
  }
}

function handleTabChange(name: string | number) {
  if (name === 'org') {
    loadOrgPending()
  }
  else {
    loadResourceDraft()
  }
}

function goOrgEdit(row: SpecialOrganization) {
  router.push({ path: '/organization', query: { editId: String(row.id) } })
}

function goResourceEdit(row: SpecialResource) {
  const type = row.resourceType || 'course'
  router.push({ path: `/resource/${type}`, query: { editId: String(row.id) } })
}

onMounted(() => {
  loadOrgPending()
  loadResourceDraft()
})
</script>

<template>
  <div>
    <div class="workbench-head">
      <h2>审核中心</h2>
    </div>

    <div class="workbench-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="机构待审" name="org">
          <el-table v-loading="orgLoading" :data="orgList">
            <el-table-column prop="name" label="机构名称" min-width="160" />
            <el-table-column prop="orgType" label="类型" width="100" />
            <el-table-column prop="region" label="地区" width="120" />
            <el-table-column prop="contactPhone" label="联系电话" width="130" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="goOrgEdit(row)">去处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="资源草稿" name="resource">
          <el-table v-loading="resourceLoading" :data="resourceList">
            <el-table-column prop="title" label="标题" min-width="160" />
            <el-table-column prop="resourceType" label="类型" width="100" />
            <el-table-column prop="providerName" label="提供方" width="120" />
            <el-table-column label="状态" width="90">
              <template #default>
                <el-tag type="info" effect="light">草稿</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="goResourceEdit(row)">去处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
