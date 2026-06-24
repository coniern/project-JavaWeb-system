<template>
  <div class="layout-root">
    <aside class="sidebar" :class="{ collapsed: isCollapsed, mobile: isMobile }">
      <div class="brand">
        <div class="brand-mark">PO</div>
        <div v-if="!isCollapsed" class="brand-copy">
          <strong>Project OS</strong>
          <span>Pure Spring Boot</span>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="nav-menu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
        background-color="transparent"
        text-color="#9fb0c3"
        active-text-color="#ffffff"
      >
        <el-menu-item v-for="item in navItems" :key="item.path" :index="item.path" @click="mobileDrawer = false">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
      <div v-if="!isCollapsed" class="sidebar-foot">
        <span class="badge-pill">统一 REST API</span>
        <p>项目、任务、AI 和权限口径已经收敛到单主线后端。</p>
      </div>
    </aside>

    <el-drawer v-model="mobileDrawer" :with-header="false" size="260px" class="mobile-nav">
      <aside class="sidebar mobile opened">
        <div class="brand">
          <div class="brand-mark">PO</div>
          <div class="brand-copy">
            <strong>Project OS</strong>
            <span>Pure Spring Boot</span>
          </div>
        </div>
        <el-menu :default-active="activeMenu" class="nav-menu" router background-color="transparent" text-color="#9fb0c3" active-text-color="#ffffff">
          <el-menu-item v-for="item in navItems" :key="item.path" :index="item.path" @click="mobileDrawer = false">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-menu>
      </aside>
    </el-drawer>

    <div class="layout-main">
      <header class="topbar">
        <div class="topbar-left">
          <el-button circle class="ghost-button" @click="handleMenuToggle">
            <el-icon><Fold /></el-icon>
          </el-button>
          <div>
            <div class="crumbs">Project OS / {{ route.meta?.title || '工作台' }}</div>
            <div class="topbar-title">{{ route.meta?.title || '工作台' }}</div>
          </div>
        </div>
        <div class="topbar-right">
          <div class="badge-pill topbar-badge">Spring Boot + Vue 3</div>
          <el-dropdown>
            <div class="profile-chip">
              <el-avatar :size="36" :icon="UserFilled" />
              <div class="profile-meta">
                <strong>{{ session.displayName }}</strong>
                <span>{{ session.user?.roleCodes?.join(' / ') || '团队成员' }}</span>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useSessionStore } from '@/stores/session'
import {
  DataAnalysis,
  Fold,
  FolderOpened,
  MagicStick,
  Tickets,
  UserFilled,
  User
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const session = useSessionStore()
const mobileDrawer = ref(false)
const isMobile = ref(window.innerWidth < 960)

const navItems = [
  { path: '/dashboard', label: '仪表盘', icon: DataAnalysis },
  { path: '/projects', label: '项目管理', icon: FolderOpened },
  { path: '/tasks', label: '任务管理', icon: Tickets },
  { path: '/users', label: '用户管理', icon: User },
  { path: '/ai', label: 'AI 助手', icon: MagicStick }
]

const activeMenu = computed(() => route.path)
const isCollapsed = computed(() => !isMobile.value && session.sidebarCollapsed)

const syncViewport = () => {
  isMobile.value = window.innerWidth < 960
  if (!isMobile.value) {
    mobileDrawer.value = false
  }
}

const handleMenuToggle = () => {
  if (isMobile.value) {
    mobileDrawer.value = true
    return
  }
  session.toggleSidebar()
}

const handleLogout = () => {
  session.clearSession()
  router.push('/login')
}

onMounted(() => {
  window.addEventListener('resize', syncViewport)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncViewport)
})
</script>

<style scoped>
.layout-root {
  min-height: 100vh;
  display: grid;
  grid-template-columns: auto 1fr;
}

.sidebar {
  display: flex;
  flex-direction: column;
  width: 280px;
  padding: 20px 16px;
  background:
    radial-gradient(circle at top, rgba(15, 118, 110, 0.26), transparent 32%),
    linear-gradient(180deg, var(--nav-bg) 0%, var(--nav-bg-soft) 100%);
  color: #fff;
  transition: width 0.24s ease;
}

.sidebar.collapsed {
  width: 90px;
}

.sidebar.mobile {
  width: 100%;
  min-height: 100%;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 12px 18px;
}

.brand-mark {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(15, 118, 110, 1), rgba(245, 158, 11, 0.85));
  color: #fff;
  font-weight: 700;
  box-shadow: 0 14px 32px rgba(15, 118, 110, 0.3);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.brand-copy strong {
  font-size: 18px;
}

.brand-copy span {
  font-size: 12px;
  color: #9fb0c3;
}

.nav-menu {
  border-right: none;
  flex: 1;
}

.nav-menu :deep(.el-menu-item) {
  margin: 4px 0;
  border-radius: 14px;
}

.nav-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(15, 118, 110, 0.95), rgba(31, 41, 55, 0.95));
}

.sidebar-foot {
  margin-top: 18px;
  padding: 18px 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar-foot p {
  margin: 12px 0 0;
  color: #bdd0df;
  font-size: 13px;
  line-height: 1.7;
}

.layout-main {
  min-width: 0;
  padding: 18px;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 22px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(10px);
  border: 1px solid var(--surface-border);
  box-shadow: var(--shadow-card);
}

.topbar-left,
.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.crumbs {
  font-size: 12px;
  color: var(--text-subtle);
}

.topbar-title {
  margin-top: 4px;
  font-size: 20px;
  font-weight: 700;
}

.ghost-button {
  border: none;
  background: rgba(15, 118, 110, 0.1);
  color: var(--brand);
}

.profile-chip {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px 8px 8px;
  border-radius: 999px;
  background: rgba(15, 118, 110, 0.08);
  cursor: pointer;
}

.profile-meta {
  display: flex;
  flex-direction: column;
}

.profile-meta strong {
  font-size: 14px;
}

.profile-meta span {
  font-size: 12px;
  color: var(--text-secondary);
}

.main-content {
  margin-top: 18px;
}

.topbar-badge {
  white-space: nowrap;
}

@media (max-width: 960px) {
  .layout-root {
    grid-template-columns: 1fr;
  }

  .sidebar {
    display: none;
  }

  .layout-main {
    padding: 14px;
  }

  .topbar {
    padding: 14px 16px;
  }

  .topbar-right {
    gap: 10px;
  }

  .topbar-badge {
    display: none;
  }
}

@media (max-width: 640px) {
  .profile-meta {
    display: none;
  }

  .topbar-left {
    min-width: 0;
  }

  .topbar-title {
    font-size: 18px;
  }
}
</style>
