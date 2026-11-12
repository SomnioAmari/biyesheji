<template>
  <div>
    <div class="toolbar">
      <el-select v-model="sourceId" clearable placeholder="全部来源" style="width: 220px" @change="onFilter">
        <el-option v-for="s in sources" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <el-empty v-if="!loading && list.length === 0" description="暂无资讯，请先在「新闻源」中添加并抓取" />

    <el-card v-for="a in list" :key="a.id" class="article-card" shadow="hover" @click="go(a.id)">
      <div class="article-title">{{ a.title }}</div>
      <div class="article-meta">
        <el-tag size="small" type="info">{{ a.sourceName }}</el-tag>
        <span class="time">{{ a.publishedAt || '时间未知' }}</span>
      </div>
      <div class="article-summary">{{ a.summary }}</div>
      <div class="article-footer" @click.stop>
        <el-button link :type="a.favorited ? 'danger' : 'primary'" @click="toggleFav(a)">
          {{ a.favorited ? '取消收藏' : '收藏' }}
        </el-button>
      </div>
    </el-card>

    <div v-if="total > size" class="pager">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="size"
        :current-page="page" @current-change="onPage" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { listArticles, listNewsSources, favoriteArticle, unfavoriteArticle } from '../api'

const router = useRouter()
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 20
const sourceId = ref(null)
const sources = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await listArticles({ page: page.value, size, sourceId: sourceId.value || undefined })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadSources() {
  const res = await listNewsSources()
  sources.value = res.data
}

function onPage(p) {
  page.value = p
  load()
}

function onFilter() {
  page.value = 1
  load()
}

function go(id) {
  router.push('/article/' + id)
}

async function toggleFav(a) {
  if (a.favorited) {
    await unfavoriteArticle(a.id)
    a.favorited = false
    ElMessage.success('已取消收藏')
  } else {
    await favoriteArticle(a.id)
    a.favorited = true
    ElMessage.success('已收藏')
  }
}

onMounted(() => {
  loadSources()
  load()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.article-card {
  margin-bottom: 14px;
  cursor: pointer;
}
.article-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.article-meta {
  margin: 8px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}
.time {
  color: #909399;
  font-size: 13px;
}
.article-summary {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.article-footer {
  margin-top: 8px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
