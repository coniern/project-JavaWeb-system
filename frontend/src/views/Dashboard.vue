<template>
  <div class="dashboard">
    <h2 class="page-title">仪表盘</h2>
    
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card gradient-1">
          <el-icon class="stat-icon"><Folder /></el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ stats.total || 0 }}</div>
            <div class="stat-label">总项目数</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card gradient-2">
          <el-icon class="stat-icon"><Document /></el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ stats.planning || 0 }}</div>
            <div class="stat-label">规划中</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card gradient-3">
          <el-icon class="stat-icon"><Odometer /></el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ stats.inProgress || 0 }}</div>
            <div class="stat-label">进行中</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card gradient-4">
          <el-icon class="stat-icon"><Select /></el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ stats.completed || 0 }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>项目状态分布</span>
          </template>
          <div ref="chartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近项目</span>
          </template>
          <el-table :data="projects" style="width: 100%">
            <el-table-column prop="name" label="项目名称" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="progress" label="进度">
              <template #default="{ row }">
                <el-progress :percentage="row.progress" :stroke-width="8" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getProjectStats, getProjectList } from '@/api/project'

const stats = ref({})
const projects = ref([])
const chartRef = ref(null)
let chartInstance = null
let resizeHandler = null

const getStatusType = (status) => {
  const map = { '规划中': 'info', '进行中': 'warning', '已完成': 'success' }
  return map[status] || ''
}

const loadStats = async () => {
  const res = await getProjectStats()
  stats.value = res.data
}

const loadProjects = async () => {
  const res = await getProjectList()
  projects.value = res.data.slice(0, 5)
}

const initChart = () => {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  chartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { top: '5%', left: 'center' },
    series: [{
      name: '项目状态',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false, position: 'center' },
      emphasis: {
        label: { show: true, fontSize: 20, fontWeight: 'bold' }
      },
      data: [
        { value: stats.value.planning || 0, name: '规划中' },
        { value: stats.value.inProgress || 0, name: '进行中' },
        { value: stats.value.completed || 0, name: '已完成' }
      ]
    }]
  })
}

onMounted(() => {
  resizeHandler = () => chartInstance?.resize()
  window.addEventListener('resize', resizeHandler)
  Promise.all([loadStats(), loadProjects()]).catch((error) => {
    ElMessage.error(error.message || '加载仪表盘失败')
  })
})

watch(stats, () => {
  initChart()
}, { deep: true })

onBeforeUnmount(() => {
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #1f2937;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  border: none;
  color: #fff;
}

.stat-icon {
  font-size: 48px;
  opacity: 0.8;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 4px;
}

.gradient-1 { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.gradient-2 { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.gradient-3 { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.gradient-4 { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
</style>
