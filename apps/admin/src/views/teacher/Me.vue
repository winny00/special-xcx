<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getMyTeacher,
  updateTeacher,
  type SpecialTeacher,
} from '@/api/special'
import FgCoverUpload from '@/components/FgCoverUpload.vue'
import { buildTeacherPayload } from '@/utils/teacher-payload'

const loading = ref(false)
const saving = ref(false)
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
})

const statusMap: Record<number, { label: string, type: 'info' | 'success' | 'warning' }> = {
  0: { label: '待审', type: 'info' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'warning' },
}

function mergeOrgOption(id: string, name?: string) {
  if (!id || orgOptions.value.some(item => item.id === id))
    return
  orgOptions.value = [...orgOptions.value, { id, name: name || `机构 ${id}` }]
}

async function fetchMine() {
  loading.value = true
  try {
    const row = await getMyTeacher()
    const orgId = row.orgId ? String(row.orgId) : ''
    Object.assign(form, {
      ...row,
      id: row.id == null ? undefined : String(row.id),
      orgId,
      userId: row.userId ? String(row.userId) : '',
      phone: '',
      initPassword: '',
    })
    if (orgId)
      mergeOrgOption(orgId)
  }
  catch {
    form.id = undefined
  }
  finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!form.name) {
    ElMessage.warning('请输入姓名')
    return
  }
  if (!form.id) {
    ElMessage.warning('暂无老师档案')
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
  saving.value = true
  try {
    await updateTeacher(payload)
    ElMessage.success('更新成功')
    await fetchMine()
  }
  finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchMine()
})
</script>

<template>
  <div>
    <div class="table-card">
      <el-skeleton v-if="loading" :rows="6" animated />
      <el-empty v-else-if="!form.id" description="暂无老师档案" />
      <el-form v-else label-width="90px">
        <el-form-item label="状态">
          <el-tag :type="statusMap[form.status ?? 0]?.type || 'info'" effect="light">
            {{ statusMap[form.status ?? 0]?.label || '未知' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" maxlength="100" />
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
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSubmit">
            保存
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
