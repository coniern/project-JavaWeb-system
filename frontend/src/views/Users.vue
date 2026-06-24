<template>
  <div class="page-shell">
    <section class="page-banner">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-subtitle">集中管理账号、角色和状态，管理员可以直接在侧边抽屉中创建和维护成员信息。</p>
      </div>
      <div class="page-actions">
        <div class="badge-pill">启用账号 {{ activeUsers }}</div>
        <el-button type="primary" @click="openCreateDrawer">添加用户</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article class="stat-panel">
        <div class="stat-label">用户总数</div>
        <div class="stat-value">{{ filteredUsers.length }}</div>
        <div class="stat-footnote">当前列表中的全部账号</div>
      </article>
      <article class="stat-panel">
        <div class="stat-label">启用账号</div>
        <div class="stat-value">{{ activeUsers }}</div>
        <div class="stat-footnote">当前可登录并参与协作的成员</div>
      </article>
      <article class="stat-panel">
        <div class="stat-label">管理员</div>
        <div class="stat-value">{{ adminUsers }}</div>
        <div class="stat-footnote">具备系统全局权限的账号数</div>
      </article>
      <article class="stat-panel">
        <div class="stat-label">禁用账号</div>
        <div class="stat-value">{{ disabledUsers }}</div>
        <div class="stat-footnote">已经被停用的成员账号</div>
      </article>
    </section>

    <el-card class="panel-card toolbar-card">
      <div class="toolbar-row">
        <div class="toolbar-filters">
          <el-input v-model="filters.keyword" placeholder="搜索用户名、昵称或邮箱" clearable style="width: 260px" />
          <el-select v-model="filters.status" clearable placeholder="账号状态" style="width: 180px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </div>
        <div class="toolbar-actions">
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="primary" @click="openCreateDrawer">添加用户</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="panel-card data-card">
      <el-table :data="filteredUsers" v-loading="loading" class="soft-table" table-layout="auto">
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="roleCodes" label="角色" min-width="160">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="role in row.roleCodes" :key="role" type="info">{{ roleLabel(role) }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDrawer(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="drawerVisible" :title="isEdit ? '编辑用户' : '添加用户'" size="460px" class="drawer-form">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item :label="isEdit ? '重置密码（可选）' : '密码'" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入至少 6 位密码" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="form.roleCode" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createUser, deleteUser, getUserList, updateUser } from '@/api/user'
import { roleOptions } from '@/constants/options'
import { useSessionStore } from '@/stores/session'

const session = useSessionStore()
const loading = ref(false)
const submitting = ref(false)
const drawerVisible = ref(false)
const isEdit = ref(false)
const users = ref([])
const formRef = ref(null)

const filters = reactive({
  keyword: '',
  status: null
})

const form = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  status: 1,
  roleCode: 'DEVELOPER'
})

const rules = computed(() => ({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    {
      validator: (rule, value, callback) => {
        if (!isEdit.value && !value) {
          callback(new Error('请输入密码'))
          return
        }
        if (value && value.length < 6) {
          callback(new Error('密码长度不能少于6位'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请选择角色', trigger: 'change' }]
}))

const filteredUsers = computed(() => {
  return users.value.filter((user) => {
    const keyword = filters.keyword.trim().toLowerCase()
    const matchesKeyword = !keyword || [user.username, user.nickname, user.email]
      .filter(Boolean)
      .some((field) => field.toLowerCase().includes(keyword))
    const matchesStatus = filters.status === null || user.status === filters.status
    return matchesKeyword && matchesStatus
  })
})

const activeUsers = computed(() => users.value.filter((user) => user.status === 1).length)
const disabledUsers = computed(() => users.value.filter((user) => user.status === 0).length)
const adminUsers = computed(() => users.value.filter((user) => user.roleCodes?.includes('ADMIN')).length)

const roleLabel = (roleCode) => roleOptions.find((item) => item.value === roleCode)?.label || roleCode

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList()
    users.value = res.data
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    status: 1,
    roleCode: 'DEVELOPER'
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
    username: row.username,
    password: '',
    nickname: row.nickname,
    email: row.email,
    phone: row.phone,
    status: row.status,
    roleCode: row.roleCodes?.[0] || 'DEVELOPER'
  })
  drawerVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    const payload = {
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      email: form.email,
      phone: form.phone,
      status: form.status,
      roleCode: form.roleCode
    }
    if (isEdit.value) {
      await updateUser(form.id, payload)
      ElMessage.success('用户信息已更新')
      if (session.user?.id === form.id) {
        session.updateUser({ ...session.user, nickname: form.nickname, email: form.email, phone: form.phone, roleCodes: [form.roleCode] })
      }
    } else {
      await createUser(payload)
      ElMessage.success('用户已创建')
    }
    drawerVisible.value = false
    await loadUsers()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」吗？`, '删除确认', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('用户已删除')
  await loadUsers()
}

const resetFilters = () => {
  filters.keyword = ''
  filters.status = null
}

onMounted(loadUsers)
</script>
