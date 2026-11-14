<template>
  <div>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="用户名 / 昵称" style="width: 220px" clearable @keyup.enter="onFilter" />
      <el-button type="primary" @click="onFilter">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column prop="nickname" label="昵称" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'warning' : 'info'" size="small">{{ row.role === 1 ? '管理员' : '普通用户' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="warning" @click="toggleRole(row)">
            {{ row.role === 1 ? '取消管理员' : '设为管理员' }}
          </el-button>
          <el-button link type="primary" @click="openReset(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="total > size" class="pager">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="size"
        :current-page="page" @current-change="onPage" />
    </div>

    <el-dialog v-model="resetVisible" title="重置密码" width="420px">
      <el-form label-width="80px">
        <el-form-item label="新密码">
          <el-input v-model="newPassword" type="password" show-password placeholder="6~50 位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" @click="doReset">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, updateUserStatus, updateUserRole, resetUserPassword } from '../api'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 20
const keyword = ref('')
const loading = ref(false)

const resetVisible = ref(false)
const newPassword = ref('')
const resetTarget = ref(null)

async function load() {
  loading.value = true
  try {
    const res = await listUsers({ keyword: keyword.value.trim() || undefined, page: page.value, size })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function onFilter() {
  page.value = 1
  load()
}

function onPage(p) {
  page.value = p
  load()
}

async function toggleStatus(row) {
  const action = row.status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}用户「${row.username}」？`, '提示', { type: 'warning' })
  await updateUserStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success(`已${action}`)
  load()
}

async function toggleRole(row) {
  const action = row.role === 1 ? '取消管理员' : '设为管理员'
  await ElMessageBox.confirm(`确定${action}「${row.username}」？`, '提示', { type: 'warning' })
  await updateUserRole(row.id, row.role === 1 ? 0 : 1)
  ElMessage.success('已更新')
  load()
}

function openReset(row) {
  resetTarget.value = row
  newPassword.value = ''
  resetVisible.value = true
}

async function doReset() {
  if (!newPassword.value || newPassword.value.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  await resetUserPassword(resetTarget.value.id, newPassword.value)
  ElMessage.success('密码已重置')
  resetVisible.value = false
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
