<template>
  <div v-if="article">
    <el-page-header @back="$router.back()" :content="'文章阅读'" />
    <el-card class="detail-card">
      <h1 class="detail-title">{{ article.title }}</h1>
      <div class="detail-meta">
        <el-tag size="small" type="info">{{ article.sourceName }}</el-tag>
        <span>{{ article.publishedAt }}</span>
        <a v-if="article.url" :href="article.url" target="_blank" rel="noopener">查看原文</a>
        <div class="meta-spacer" />
        <el-button size="small" type="primary" plain @click="toggleTts">
          <el-icon><Headset /></el-icon> {{ isSpeaking ? '停止朗读' : '朗读' }}
        </el-button>
      </div>
      <el-divider />
      <div class="detail-content" v-html="article.content || '<p>暂无正文</p>'"></div>
      <div class="detail-footer">
        <el-button :type="article.favorited ? 'danger' : 'primary'" @click="toggleFav">
          {{ article.favorited ? '取消收藏' : '收藏' }}
        </el-button>
      </div>
    </el-card>

    <TtsBar :now-text="currentText" />
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Headset } from '@element-plus/icons-vue'
import { getArticle, favoriteArticle, unfavoriteArticle, reportProgress } from '../api'
import { useTts, htmlToPlainText } from '../composables/useTts'
import TtsBar from '../components/TtsBar.vue'

const route = useRoute()
const article = ref(null)
const scrollPos = ref(0)

const { isSpeaking, speak, stop } = useTts()
const currentText = ref('')

function toggleTts() {
  if (isSpeaking.value) {
    stop()
    currentText.value = ''
    return
  }
  currentText.value = ''
  const text = htmlToPlainText(article.value?.content)
  speak(text, (chunk) => {
    currentText.value = chunk.text.length > 40 ? chunk.text.slice(0, 40) + '…' : chunk.text
  })
}

async function load() {
  const res = await getArticle(route.params.id)
  article.value = res.data
  if (article.value.scrollPosition) {
    nextTick(() => {
      const el = document.documentElement
      el.scrollTop = article.value.scrollPosition * (el.scrollHeight - el.clientHeight)
    })
  }
}

function onScroll() {
  const el = document.documentElement
  const max = el.scrollHeight - el.clientHeight
  scrollPos.value = max > 0 ? Math.min(1, Math.max(0, el.scrollTop / max)) : 0
}

async function toggleFav() {
  if (article.value.favorited) {
    await unfavoriteArticle(article.value.id)
    article.value.favorited = false
  } else {
    await favoriteArticle(article.value.id)
    article.value.favorited = true
  }
}

function saveProgress() {
  if (article.value) {
    reportProgress({ targetType: 0, targetId: article.value.id, scrollPosition: scrollPos.value })
  }
}

onMounted(() => {
  load()
  window.addEventListener('scroll', onScroll)
})

onBeforeUnmount(() => {
  stop()
  saveProgress()
  window.removeEventListener('scroll', onScroll)
})
</script>

<style scoped>
.detail-card {
  padding: 10px 30px;
}
.detail-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 12px;
}
.detail-meta {
  display: flex;
  align-items: center;
  gap: 14px;
  color: #909399;
  font-size: 14px;
}
.detail-content {
  line-height: 1.9;
  font-size: 16px;
  color: #303133;
}
.detail-content :deep(img) {
  max-width: 100%;
}
.detail-footer {
  margin-top: 24px;
}
.meta-spacer {
  flex: 1;
}
</style>
