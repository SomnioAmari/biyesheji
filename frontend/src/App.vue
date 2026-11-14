<template>
  <router-view v-if="isPublic" />
  <el-container v-else class="app-container">
    <el-aside width="220px" class="app-aside">
      <div class="brand" @click="$router.push('/')">
        <el-icon><Reading /></el-icon>
        <span>纯净阅读</span>
      </div>
      <el-menu :default-active="activeMenu" router class="nav-menu">
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon><span>新闻主页</span>
        </el-menu-item>
        <el-menu-item index="/novel/search">
          <el-icon><Search /></el-icon><span>书籍搜索</span>
        </el-menu-item>
        <el-menu-item index="/bookshelf">
          <el-icon><Collection /></el-icon><span>书架</span>
        </el-menu-item>
        <el-menu-item index="/novel/mine">
          <el-icon><Upload /></el-icon><span>我的作品</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon><span>设置</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon><span>个人中心</span>
        </el-menu-item>

        <template v-if="isAdmin">
          <div class="menu-divider">管理员</div>
          <el-menu-item index="/admin/review">
            <el-icon><Checked /></el-icon><span>审核中心</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><UserFilled /></el-icon><span>用户管理</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="header-title">{{ currentTitle }}</div>
        <div class="user-actions">
          <el-tag v-if="isAdmin" type="warning" size="small">管理员</el-tag>
          <span class="username">{{ user?.nickname || user?.username }}</span>
          <el-button link type="primary" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from './store/user'

const route = useRoute()
const userStore = useUserStore()
const user = computed(() => userStore.user)
const isAdmin = computed(() => userStore.isAdmin)

const isPublic = computed(() => route.meta.public === true)

const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/article')) return '/'
  if (p.startsWith('/novel/search')) return '/novel/search'
  if (p.startsWith('/novel/mine')) return '/novel/mine'
  if (p.startsWith('/bookshelf')) return '/bookshelf'
  if (p.startsWith('/settings')) return '/settings'
  if (p.startsWith('/admin/review')) return '/admin/review'
  if (p.startsWith('/admin/users')) return '/admin/users'
  if (p.startsWith('/profile')) return '/profile'
  if (p.startsWith('/novel')) return '/novel/search'
  return p
})

const currentTitle = computed(() => {
  const m = activeMenu.value
  const titles = {
    '/': '新闻主页',
    '/novel/search': '书籍搜索',
    '/novel/mine': '我的作品',
    '/bookshelf': '书架',
    '/settings': '设置',
    '/profile': '个人中心',
    '/admin/review': '审核中心',
    '/admin/users': '用户管理'
  }
  return titles[m] || '纯净阅读'
})

function logout() {
  userStore.logout()
  window.location.href = '/login'
}
</script>

<style>
body {
  margin: 0;
  background: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.app-container {
  min-height: 100vh;
}
.app-aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
}
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 60px;
  padding: 0 20px;
  font-size: 19px;
  font-weight: 700;
  color: #409eff;
  cursor: pointer;
  border-bottom: 1px solid #f0f2f5;
}
.nav-menu {
  border-right: none;
  flex: 1;
}
.menu-divider {
  padding: 12px 20px 6px;
  font-size: 12px;
  color: #c0c4cc;
}
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
  height: 60px;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  white-space: nowrap;
}
.username {
  color: #606266;
  font-size: 14px;
}
.app-main {
  max-width: 1180px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}
</style>
