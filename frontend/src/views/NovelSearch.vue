<template>
  <div>
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="输入书名 / 关键词搜索" clearable
        style="max-width: 420px" @keyup.enter="search" />
      <el-button type="primary" :loading="loading" @click="search">搜索</el-button>
    </div>

    <el-alert v-if="!sourcesCount" type="info" :closable="false" class="tip"
      title="暂无可用书源，请先在「书源管理」中导入书源规则" />

    <el-empty v-if="searched && !results.length" description="没有搜索到结果，换个关键词试试" />

    <div class="result-grid">
      <el-card v-for="(r, i) in results" :key="i" class="book-card" shadow="hover" @click="go(r)">
        <el-image :src="r.cover" fit="cover" class="book-cover">
          <template #error>
            <div class="cover-fallback">{{ r.name?.charAt(0) }}</div>
          </template>
        </el-image>
        <div class="book-name" :title="r.name">{{ r.name }}</div>
        <div class="book-author">{{ r.author || '未知作者' }}</div>
        <div class="book-source">{{ r.sourceName }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { searchNovel, listBookSources } from '../api'

const router = useRouter()
const keyword = ref('')
const results = ref([])
const loading = ref(false)
const searched = ref(false)
const sourcesCount = ref(0)

async function search() {
  if (!keyword.value.trim()) return
  loading.value = true
  searched.value = true
  try {
    const res = await searchNovel(keyword.value.trim())
    results.value = res.data
  } finally {
    loading.value = false
  }
}

function go(r) {
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
.tip {
  margin-bottom: 16px;
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
.book-source {
  color: #409eff;
  font-size: 12px;
  margin-top: 4px;
}
</style>
