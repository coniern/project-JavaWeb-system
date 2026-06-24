<template>
  <div class="page-shell">
    <section class="page-banner">
      <div>
        <h1 class="page-title">AI 助手</h1>
        <p class="page-subtitle">输入项目目标和背景，生成摘要、风险、里程碑与任务建议，并将任务一键导入系统。</p>
      </div>
      <div class="page-actions">
        <div class="badge-pill">{{ aiStatus.connected ? 'AI 已连接' : '本地回退模式' }}</div>
        <el-tag :type="result.usedFallback ? 'warning' : 'success'">
          {{ result.usedFallback ? 'Fallback' : (result.provider || 'Remote AI') }}
        </el-tag>
      </div>
    </section>

    <section class="grid-two">
      <el-card class="section-card panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <strong>项目输入</strong>
              <p class="muted-text">尽量补充背景、目标和截止时间，任务拆解会更稳定。</p>
            </div>
          </div>
        </template>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="目标项目" prop="targetProjectId">
            <el-select v-model="form.targetProjectId" clearable placeholder="可选：选择要导入任务的项目" style="width: 100%">
              <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="项目名称" prop="name">
            <el-input v-model="form.name" placeholder="例如：毕业设计项目管理平台重构" />
          </el-form-item>
          <el-form-item label="项目描述" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="描述背景、现有问题和本轮改造重点" />
          </el-form-item>
          <el-form-item label="项目目标" prop="goal">
            <el-input v-model="form.goal" type="textarea" :rows="3" placeholder="例如：统一后端主线、优化前端交互并完成论文演示版本" />
          </el-form-item>
          <el-form-item label="团队人数" prop="teamSize">
            <el-input-number v-model="form.teamSize" :min="1" :max="30" style="width: 100%" />
          </el-form-item>
          <el-form-item label="截止日期" prop="deadline">
            <el-date-picker v-model="form.deadline" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>

          <div class="drawer-actions ai-actions">
            <el-button @click="resetForm">重置</el-button>
            <el-button type="primary" :loading="loading" @click="handleGenerate">生成建议</el-button>
            <el-button type="success" plain :disabled="!canImport" :loading="importing" @click="handleImportTasks">导入任务</el-button>
          </div>
        </el-form>
      </el-card>

      <div class="app-page">
        <el-card class="section-card panel-card">
          <template #header>
            <div class="panel-header">
              <div>
                <strong>AI 摘要与风险</strong>
                <p class="muted-text">结合当前输入输出阶段判断和管理建议。</p>
              </div>
            </div>
          </template>
          <el-empty v-if="!hasResult" description="填写左侧信息后生成建议" />
          <div v-else class="summary-list">
            <div class="summary-item">
              <div>
                <h4>项目摘要</h4>
                <p>{{ result.summary }}</p>
              </div>
            </div>
            <div v-for="item in result.riskTips" :key="item" class="summary-item">
              <div>
                <h4>风险提示</h4>
                <p>{{ item }}</p>
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="section-card panel-card">
          <template #header>
            <div class="panel-header">
              <div>
                <strong>里程碑与任务拆解</strong>
                <p class="muted-text">可直接导入任务列表，后续再按项目进行分配。</p>
              </div>
            </div>
          </template>
          <div v-if="hasResult" class="app-page">
            <el-space wrap>
              <el-tag v-for="milestone in result.milestoneSuggestions" :key="milestone" effect="plain">{{ milestone }}</el-tag>
            </el-space>
            <el-table :data="result.taskSuggestions" class="soft-table" table-layout="auto">
              <el-table-column prop="title" label="任务" min-width="180" />
              <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
              <el-table-column prop="priority" label="优先级" width="100">
                <template #default="{ row }">
                  <el-tag :type="findOptionTag(taskPriorityOptions, row.priority)">{{ findOptionLabel(taskPriorityOptions, row.priority) }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-else description="暂无 AI 结果" />
        </el-card>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAiStatus, getAiSuggestions } from '@/api/ai'
import { getProjectList } from '@/api/project'
import { createTask } from '@/api/task'
import { findOptionLabel, findOptionTag, taskPriorityOptions } from '@/constants/options'
import { useSessionStore } from '@/stores/session'

const session = useSessionStore()
const formRef = ref(null)
const loading = ref(false)
const importing = ref(false)
const projects = ref([])
const aiStatus = ref({})
const result = reactive({
  summary: '',
  riskTips: [],
  milestoneSuggestions: [],
  taskSuggestions: [],
  provider: '',
  usedFallback: false
})

const form = reactive({
  targetProjectId: null,
  name: '',
  description: '',
  goal: '',
  teamSize: 3,
  deadline: ''
})

const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入项目描述', trigger: 'blur' }],
  goal: [{ required: true, message: '请输入项目目标', trigger: 'blur' }]
}

const hasResult = computed(() => Boolean(result.summary))
const canImport = computed(() => form.targetProjectId && result.taskSuggestions.length > 0)

const loadProjects = async () => {
  const [projectRes, statusRes] = await Promise.all([
    getProjectList({ pageSize: 200 }),
    getAiStatus()
  ])
  projects.value = projectRes.data
  aiStatus.value = statusRes.data
}

const handleGenerate = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const res = await getAiSuggestions(form)
    Object.assign(result, res.data)
  } finally {
    loading.value = false
  }
}

const handleImportTasks = async () => {
  if (!canImport.value) {
    ElMessage.warning('请先选择目标项目并生成任务建议')
    return
  }
  importing.value = true
  try {
    for (const item of result.taskSuggestions) {
      await createTask({
        projectId: form.targetProjectId,
        title: item.title,
        description: item.description,
        status: 'todo',
        priority: item.priority,
        assigneeId: session.user?.id
      })
    }
    ElMessage.success('AI 任务建议已导入')
  } finally {
    importing.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    targetProjectId: null,
    name: '',
    description: '',
    goal: '',
    teamSize: 3,
    deadline: ''
  })
  Object.assign(result, {
    summary: '',
    riskTips: [],
    milestoneSuggestions: [],
    taskSuggestions: [],
    provider: '',
    usedFallback: false
  })
}

onMounted(loadProjects)
</script>

<style scoped>
.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.panel-header strong {
  font-size: 16px;
}

.panel-header p {
  margin: 6px 0 0;
  font-size: 13px;
}

.ai-actions {
  justify-content: flex-start;
}
</style>
