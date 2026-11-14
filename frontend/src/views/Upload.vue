<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openDialog">上传书籍</el-button>
      <span class="tip">上传后需管理员审核通过，才会进入内部书库供其他用户搜索</span>
    </div>

    <el-table :data="list" v-loading="loading">
      <el-table-column label="封面" width="70">
        <template #default="{ row }">
          <el-image :src="row.cover" fit="cover" class="cover" style="width:44px;height:60px">
            <template #error><div class="cover-fb">{{ row.name?.charAt(0) }}</div></template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="书名" min-width="150" show-overflow-tooltip />
      <el-table-column prop="author" label="作者" width="120" />
      <el-table-column prop="category" label="分类" width="100">
        <template #default="{ row }">{{ row.category || '-' }}</template>
      </el-table-column>
      <el-table-column prop="chapterCount" label="章节" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.reviewStatus)" size="small">{{ statusText(row.reviewStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewReason" label="驳回原因" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.reviewReason || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="上传时间" width="160" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goDetail(row)">查看</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="上传书籍" width="640px" :close-on-click-modal="false">
      <el-form :model="form" label-width="90px">
        <el-form-item label="书名" required><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="作者"><el-input v-model="form.author" placeholder="可选，默认佚名" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.category" placeholder="如：玄幻 / 都市 / 科幻" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.intro" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="封面">
          <el-upload :auto-upload="false" :limit="1" accept="image/*" list-type="picture"
            :on-change="onCoverChange" :on-remove="onCoverRemove">
            <el-button :icon="Plus">选择图片</el-button>
            <template #tip><div class="up-tip">可选，jpg/png/gif/webp</div></template>
          </el-upload>
        </el-form-item>
        <el-form-item label="正文来源">
          <el-radio-group v-model="form.source">
            <el-radio value="file">上传 TXT 文件</el-radio>
            <el-radio value="paste">粘贴全文</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.source === 'file'" label="TXT 文件">
          <el-upload :auto-upload="false" :limit="1" accept=".txt,text/plain"
            :on-change="onTxtChange" :on-remove="onTxtRemove">
            <el-button :icon="Document">选择 TXT</el-button>
            <template #tip><div class="up-tip">自动按「第X章」等标题拆分章节</div></template>
          </el-upload>
        </el-form-item>
        <el-form-item v-else label="正文">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="粘贴全文，自动按章节标题拆分" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Document } from '@element-plus/icons-vue'
import { listMyNovels, uploadNovel, deleteMyNovel } from '../api'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({})
const coverFile = ref(null)
const txtFile = ref(null)

async function load() {
  loading.value = true
  try {
    const res = await listMyNovels()
    list.value = res.data
  } finally {
    loading.value = false
  }
}

function openDialog() {
  form.value = { title: '', author: '', category: '', intro: '', source: 'file', content: '' }
  coverFile.value = null
  txtFile.value = null
  dialogVisible.value = true
}

function onCoverChange(file) {
  coverFile.value = file.raw
}

function onCoverRemove() {
  coverFile.value = null
}

function onTxtChange(file) {
  txtFile.value = file.raw
}

function onTxtRemove() {
  txtFile.value = null
}

async function submit() {
  if (!form.value.title?.trim()) {
    ElMessage.warning('请填写书名')
    return
  }
  if (form.value.source === 'file' && !txtFile.value) {
    ElMessage.warning('请选择 TXT 文件')
    return
  }
  if (form.value.source === 'paste' && !form.value.content?.trim()) {
    ElMessage.warning('请粘贴正文内容')
    return
  }
  const fd = new FormData()
  fd.append('title', form.value.title.trim())
  if (form.value.author?.trim()) fd.append('author', form.value.author.trim())
  if (form.value.category?.trim()) fd.append('category', form.value.category.trim())
  if (form.value.intro?.trim()) fd.append('intro', form.value.intro.trim())
  if (coverFile.value) fd.append('cover', coverFile.value)
  if (form.value.source === 'file') {
    fd.append('file', txtFile.value)
  } else {
    fd.append('content', form.value.content)
  }

  submitting.value = true
  try {
    await uploadNovel(fd)
    ElMessage.success('上传成功，等待管理员审核')
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

function goDetail(row) {
  router.push(`/novel/${row.bookId}`)
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除「${row.name}」？删除后章节内容一并移除。`, '提示', { type: 'warning' })
  await deleteMyNovel(row.bookId)
  ElMessage.success('已删除')
  load()
}

function statusText(s) {
  return s === 0 ? '待审核' : s === 1 ? '已通过' : '已拒绝'
}

function statusType(s) {
  return s === 0 ? 'info' : s === 1 ? 'success' : 'danger'
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
.up-tip {
  font-size: 12px;
  color: #c0c4cc;
}
.cover-fb {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e4e7ed;
  color: #909399;
}
</style>
