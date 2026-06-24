<template>
  <div class="page-shell">
    <section class="page-banner">
      <div>
        <h1 class="page-title">任务管理</h1>
        <p class="page-subtitle">按项目、状态和优先级追踪任务，支持从 AI 建议直接导入后继续细化。</p>
      </div>
      <div class="page-actions">
        <div class="badge-pill">待开始 {{ stats.todo || 0 }}</div>
        <el-button type="primary" @click="openCreateDrawer">新建任务</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article class="stat-panel">
        <div class="stat-label">任务总数</div>
        <div class="stat-value">{{ stats.total || 0 }}</div>
        <div class="stat-footnote">当前所有项目任务</div>
      </article>
      <article class="stat-panel">
        <div class="stat-label">待开始</div>
        <div class="stat-value">{{ stats.todo || 0 }}</div>
        <div class="stat-footnote">等待认领或推进</div>
      </article>
      <article class="stat-panel">
        <div class="stat-label">进行中</div>
        <div class="stat-value">{{ stats.inProgress || 0 }}</div>
        <div class="stat-footnote">正在开发或联调的任务</div>
      </article>
      <article class="stat-panel">
        <div class="stat-label">已完成</div>
        <div class="stat-value">{{ stats.done || 0 }}</div>
        <div class="stat-footnote">已关闭的任务</div>
      </article>
    </section>

    <el-card class="panel-card toolbar-card">
      <div class="toolbar-row">
        <div class="toolbar-filters">
          <el-input v-model="filters.keyword" placeholder="搜索任务标题或描述" clearable style="width: 250px" @keyup.enter="loadTasks" />
          <el-select v-model="filters.projectId" clearable placeholder="所属项目" style="width: 220px">
            <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
          </el-select>
          <el-select v-model="filters.status" clearable placeholder="任务状态" style="width: 180px">
            <el-option v-for="item in taskStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button @click="loadTasks">筛选</el-button>
        </div>
        <div class="toolbar-actions">
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="primary" @click="openCreateDrawer">新建任务</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="panel-card data-card">
      <el-table :data="tasks" v-loading="loading" class="soft-table" table-layout="auto">
        <el-table-column prop="title" label="任务标题" min-width="180" />
        <el-table-column prop="projectName" label="所属项目" min-width="160" />
        <el-table-column prop="assigneeName" label="执行人" width="130" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="findOptionTag(taskStatusOptions, row.statusCode)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priorityLabel" label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="findOptionTag(taskPriorityOptions, row.priority)">{{ row.priorityLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止日期" width="120" />
        <el-table-column prop="updatedAt" label="最近更新" width="120" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDrawer(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="drawerVisible" :title="isEdit ? '编辑任务' : '新建任务'" size="500px" class="drawer-form">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="例如：补齐登录链路异常处理" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="描述任务范围、背景和完成标准" />
        </el-form-item>
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="请选择所属项目" style="width: 100%">
            <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行人" prop="assigneeId">
          <el-select v-model="form.assigneeId" placeholder="请选择执行人" style="width: 100%">
            <el-option v-for="user in users" :key="user.id" :label="user.nickname || user.username" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option v-for="item in taskStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择优先级" style="width: 100%">
            <el-option v-for="item in taskPriorityOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期" prop="dueDate">
          <el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-actions">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProjectList } from '@/api/project'
import { createTask, deleteTask, getTaskList, getTaskStats, updateTask } from '@/api/task'
import { getUserList } from '@/api/user'
import { findOptionTag, taskPriorityOptions, taskStatusOptions } from '@/constants/options'

const loading = ref(false)
const submitting = ref(false)
const drawerVisible = ref(false)
const isEdit = ref(false)
const tasks = ref([])
const projects = ref([])
const users = ref([])
const stats = ref({})
const formRef = ref(null)

const filters = reactive({
  keyword: '',
  projectId: null,
  status: ''
})

const form = reactive({
  id: null,
  title: '',
  description: '',
  projectId: null,
  assigneeId: null,
  status: 'todo',
  priority: 2,
  dueDate: ''
})

const rules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  projectId: [{ required: true, message: '请选择所属项目', trigger: 'change' }],
  assigneeId: [{ required: true, message: '请选择执行人', trigger: 'change' }],
  status: [{ required: true, message: '请选择任务状态', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

const loadTasks = async () => {
  loading.value = true
  try {
    const [taskRes, statsRes] = await Promise.all([
      getTaskList(filters),
      getTaskStats(filters.projectId ? { projectId: filters.projectId } : {})
    ])
    tasks.value = taskRes.data
    stats.value = statsRes.data
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  const [projectRes, userRes] = await Promise.all([
    getProjectList({ pageSize: 200 }),
    getUserList()
  ])
  projects.value = projectRes.data
  users.value = userRes.data
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    title: '',
    description: '',
    projectId: null,
    assigneeId: null,
    status: 'todo',
    priority: 2,
    dueDate: ''
  })
}

const openCreateDrawer = () => {
  isEdit.value = false
  resetForm()
  drawerVisible.value = true
}

const openEditDrawer = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    title: row.title,
    description: row.description,
    projectId: row.projectId,
    assigneeId: row.assigneeId,
    status: row.statusCode,
    priority: row.priority,
    dueDate: row.dueDate
  })
  drawerVisible.value = true
}

const buildPayload = () => ({
  title: form.title,
  description: form.description,
  projectId: form.projectId,
  assigneeId: form.assigneeId,
  status: form.status,
  priority: form.priority,
  dueDate: form.dueDate
})

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateTask(form.id, buildPayload())
      ElMessage.success('任务已更新')
    } else {
      await createTask(buildPayload())
      ElMessage.success('任务已创建')
    }
    drawerVisible.value = false
    await loadTasks()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除任务「${row.title}」吗？`, '删除确认', { type: 'warning' })
  await deleteTask(row.id)
  ElMessage.success('任务已删除')
  await loadTasks()
}

const resetFilters = async () => {
  filters.keyword = ''
  filters.projectId = null
  filters.status = ''
  await loadTasks()
}

onMounted(async () => {
  await Promise.all([loadTasks(), loadOptions()])
})
</script>
