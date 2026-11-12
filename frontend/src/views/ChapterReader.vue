<template>
  <div v-if="content">
    <el-page-header @back="backToBook" :content="content.name" />
    <el-card class="reader-card">
      <div class="chapter-content">{{ content.content }}</div>
      <div class="chapter-nav">
        <el-button :disabled="!content.prevId" @click="goChapter(content.prevId)">上一章</el-button>
        <el-button type="primary" :disabled="!content.nextId" @click="goChapter(content.nextId)">下一章</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getChapterContent, reportProgress } from '../api'

const route = useRoute()
const router = useRouter()
const bookId = Number(route.params.bookId)
const content = ref(null)

async function load(chapterId) {
  const res = await getChapterContent(bookId, chapterId)
  content.value = res.data
  reportProgress({ targetType: 1, targetId: bookId, chapterId: Number(chapterId) })
  window.scrollTo(0, 0)
}

function goChapter(id) {
  router.push(`/novel/${bookId}/${id}`)
}

function backToBook() {
  router.push(`/novel/${bookId}`)
}

watch(() => route.params.chapterId, (id) => {
  if (id) load(id)
}, { immediate: true })
</script>

<style scoped>
.reader-card {
  padding: 10px 30px;
}
.chapter-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 2;
  font-size: 17px;
  color: #303133;
}
.chapter-nav {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>
