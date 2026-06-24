<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="brand-panel">
        <div class="brand-badge">Project OS</div>
        <h1>把项目管理系统收敛成一套干净的 Spring Boot 主线。</h1>
        <p>
          当前版本统一了认证、项目、任务、用户和 AI 助手接口，前端也重做为更稳定的企业后台布局。
        </p>

        <div class="feature-list">
          <div class="feature-item">
            <strong>单一主线</strong>
            <span>旧 JSP / Servlet 与重复后端实现已退出正式架构。</span>
          </div>
          <div class="feature-item">
            <strong>REST 统一</strong>
            <span>认证、项目、任务和用户接口全部使用一致的资源化路径。</span>
          </div>
          <div class="feature-item">
            <strong>AI 协作</strong>
            <span>支持摘要、风险和任务建议生成，并可直接导入任务。</span>
          </div>
        </div>
      </section>

      <section class="form-panel">
        <div class="form-header">
          <h2>登录后台</h2>
          <p>使用演示账号进入系统，继续管理项目和任务。</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleLogin">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" size="large" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" size="large" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-button" @click="handleLogin">
            进入工作台
          </el-button>
        </el-form>

        <div class="account-hints">
          <span>演示账号</span>
          <div class="hint-tags">
            <el-tag>admin / 123456</el-tag>
            <el-tag type="success">dev / 123456</el-tag>
            <el-tag type="warning">tester / 123456</el-tag>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'
import { useSessionStore } from '@/stores/session'

const router = useRouter()
const session = useSessionStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const res = await login(form)
    session.setSession({
      token: res.data.token,
      user: res.data.user
    })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px 18px;
  background:
    radial-gradient(circle at top left, rgba(15, 118, 110, 0.18), transparent 30%),
    radial-gradient(circle at bottom right, rgba(245, 158, 11, 0.16), transparent 28%),
    linear-gradient(180deg, #f8fbfa, #edf4f2);
}

.login-shell {
  width: min(1120px, 100%);
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  overflow: hidden;
  border-radius: 34px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 26px 60px rgba(15, 23, 42, 0.12);
  backdrop-filter: blur(14px);
}

.brand-panel {
  padding: 48px;
  background:
    radial-gradient(circle at top left, rgba(15, 118, 110, 0.26), transparent 36%),
    linear-gradient(180deg, #102338 0%, #11273f 100%);
  color: #fff;
}

.brand-badge {
  display: inline-flex;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: #d5e7e4;
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.brand-panel h1 {
  margin: 24px 0 18px;
  font-size: 40px;
  line-height: 1.15;
}

.brand-panel p {
  margin: 0;
  color: #bfd1db;
  font-size: 15px;
  line-height: 1.9;
}

.feature-list {
  margin-top: 34px;
  display: grid;
  gap: 14px;
}

.feature-item {
  padding: 18px 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.feature-item strong {
  display: block;
  margin-bottom: 8px;
  font-size: 16px;
}

.feature-item span {
  color: #bfd1db;
  font-size: 13px;
  line-height: 1.7;
}

.form-panel {
  padding: 48px 42px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header h2 {
  margin: 0;
  font-size: 30px;
}

.form-header p {
  margin: 10px 0 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.login-button {
  width: 100%;
  margin-top: 8px;
}

.account-hints {
  margin-top: 26px;
  padding-top: 22px;
  border-top: 1px solid rgba(15, 23, 42, 0.08);
  color: var(--text-secondary);
  font-size: 13px;
}

.hint-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

@media (max-width: 960px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    padding: 32px 28px;
  }

  .brand-panel h1 {
    font-size: 32px;
  }

  .form-panel {
    padding: 32px 28px;
  }
}
</style>
