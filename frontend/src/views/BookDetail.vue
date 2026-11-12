<template>
  <div v-if="detail">
    <el-page-header @back="$router.back()" :content="detail.name" />
    <el-card class="detail-card">
      <div class="book-head">
        <el-image :src="detail.cover" fit="cover" class="cover">
          <template #error>
            <div class="cover-fallback">{{ detail.name?.charAt(0) }}</div>
          </template>
        </el-image>
        <div class="book-info">
          <h2 class="name">{{ detail.name }}</h2>
          <p class="line">作者：{{ detail.author || '未知' }}</p>
          <p class="line">章节数：{{ detail.chapterCount }}</p>
          <p class="intro">{{ detail.intro || '暂无简介' }}</p>
          <div class="actions">
            <el-button v-if="!detail.inShelf" type="primary" @click="addShelf">加入书架</el-button>
            <el-button v-else disabled>已在书架</el-button>
            <el-button :loading="catalogLoading" @click="loadCatalog">查看目录</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-card v-if="catalog.length" class="catalog-card">
      <div class="catalog-title">目录（{{ catalog.length }} 章）</div>
      <el-scrollbar height="420px">
        <div v-for="c in catalog" :key="c.id" class="chapter-item" @click="goChapter(c.id)">{{ c.name }}</div>
      </el-scrollbar>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getNovelDetail, getNovelDetailById, getNovelCatalog, addToBookshelf } from '../api'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const bookId = ref(null)
const catalog = ref([])
const catalogLoading = ref(false)

async function load() {
  if (route.query.sourceId && route.query.bookUrl) {
    const res = await getNovelDetail(route.query.sourceId, route.query.bookUrl)
    detail.value = res.data
    bookId.value = res.data.bookId
  } else {
    bookId.value = Number(route.params.bookId)
    const res = await getNovelDetailById(bookId.value)
    detail.value = res.data
  }
}

async function loadCatalog() {
  catalogLoading.value = true
  try {
    const res = await getNovelCatalog(bookId.value)
    catalog.value = res.data
  } finally {
    catalogLoading.value = false
  }
}

async function addShelf() {
  await addToBookshelf(bookId.value)
  detail.value.inShelf = true
  ElMessage.success('已加入书架')
}

function goChapter(chapterId) {
  router.push(`/novel/${bookId.value}/${chapterId}`)
}

onMounted(load)
</script>

<style scoped>
.detail-card {
  margin-bottom: 16px;
}
.book-head {
  display: flex;
  gap: 24px;
}
.cover {
  width: 140px;
  height: 190px;
  border-radius: 6px;
  flex-shrink: 0;
}
.cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e4e7ed;
  color: #909399;
  font-size: 40px;
}
.book-info {
  flex: 1;
}
.name {
  margin: 0 0 10px;
}
.line {
  color: #606266;
  margin: 6px 0;
}
.intro {
  color: #909399;
  line-height: 1.7;
  max-height: 120px;
  overflow: auto;
}
.actions {
  margin-top: 16px;
}
.catalog-title {
  font-weight: 600;
  margin-bottom: 12px;
}
.chapter-item {
  padding: 10px 6px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  color: #303133;
}
.chapter-item:hover {
  color: #409eff;
}
</style>
