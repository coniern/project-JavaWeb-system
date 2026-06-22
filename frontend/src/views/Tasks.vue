<template>
  <div class="tasks-page">
    <div class="page-header">
      <h2 class="page-title">任务管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建任务
      </el-button>
    </div>
    
    <el-card>
      <el-table :data="tasks" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="任务标题" min-width="180" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">{{ getPriorityLabel(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止日期" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 新建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑任务' : '新建任务'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%">
            <el-option
              v-for="project in projectOptions"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="执行人" prop="assigneeId">
          <el-select v-model="form.assigneeId" placeholder="请选择执行人" style="width: 100%">
            <el-option
              v-for="user in assigneeOptions"
              :key="user.id"
              :label="user.nickname || user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待开始" value="待开始" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择优先级" style="width: 100%">
            <el-option label="低" :value="3" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期" prop="dueDate">
          <el-date-picker v-model="form.dueDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTaskList, createTask, updateTask, deleteTask } from '@/api/task'
import { getProjectList } from '@/api/project'
import { getUserList } from '@/api/user'

const loading = ref(false)
const tasks = ref([])
const projectOptions = ref([])
const assigneeOptions = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  projectId: null,
  title: '',
  description: '',
  status: '待开始',
  priority: 2,
  assigneeId: null,
  dueDate: ''
})

const rules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  assigneeId: [{ required: true, message: '请选择执行人', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const getStatusType = (status) => {
  const map = { '待开始': 'info', '进行中': 'warning', '已完成': 'success' }
  return map[status] || ''
}

const getPriorityType = (priority) => {
  const map = { 1: 'danger', 2: 'warning', 3: 'success' }
  return map[priority] || ''
}

const getPriorityLabel = (priority) => {
  const map = { 1: '高', 2: '中', 3: '低' }
  return map[priority] || '中'
}

const loadTasks = async () => {
  loading.value = true
  try {
    const res = await getTaskList()
    tasks.value = res.data
  } catch (error) {
    ElMessage.error(error.message || '加载任务失败')
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  const [projectRes, userRes] = await Promise.all([getProjectList(), getUserList()])
  projectOptions.value = projectRes.data
  assigneeOptions.value = userRes.data
}

const getDefaultProjectId = () => projectOptions.value[0]?.id || null
const getDefaultAssigneeId = () => assigneeOptions.value[0]?.id || null

const handleCreate = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    projectId: getDefaultProjectId(),
    title: '',
    description: '',
    status: '待开始',
    priority: 2,
    assigneeId: getDefaultAssigneeId(),
    dueDate: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await updateTask(form)
          ElMessage.success('更新成功')
        } else {
          await createTask(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        await loadTasks()
      } catch (error) {
        ElMessage.error(error.message || (isEdit.value ? '更新失败' : '创建失败'))
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该任务吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteTask(row.id)
      ElMessage.success('删除成功')
      await loadTasks()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  })
}

onMounted(() => {
  Promise.all([loadTasks(), loadOptions()]).catch((error) => {
    ElMessage.error(error.message || '页面初始化失败')
  })
})
</script>

<style scoped>
.tasks-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}
</style>
