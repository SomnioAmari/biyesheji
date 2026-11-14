<template>
  <div>
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="输入书名 / 作者 / 关键词搜索" clearable
        style="max-width: 420px" @keyup.enter="search" />
      <el-button type="primary" :loading="loading" @click="search">搜索</el-button>
    </div>

    <!-- 内部书库结果 -->
    <template v-if="librarySearched">
      <div v-if="libraryResults.length" class="section">
        <div class="section-title">
          <el-tag type="success" size="small">内部书库</el-tag>
          <span class="count">共 {{ libraryResults.length }} 本</span>
        </div>
        <div class="result-grid">
          <el-card v-for="r in libraryResults" :key="'l' + r.bookId" class="book-card" shadow="hover"
            @click="goLibrary(r.bookId)">
            <el-image :src="r.cover" fit="cover" class="book-cover">
              <template #error><div class="cover-fallback">{{ r.name?.charAt(0) }}</div></template>
            </el-image>
            <div class="book-name" :title="r.name">{{ r.name }}</div>
            <div class="book-author">{{ r.author || '未知作者' }}</div>
            <div class="book-meta">{{ r.category || '未分类' }} · {{ r.chapterCount }}章</div>
          </el-card>
        </div>
      </div>

      <div v-else class="empty-prompt">
        <el-empty description="内部书库没有找到相关书籍" :image-size="80" />
        <div class="prompt-actions">
          <el-button type="primary" :loading="sourceLoading" @click="searchSources">
            使用自定义书源搜索
          </el-button>
        </div>
        <div class="prompt-tip">也支持先在「设置 → 书源」里导入书源，再按书源聚合搜索</div>
      </div>
    </template>

    <!-- 书源搜索结果 -->
    <template v-if="sourceSearched">
      <div class="section">
        <div class="section-title">
          <el-tag type="warning" size="small">自定义书源</el-tag>
          <span class="count">共 {{ sourceResults.length }} 条</span>
        </div>
        <el-empty v-if="!sourceResults.length" description="书源也未搜索到结果，换关键词或检查书源" :image-size="80" />
        <div v-else class="result-grid">
          <el-card v-for="(r, i) in sourceResults" :key="'s' + i" class="book-card" shadow="hover" @click="goSource(r)">
            <el-image :src="r.cover" fit="cover" class="book-cover">
              <template #error><div class="cover-fallback">{{ r.name?.charAt(0) }}</div></template>
            </el-image>
            <div class="book-name" :title="r.name">{{ r.name }}</div>
            <div class="book-author">{{ r.author || '未知作者' }}</div>
            <div class="book-source">{{ r.sourceName }}</div>
          </el-card>
        </div>
      </div>
    </template>

    <el-empty v-if="!searched" description="输入关键词搜索内部书库，搜不到可再按自定义书源搜索" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { searchLibrary, searchNovelSources, listBookSources } from '../api'

const router = useRouter()
const keyword = ref('')
const searched = ref(false)
const loading = ref(false)
const librarySearched = ref(false)
const libraryResults = ref([])
const sourceSearched = ref(false)
const sourceLoading = ref(false)
const sourceResults = ref([])
const sourcesCount = ref(0)

async function search() {
  const kw = keyword.value.trim()
  if (!kw) return
  loading.value = true
  searched.value = true
  sourceSearched.value = false
  sourceResults.value = []
  try {
    const res = await searchLibrary(kw)
    libraryResults.value = res.data
    librarySearched.value = true
  } finally {
    loading.value = false
  }
}

async function searchSources() {
  const kw = keyword.value.trim()
  if (!kw) return
  sourceLoading.value = true
  try {
    const res = await searchNovelSources(kw)
    sourceResults.value = res.data
    sourceSearched.value = true
  } finally {
    sourceLoading.value = false
  }
}

function goLibrary(bookId) {
  router.push(`/novel/${bookId}`)
}

function goSource(r) {
  router.push({ path: '/novel/0', query: { sourceId: r.sourceId, bookUrl: r.bookUrl } })
}

onMounted(async () => {
  try {
    const res = await listBookSources()
    sourcesCount.value = res.data.length
  } catch (e) {
    sourcesCount.value = 0
  }
})
</script>

<style scoped>
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.section {
  margin-bottom: 8px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.count {
  color: #909399;
  font-size: 13px;
}
.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
}
.book-card {
  cursor: pointer;
  text-align: center;
}
.book-cover {
  width: 110px;
  height: 150px;
  border-radius: 4px;
  margin-bottom: 8px;
}
.cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e4e7ed;
  color: #909399;
  font-size: 30px;
}
.book-name {
  font-weight: 600;
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.book-author {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}
.book-meta {
  color: #67c23a;
  font-size: 12px;
  margin-top: 4px;
}
.book-source {
  color: #e6a23c;
  font-size: 12px;
  margin-top: 4px;
}
.empty-prompt {
  text-align: center;
  padding: 20px 0;
}
.prompt-actions {
  margin-top: 8px;
}
.prompt-tip {
  margin-top: 12px;
  color: #c0c4cc;
  font-size: 13px;
}
</style>
