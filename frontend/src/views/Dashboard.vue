<template>
  <div class="app-page">
    <section class="page-banner">
      <div>
        <h1 class="page-title">仪表盘</h1>
        <p class="page-subtitle">
          查看项目推进、任务积压和 AI 洞察。当前版本已经切换为单一 Spring Boot 主线和统一 REST 接口。
        </p>
      </div>
      <div class="page-actions">
        <div class="badge-pill">平均进度 {{ stats.averageProgress || 0 }}%</div>
        <el-button type="primary" @click="router.push('/projects')">查看项目</el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article class="stat-panel">
        <div class="stat-label">项目总数</div>
        <div class="stat-value">{{ stats.totalProjects || 0 }}</div>
        <div class="stat-footnote">统一主线后的全部活跃项目</div>
      </article>
      <article class="stat-panel">
        <div class="stat-label">进行中项目</div>
        <div class="stat-value">{{ stats.activeProjects || 0 }}</div>
        <div class="stat-footnote">当前需要持续推进的核心项目</div>
      </article>
      <article class="stat-panel">
        <div class="stat-label">任务待处理</div>
        <div class="stat-value">{{ stats.todoTasks || 0 }}</div>
        <div class="stat-footnote">待开始的任务项</div>
      </article>
      <article class="stat-panel">
        <div class="stat-label">团队成员</div>
        <div class="stat-value">{{ stats.registeredUsers || 0 }}</div>
        <div class="stat-footnote">当前系统中的有效账号数</div>
      </article>
    </section>

    <section class="grid-two">
      <el-card class="section-card panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <strong>项目状态分布</strong>
              <p class="muted-text">规划、开发、待审核和已完成的结构占比。</p>
            </div>
          </div>
        </template>
        <div ref="chartRef" class="chart-box"></div>
      </el-card>

      <el-card class="section-card panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <strong>AI 洞察</strong>
              <p class="muted-text">基于当前项目状态自动生成的管理建议。</p>
            </div>
            <el-tag :type="aiStatus.connected ? 'success' : 'warning'">
              {{ aiStatus.connected ? aiStatus.provider || 'AI 已连接' : '回退模式' }}
            </el-tag>
          </div>
        </template>
        <div class="summary-list">
          <div class="summary-item">
            <div>
              <h4>洞察摘要</h4>
              <p>{{ insight || '正在加载 AI 洞察...' }}</p>
            </div>
          </div>
          <div class="summary-item">
            <div>
              <h4>任务节奏</h4>
              <p>当前待开始任务 {{ stats.todoTasks || 0 }} 个，进行中任务 {{ stats.inProgressTasks || 0 }} 个，建议优先清理阻塞项。</p>
            </div>
          </div>
          <div class="summary-item">
            <div>
              <h4>协作建议</h4>
              <p>当测试中项目和待开始任务同时堆积时，优先缩小迭代范围，减少联调回滚成本。</p>
            </div>
          </div>
        </div>
      </el-card>
    </section>

    <section class="grid-two">
      <el-card class="section-card panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <strong>最近项目</strong>
              <p class="muted-text">关注近期更新的项目进度和负责人。</p>
            </div>
          </div>
        </template>
        <el-table :data="projects" class="soft-table" table-layout="auto">
          <el-table-column prop="name" label="项目名称" min-width="180" />
          <el-table-column prop="manager" label="负责人" width="140" />
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
        </el-table>
      </el-card>

      <el-card class="section-card panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <strong>近期任务</strong>
              <p class="muted-text">按任务更新时间排序，便于快速识别热点工作。</p>
            </div>
          </div>
        </template>
        <div class="summary-list">
          <div v-for="task in tasks" :key="task.id" class="summary-item">
            <div>
              <h4>{{ task.title }}</h4>
              <p>{{ task.projectName }} · {{ task.assigneeName }} · 截止 {{ task.dueDate || '待定' }}</p>
            </div>
            <el-tag :type="findOptionTag(taskStatusOptions, task.statusCode)">{{ task.status }}</el-tag>
          </div>
          <el-empty v-if="!tasks.length" description="暂无任务数据" />
        </div>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getProjectDashboard, getProjectList } from '@/api/project'
import { getTaskList } from '@/api/task'
import { getAiStatus, getDashboardInsight } from '@/api/ai'
import { findOptionTag, projectStatusOptions, taskStatusOptions } from '@/constants/options'

const router = useRouter()
const stats = ref({})
const projects = ref([])
const tasks = ref([])
const insight = ref('')
const aiStatus = ref({})
const chartRef = ref(null)
let chartInstance = null

const loadDashboard = async () => {
  const [dashboardRes, projectRes, taskRes, insightRes, aiStatusRes] = await Promise.all([
    getProjectDashboard(),
    getProjectList({ pageSize: 6 }),
    getTaskList({}),
    getDashboardInsight(),
    getAiStatus()
  ])
  stats.value = dashboardRes.data
  projects.value = projectRes.data.slice(0, 5)
  tasks.value = taskRes.data.slice(0, 5)
  insight.value = insightRes.data.insight
  aiStatus.value = aiStatusRes.data
}

const renderChart = () => {
  if (!chartRef.value) {
    return
  }
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption({
    tooltip: { trigger: 'item' },
    color: ['#94a3b8', '#0f766e', '#d97706', '#15803d'],
    series: [
      {
        type: 'pie',
        radius: ['48%', '74%'],
        itemStyle: { borderRadius: 12, borderColor: '#fff', borderWidth: 3 },
        label: { formatter: '{b}\n{d}%' },
        data: [
          { name: '待规划', value: stats.value.planningProjects || 0 },
          { name: '进行中', value: stats.value.activeProjects || 0 },
          { name: '待审核', value: stats.value.testingProjects || 0 },
          { name: '已完成', value: stats.value.completedProjects || 0 }
        ]
      }
    ]
  })
}

const handleResize = () => {
  chartInstance?.resize()
}

watch(stats, async () => {
  await nextTick()
  renderChart()
}, { deep: true })

onMounted(async () => {
  await loadDashboard()
  renderChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
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

.chart-box {
  height: 360px;
}
</style>
