<template>
  <div class="page-shell">
    <section class="page-banner">
      <div>
        <h1 class="page-title">项目管理</h1>
        <p class="page-subtitle">统一查看项目状态、负责人、技术栈和里程碑日期，使用右侧抽屉完成新增与编辑。</p>
      </div>
      <div class="page-actions">
        <div class="badge-pill">活跃项目 {{ stats.activeProjects || 0 }}</div>
        <el-button type="primary" @click="openCreateDrawer">新建项目</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article class="stat-panel">
        <div class="stat-label">待规划</div>
        <div class="stat-value">{{ stats.planningProjects || 0 }}</div>
        <div class="stat-footnote">尚未进入开发的项目</div>
      </article>
      <article class="stat-panel">
        <div class="stat-label">进行中</div>
        <div class="stat-value">{{ stats.activeProjects || 0 }}</div>
        <div class="stat-footnote">当前推进中的核心工作</div>
      </article>
      <article class="stat-panel">
        <div class="stat-label">待审核</div>
        <div class="stat-value">{{ stats.testingProjects || 0 }}</div>
        <div class="stat-footnote">等待测试与验收的项目</div>
      </article>
      <article class="stat-panel">
        <div class="stat-label">已完成</div>
        <div class="stat-value">{{ stats.completedProjects || 0 }}</div>
        <div class="stat-footnote">已进入复盘或交付阶段</div>
      </article>
    </section>

    <el-card class="panel-card toolbar-card">
      <div class="toolbar-row">
        <div class="toolbar-filters">
          <el-input v-model="filters.keyword" placeholder="搜索项目名称或描述" clearable style="width: 260px" @keyup.enter="loadProjects" />
          <el-select v-model="filters.status" clearable placeholder="全部状态" style="width: 180px">
            <el-option v-for="item in projectStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button @click="loadProjects">筛选</el-button>
        </div>
        <div class="toolbar-actions">
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="primary" @click="openCreateDrawer">新建项目</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="panel-card data-card">
      <el-table :data="projects" v-loading="loading" class="soft-table" table-layout="auto">
        <el-table-column prop="name" label="项目名称" min-width="180" />
        <el-table-column prop="manager" label="负责人" width="140" />
        <el-table-column prop="techStack" label="技术栈" min-width="160" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="findOptionTag(projectStatusOptions, row.statusCode)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" min-width="180">
          <template #default="{ row }">
            <el-progress :percentage="row.progress || 0" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDrawer(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="drawerVisible" :title="isEdit ? '编辑项目' : '新建项目'" size="520px" class="drawer-form">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：统一 Spring Boot 项目管理平台" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="说明业务目标、范围和现状" />
        </el-form-item>
        <el-form-item label="技术栈" prop="techStack">
          <el-select v-model="form.techStack" allow-create filterable placeholder="请选择或输入技术栈" style="width: 100%">
            <el-option v-for="item in techStackOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option v-for="item in projectStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="leaderId">
          <el-select v-model="form.leaderId" placeholder="请选择负责人" style="width: 100%">
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.nickname || user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="进度" prop="progress">
          <el-slider v-model="form.progress" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
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
import { createProject, deleteProject, getProjectDashboard, getProjectList, updateProject } from '@/api/project'
import { getUserList } from '@/api/user'
import { findOptionTag, projectStatusOptions, techStackOptions } from '@/constants/options'

const loading = ref(false)
const submitting = ref(false)
const drawerVisible = ref(false)
const isEdit = ref(false)
const projects = ref([])
const users = ref([])
const stats = ref({})
const formRef = ref(null)

const filters = reactive({
  keyword: '',
  status: ''
})

const form = reactive({
  id: null,
  name: '',
  description: '',
  techStack: 'Spring Boot',
  status: 'planning',
  leaderId: null,
  progress: 0,
  startDate: '',
  endDate: ''
})

const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  techStack: [{ required: true, message: '请输入技术栈', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  leaderId: [{ required: true, message: '请选择负责人', trigger: 'change' }]
}

const loadProjects = async () => {
  loading.value = true
  try {
    const [projectRes, statsRes] = await Promise.all([
      getProjectList(filters),
      getProjectDashboard()
    ])
    projects.value = projectRes.data
    stats.value = statsRes.data
  } finally {
    loading.value = false
  }
}

const loadUsers = async () => {
  const res = await getUserList()
  users.value = res.data
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    techStack: 'Spring Boot',
    status: 'planning',
    leaderId: null,
    progress: 0,
    startDate: '',
    endDate: ''
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
    name: row.name,
    description: row.description,
    techStack: row.techStack,
    status: row.statusCode,
    leaderId: row.leaderId,
    progress: row.progress || 0,
    startDate: row.startDate || '',
    endDate: row.endDate || ''
  })
  drawerVisible.value = true
}

const toDateTime = (value) => value ? `${value}T00:00:00` : null

const buildPayload = () => ({
  name: form.name,
  description: form.description,
  techStack: form.techStack,
  status: form.status,
  leaderId: form.leaderId,
  progress: form.progress,
  startTime: toDateTime(form.startDate),
  endTime: toDateTime(form.endDate)
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
      await updateProject(form.id, buildPayload())
      ElMessage.success('项目已更新')
    } else {
      await createProject(buildPayload())
      ElMessage.success('项目已创建')
    }
    drawerVisible.value = false
    await loadProjects()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除项目「${row.name}」吗？`, '删除确认', { type: 'warning' })
  await deleteProject(row.id)
  ElMessage.success('项目已删除')
  await loadProjects()
}

const resetFilters = async () => {
  filters.keyword = ''
  filters.status = ''
  await loadProjects()
}

onMounted(async () => {
  await Promise.all([loadProjects(), loadUsers()])
})
</script>
