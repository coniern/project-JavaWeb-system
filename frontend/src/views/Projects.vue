<template>
  <div class="projects-page">
    <div class="page-header">
      <h2 class="page-title">项目管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建项目
      </el-button>
    </div>
    
    <el-card>
      <el-table :data="projects" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="项目名称" min-width="180" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 新建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑项目' : '新建项目'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="规划中" value="规划中" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="ownerId">
          <el-select v-model="form.ownerId" placeholder="请选择负责人" style="width: 100%">
            <el-option
              v-for="user in ownerOptions"
              :key="user.id"
              :label="user.nickname || user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="进度" prop="progress">
          <el-input-number v-model="form.progress" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
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
import { getProjectList, createProject, updateProject, deleteProject } from '@/api/project'
import { getUserList } from '@/api/user'

const loading = ref(false)
const projects = ref([])
const ownerOptions = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  description: '',
  status: '规划中',
  startDate: '',
  endDate: '',
  ownerId: null,
  progress: 0
})

const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  ownerId: [{ required: true, message: '请选择负责人', trigger: 'change' }]
}

const getStatusType = (status) => {
  const map = { '规划中': 'info', '进行中': 'warning', '已完成': 'success' }
  return map[status] || ''
}

const loadProjects = async () => {
  loading.value = true
  try {
    const res = await getProjectList()
    projects.value = res.data
  } catch (error) {
    ElMessage.error(error.message || '加载项目失败')
  } finally {
    loading.value = false
  }
}

const loadUsers = async () => {
  const res = await getUserList()
  ownerOptions.value = res.data
}

const getDefaultOwnerId = () => ownerOptions.value[0]?.id || null

const handleCreate = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    status: '规划中',
    startDate: '',
    endDate: '',
    ownerId: getDefaultOwnerId(),
    progress: 0
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
          await updateProject(form)
          ElMessage.success('更新成功')
        } else {
          await createProject(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        await loadProjects()
      } catch (error) {
        ElMessage.error(error.message || (isEdit.value ? '更新失败' : '创建失败'))
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该项目吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteProject(row.id)
      ElMessage.success('删除成功')
      await loadProjects()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  })
}

onMounted(() => {
  Promise.all([loadProjects(), loadUsers()]).catch((error) => {
    ElMessage.error(error.message || '页面初始化失败')
  })
})
</script>

<style scoped>
.projects-page {
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
