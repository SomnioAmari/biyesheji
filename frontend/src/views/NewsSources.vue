<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增新闻源</el-button>
      <el-button :icon="MagicStick" :loading="importing" @click="importRecommended">添加推荐源</el-button>
    </div>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">
          <el-tag :type="row.type === 0 ? 'success' : 'warning'" size="small">{{ row.type === 0 ? 'RSS' : '网页' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="url" label="地址" min-width="220" show-overflow-tooltip />
      <el-table-column prop="weight" label="权重" width="80" />
      <el-table-column prop="fetchInterval" label="间隔(分)" width="90" />
      <el-table-column label="启用" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.enabled" :active-value="1" :inactive-value="0" @change="toggleEnabled(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="lastFetchAt" label="上次抓取" width="160" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="success" @click="fetchNow(row)">抓取</el-button>
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑新闻源' : '新增新闻源'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio :value="0">RSS</el-radio>
            <el-radio :value="1">网页源</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="地址"><el-input v-model="form.url" placeholder="RSS 地址或列表页地址" /></el-form-item>
        <el-form-item label="权重"><el-input-number v-model="form.weight" :min="1" :max="10" /></el-form-item>
        <el-form-item label="间隔(分)"><el-input-number v-model="form.fetchInterval" :min="1" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.category" /></el-form-item>
        <el-form-item label="规则JSON">
          <el-input v-model="form.ruleJson" type="textarea" :rows="3"
            placeholder='网页源可填，例如 {"listSelector":".news-list a","contentSelector":"article"}' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick } from '@element-plus/icons-vue'
import { listNewsSources, createNewsSource, updateNewsSource, deleteNewsSource, fetchNewsSource } from '../api'
import { RECOMMENDED_NEWS_SOURCES } from '../recommended'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = ref({})
const importing = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await listNewsSources()
    list.value = res.data
  } finally {
    loading.value = false
  }
}

async function importRecommended() {
  const existingUrls = new Set(list.value.map((s) => s.url))
  const toAdd = RECOMMENDED_NEWS_SOURCES.filter((s) => !existingUrls.has(s.url))
  if (!toAdd.length) {
    ElMessage.info('推荐源都已添加过')
    return
  }
  importing.value = true
  try {
    for (const s of toAdd) {
      await createNewsSource({ type: 0, name: s.name, url: s.url, category: s.category, weight: s.weight, fetchInterval: s.fetchInterval, enabled: 1 })
    }
    ElMessage.success(`已添加 ${toAdd.length} 个推荐源`)
    load()
  } finally {
    importing.value = false
  }
}

function openDialog(row) {
  form.value = row
    ? { ...row }
    : { id: null, type: 0, name: '', url: '', category: '', ruleJson: '', weight: 5, fetchInterval: 60, enabled: 1 }
  dialogVisible.value = true
}

function toReq(r) {
  return {
    type: r.type, name: r.name, url: r.url, category: r.category,
    ruleJson: r.ruleJson, weight: r.weight, fetchInterval: r.fetchInterval, enabled: r.enabled
  }
}

async function save() {
  if (!form.value.name || !form.value.url) {
    ElMessage.warning('请填写名称和地址')
    return
  }
  if (form.value.id) {
    await updateNewsSource(form.value.id, toReq(form.value))
  } else {
    await createNewsSource(toReq(form.value))
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function toggleEnabled(row) {
  await updateNewsSource(row.id, toReq(row))
  ElMessage.success('已更新')
}

async function fetchNow(row) {
  const res = await fetchNewsSource(row.id)
  if (res.data.status === 1) {
    ElMessage.success(`抓取成功，入库 ${res.data.fetchedCount} 条`)
  } else {
    ElMessage.error(`抓取失败：${res.data.message}`)
  }
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除新闻源「${row.name}」？`, '提示', { type: 'warning' })
  await deleteNewsSource(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
</style>
