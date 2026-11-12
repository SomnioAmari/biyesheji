<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" :icon="DocumentAdd" @click="openPaste">粘贴导入</el-button>
      <el-button type="primary" :icon="Link" plain @click="openUrl">URL 导入</el-button>
      <span class="tip">支持导入 legado（开源阅读）书源与本系统规则，单个或合集均可</span>
    </div>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="name" label="书源名称" min-width="140" />
      <el-table-column prop="url" label="书源首页" min-width="220" show-overflow-tooltip />
      <el-table-column label="启用" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.enabled" :active-value="1" :inactive-value="0" @change="toggleEnabled(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 粘贴导入 -->
    <el-dialog v-model="pasteVisible" title="粘贴导入书源" width="680px">
      <el-alert type="info" :closable="false" class="mb"
        title="支持 legado（开源阅读）书源 JSON 或本系统规则 JSON，也支持书源合集数组" />
      <el-input v-model="pasteJson" type="textarea" :rows="18" spellcheck="false"
        placeholder='粘贴书源 JSON，例如 legado 书源的 {"bookSourceName":"...","ruleSearch":{...}}' />
      <template #footer>
        <el-button @click="pasteVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="doPasteImport">导入</el-button>
      </template>
    </el-dialog>

    <!-- URL 导入 -->
    <el-dialog v-model="urlVisible" title="URL 导入书源" width="520px">
      <el-alert type="info" :closable="false" class="mb"
        title="输入一个指向书源 JSON 的链接（单个书源或书源合集），系统会自动抓取并导入" />
      <el-input v-model="importUrl" placeholder="https://example.com/bookSource.json" @keyup.enter="doUrlImport" />
      <template #footer>
        <el-button @click="urlVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="doUrlImport">导入</el-button>
      </template>
    </el-dialog>

    <!-- 编辑 -->
    <el-dialog v-model="editVisible" title="编辑书源" width="640px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="书源首页"><el-input v-model="form.url" /></el-form-item>
        <el-form-item label="规则JSON">
          <el-input v-model="form.ruleJson" type="textarea" :rows="16" spellcheck="false" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DocumentAdd, Link } from '@element-plus/icons-vue'
import {
  listBookSources, updateBookSource, deleteBookSource,
  importBookSources, importBookSourcesByUrl
} from '../api'

const list = ref([])
const loading = ref(false)
const importing = ref(false)

const pasteVisible = ref(false)
const urlVisible = ref(false)
const editVisible = ref(false)
const pasteJson = ref('')
const importUrl = ref('')
const form = ref({})

async function load() {
  loading.value = true
  try {
    const res = await listBookSources()
    list.value = res.data
  } finally {
    loading.value = false
  }
}

function openPaste() {
  pasteJson.value = ''
  pasteVisible.value = true
}

function openUrl() {
  importUrl.value = ''
  urlVisible.value = true
}

function openEdit(row) {
  form.value = { ...row }
  editVisible.value = true
}

async function doPasteImport() {
  if (!pasteJson.value.trim()) {
    ElMessage.warning('请粘贴书源 JSON')
    return
  }
  importing.value = true
  try {
    const res = await importBookSources({ ruleJson: pasteJson.value })
    ElMessage.success(`成功导入 ${res.data.length} 个书源`)
    pasteVisible.value = false
    load()
  } finally {
    importing.value = false
  }
}

async function doUrlImport() {
  if (!importUrl.value.trim()) {
    ElMessage.warning('请输入 URL')
    return
  }
  importing.value = true
  try {
    const res = await importBookSourcesByUrl({ url: importUrl.value.trim() })
    ElMessage.success(`成功导入 ${res.data.length} 个书源`)
    urlVisible.value = false
    load()
  } finally {
    importing.value = false
  }
}

async function saveEdit() {
  if (!form.value.name || !form.value.url || !form.value.ruleJson) {
    ElMessage.warning('请填写名称、首页和规则')
    return
  }
  await updateBookSource(form.value.id, {
    name: form.value.name, url: form.value.url,
    ruleJson: form.value.ruleJson, enabled: form.value.enabled
  })
  ElMessage.success('保存成功')
  editVisible.value = false
  load()
}

async function toggleEnabled(row) {
  await updateBookSource(row.id, { name: row.name, url: row.url, ruleJson: row.ruleJson, enabled: row.enabled })
  ElMessage.success('已更新')
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除书源「${row.name}」？`, '提示', { type: 'warning' })
  await deleteBookSource(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}
.tip {
  color: #909399;
  font-size: 13px;
}
.mb {
  margin-bottom: 12px;
}
</style>
