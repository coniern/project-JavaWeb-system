<template>
  <div class="ai-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">AI 助手</h2>
        <p class="page-subtitle">生成项目摘要、风险提示、里程碑和任务建议，并可一键导入任务。</p>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="10">
        <el-card class="form-card">
          <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
            <el-form-item label="目标项目" prop="targetProjectId">
              <el-select v-model="form.targetProjectId" clearable placeholder="可选：选择要导入任务的项目" style="width: 100%">
                <el-option
                  v-for="project in projects"
                  :key="project.id"
                  :label="project.name"
                  :value="project.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="项目名称" prop="name">
              <el-input v-model="form.name" placeholder="例如：CRM 客户管理系统升级" />
            </el-form-item>
            <el-form-item label="项目描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="4" placeholder="描述当前项目背景、范围和现状" />
            </el-form-item>
            <el-form-item label="目标" prop="goal">
              <el-input v-model="form.goal" type="textarea" :rows="3" placeholder="例如：2 周内完成 MVP 并支持销售团队试用" />
            </el-form-item>
            <el-form-item label="团队人数" prop="teamSize">
              <el-input-number v-model="form.teamSize" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
            <el-form-item label="截止日期" prop="deadline">
              <el-date-picker v-model="form.deadline" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
            <div class="actions">
              <el-button type="primary" :loading="loading" @click="handleGenerate">生成建议</el-button>
              <el-button
                type="success"
                plain
                :disabled="!canImport"
                :loading="importing"
                @click="handleImportTasks"
              >
                导入为任务
              </el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="14">
        <el-card class="result-card">
          <template #header>
            <div class="result-header">
              <span>AI 输出</span>
              <el-tag v-if="result.provider" :type="result.usedFallback ? 'warning' : 'success'">
                {{ result.usedFallback ? '本地建议' : '远程 AI' }}
              </el-tag>
            </div>
          </template>

          <el-empty v-if="!hasResult" description="填写左侧信息后生成建议" />

          <div v-else class="result-body">
            <section class="section-block">
              <h3>项目摘要</h3>
              <el-alert :title="result.summary" type="info" :closable="false" show-icon />
            </section>

            <section class="section-block">
              <h3>风险提示</h3>
              <el-timeline>
                <el-timeline-item v-for="item in result.riskTips" :key="item" type="warning">
                  {{ item }}
                </el-timeline-item>
              </el-timeline>
            </section>

            <section class="section-block">
              <h3>里程碑建议</h3>
              <div class="tag-list">
                <el-tag v-for="item in result.milestoneSuggestions" :key="item" effect="plain">
                  {{ item }}
                </el-tag>
              </div>
            </section>

            <section class="section-block">
              <h3>任务拆解</h3>
              <el-table :data="result.taskSuggestions" style="width: 100%">
                <el-table-column prop="title" label="任务" min-width="180" />
                <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
                <el-table-column prop="priority" label="优先级" width="90">
                  <template #default="{ row }">
                    <el-tag :type="priorityTagType(row.priority)">{{ priorityLabel(row.priority) }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </section>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAiSuggestions } from '@/api/ai'
import { getProjectList } from '@/api/project'
import { createTask } from '@/api/task'

const formRef = ref(null)
const loading = ref(false)
const importing = ref(false)
const projects = ref([])
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
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
}

const currentUser = computed(() => JSON.parse(localStorage.getItem('user') || '{}'))
const hasResult = computed(() => result.summary || result.taskSuggestions.length > 0)
const canImport = computed(() => hasResult.value && form.targetProjectId && result.taskSuggestions.length > 0)

const priorityLabel = (priority) => ({ 1: '高', 2: '中', 3: '低' }[priority] || '中')
const priorityTagType = (priority) => ({ 1: 'danger', 2: 'warning', 3: 'success' }[priority] || '')

const loadProjects = async () => {
  const res = await getProjectList()
  projects.value = res.data
}

const handleGenerate = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    loading.value = true
    try {
      const res = await getAiSuggestions(form)
      Object.assign(result, res.data)
      ElMessage.success(res.data.usedFallback ? '已生成本地 AI 建议' : '已生成 AI 建议')
    } catch (error) {
      ElMessage.error(error.message || '生成建议失败')
    } finally {
      loading.value = false
    }
  })
}

const handleImportTasks = async () => {
  importing.value = true
  try {
    const assigneeId = currentUser.value.id || 1
    for (const item of result.taskSuggestions) {
      await createTask({
        projectId: form.targetProjectId,
        assigneeId,
        title: item.title,
        description: item.description,
        priority: item.priority || 2,
        status: '待开始',
        dueDate: form.deadline
      })
    }
    ElMessage.success('AI 建议已导入任务管理')
  } catch (error) {
    ElMessage.error(error.message || '导入任务失败')
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  loadProjects().catch((error) => {
    ElMessage.error(error.message || '加载项目列表失败')
  })
})
</script>

<style scoped>
.ai-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
}

.page-subtitle {
  color: #6b7280;
}

.form-card,
.result-card {
  min-height: 100%;
}

.actions {
  display: flex;
  gap: 12px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-block h3 {
  font-size: 16px;
  margin-bottom: 12px;
  color: #1f2937;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
