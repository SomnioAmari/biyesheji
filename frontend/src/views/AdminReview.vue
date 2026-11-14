<template>
  <div>
    <div class="toolbar">
      <el-select v-model="reviewStatus" clearable placeholder="全部状态" style="width: 140px" @change="onFilter">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-input v-model="keyword" placeholder="书名 / 作者" style="width: 220px" clearable @keyup.enter="onFilter" />
      <el-button type="primary" @click="onFilter">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading">
      <el-table-column label="封面" width="70">
        <template #default="{ row }">
          <el-image :src="row.cover" fit="cover" style="width:44px;height:60px">
            <template #error><div class="cover-fb">{{ row.name?.charAt(0) }}</div></template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="书名" min-width="160" show-overflow-tooltip />
      <el-table-column prop="author" label="作者" width="120" />
      <el-table-column prop="category" label="分类" width="90">
        <template #default="{ row }">{{ row.category || '-' }}</template>
      </el-table-column>
      <el-table-column prop="uploaderName" label="上传者" width="110" />
      <el-table-column prop="chapterCount" label="章节" width="70" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.reviewStatus)" size="small">{{ statusText(row.reviewStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="提交时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goDetail(row)">查看</el-button>
          <el-button v-if="row.reviewStatus !== 1" link type="success" @click="approve(row)">通过</el-button>
          <el-button v-if="row.reviewStatus !== 2" link type="warning" @click="openReject(row)">驳回</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="total > size" class="pager">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="size"
        :current-page="page" @current-change="onPage" />
    </div>

    <el-dialog v-model="rejectVisible" title="驳回书籍" width="480px">
      <el-form label-width="80px">
        <el-form-item label="驳回原因">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请填写驳回原因（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="warning" @click="doReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAdminNovels, reviewNovel, deleteAdminNovel } from '../api'

const router = useRouter()
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const reviewStatus = ref(null)
const loading = ref(false)

const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTarget = ref(null)

async function load() {
  loading.value = true
  try {
    const res = await listAdminNovels({
      reviewStatus: reviewStatus.value ?? undefined,
      keyword: keyword.value.trim() || undefined,
      page: page.value,
      size
    })
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

function goDetail(row) {
  router.push(`/novel/${row.bookId}`)
}

async function approve(row) {
  await ElMessageBox.confirm(`确定通过「${row.name}」并入库？`, '提示', { type: 'success' })
  await reviewNovel(row.bookId, { approved: true })
  ElMessage.success('已通过并入库')
  load()
}

function openReject(row) {
  rejectTarget.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

async function doReject() {
  await reviewNovel(rejectTarget.value.bookId, { approved: false, reason: rejectReason.value })
  ElMessage.success('已驳回')
  rejectVisible.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除「${row.name}」及其全部章节？`, '提示', { type: 'warning' })
  await deleteAdminNovel(row.bookId)
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
  gap: 12px;
  margin-bottom: 16px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
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
