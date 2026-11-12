<template>
  <router-view v-if="isPublic" />
  <el-container v-else class="app-container">
    <el-header class="app-header">
      <div class="brand" @click="$router.push('/')">纯净阅读</div>
      <el-menu mode="horizontal" :default-active="activeMenu" router :ellipsis="false" class="nav-menu">
        <el-menu-item index="/">新闻主页</el-menu-item>
        <el-menu-item index="/news-sources">新闻源</el-menu-item>
        <el-menu-item index="/novel/search">小说搜索</el-menu-item>
        <el-menu-item index="/bookshelf">书架</el-menu-item>
        <el-menu-item index="/book-sources">书源管理</el-menu-item>
        <el-menu-item index="/profile">个人中心</el-menu-item>
      </el-menu>
      <div class="user-actions">
        <span class="username">{{ user?.nickname || user?.username }}</span>
        <el-button link type="primary" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from './store/user'

const route = useRoute()
const userStore = useUserStore()
const user = computed(() => userStore.user)

const isPublic = computed(() => route.meta.public === true)

const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/article')) return '/'
  if (p.startsWith('/novel')) return '/novel/search'
  return p
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
}
.app-container {
  min-height: 100vh;
}
.app-header {
  display: flex;
  align-items: center;
  gap: 24px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}
.brand {
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
  cursor: pointer;
  white-space: nowrap;
}
.nav-menu {
  flex: 1;
  border-bottom: none;
}
.user-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}
.username {
  color: #606266;
  font-size: 14px;
}
.app-main {
  max-width: 1100px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}
</style>
