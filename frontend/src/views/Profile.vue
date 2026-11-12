<template>
  <div>
    <el-card class="profile-card">
      <h3>个人信息</h3>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ user?.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ user?.email || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button type="danger" plain @click="logout">退出登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '../store/user'

const store = useUserStore()
const user = computed(() => store.user)

function logout() {
  store.logout()
  window.location.href = '/login'
}
</script>

<style scoped>
.profile-card {
  max-width: 500px;
}
.actions {
  margin-top: 20px;
}
</style>
