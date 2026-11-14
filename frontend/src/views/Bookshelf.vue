<template>
  <div>
    <el-tabs v-model="activeTab">
      <!-- 收藏的书籍 -->
      <el-tab-pane label="收藏的书籍" name="novel">
        <el-empty v-if="!novels.length" description="书架为空，去「书籍搜索」找书加入吧" />
        <el-card v-for="b in novels" :key="b.bookId" class="shelf-item" shadow="hover">
          <div class="shelf-cover" @click="goDetail(b.bookId)">
            <el-image :src="b.cover" fit="cover" class="cover">
              <template #error>
                <div class="cover-fallback">{{ b.name?.charAt(0) }}</div>
              </template>
            </el-image>
          </div>
          <div class="shelf-info" @click="goDetail(b.bookId)">
            <div class="name">{{ b.name }}</div>
            <div class="author">{{ b.author }}</div>
            <div class="meta">共 {{ b.chapterCount }} 章 · {{ b.lastChapterName ? '读到：' + b.lastChapterName : '未开始阅读' }}</div>
          </div>
          <div class="shelf-actions">
            <el-button type="primary" size="small" @click="continueRead(b)">继续阅读</el-button>
            <el-button type="danger" size="small" plain @click="removeNovel(b)">移除</el-button>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 收藏的新闻 -->
      <el-tab-pane label="收藏的新闻" name="article">
        <el-empty v-if="!articles.length" description="还没有收藏的新闻，去「新闻主页」收藏吧" />
        <el-card v-for="a in articles" :key="a.id" class="article-card" shadow="hover" @click="goArticle(a.id)">
          <div class="article-title">{{ a.title }}</div>
          <div class="article-meta">
            <el-tag size="small" type="info">{{ a.sourceName }}</el-tag>
            <span class="time">{{ a.publishedAt || '时间未知' }}</span>
          </div>
          <div class="article-summary">{{ a.summary }}</div>
          <div class="article-footer" @click.stop>
            <el-button link type="danger" @click="removeArticle(a)">取消收藏</el-button>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBookshelf, removeFromBookshelf, listFavoriteArticles, unfavoriteArticle } from '../api'

const router = useRouter()
const activeTab = ref('novel')
const novels = ref([])
const articles = ref([])

async function loadNovels() {
  const res = await listBookshelf()
  novels.value = res.data
}

async function loadArticles() {
  const res = await listFavoriteArticles()
  articles.value = res.data
}

function goDetail(bookId) {
  router.push(`/novel/${bookId}`)
}

function goArticle(id) {
  router.push('/article/' + id)
}

function continueRead(b) {
  if (b.lastChapterId) {
    router.push(`/novel/${b.bookId}/${b.lastChapterId}`)
  } else {
    router.push(`/novel/${b.bookId}`)
  }
}

async function removeNovel(b) {
  await ElMessageBox.confirm(`确定将「${b.name}」移出书架？`, '提示', { type: 'warning' })
  await removeFromBookshelf(b.bookId)
  ElMessage.success('已移除')
  loadNovels()
}

async function removeArticle(a) {
  await ElMessageBox.confirm(`确定取消收藏「${a.title}」？`, '提示', { type: 'warning' })
  await unfavoriteArticle(a.id)
  ElMessage.success('已取消收藏')
  loadArticles()
}

onMounted(() => {
  loadNovels()
  loadArticles()
})
</script>

<style scoped>
.shelf-item {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 14px;
}
.shelf-cover {
  cursor: pointer;
  flex-shrink: 0;
}
.cover {
  width: 70px;
  height: 95px;
  border-radius: 4px;
}
.cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e4e7ed;
  color: #909399;
  font-size: 26px;
}
.shelf-info {
  flex: 1;
  cursor: pointer;
}
.name {
  font-size: 17px;
  font-weight: 600;
}
.author {
  color: #909399;
  font-size: 13px;
  margin: 4px 0;
}
.meta {
  color: #606266;
  font-size: 13px;
}
.shelf-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.article-card {
  margin-bottom: 14px;
  cursor: pointer;
}
.article-title {
  font-size: 17px;
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
</style>
