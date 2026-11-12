<template>
  <div>
    <el-empty v-if="!list.length" description="书架为空，去「小说搜索」找书加入吧" />
    <el-card v-for="b in list" :key="b.bookId" class="shelf-item" shadow="hover">
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
        <el-button type="danger" size="small" plain @click="remove(b)">移除</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBookshelf, removeFromBookshelf } from '../api'

const router = useRouter()
const list = ref([])

async function load() {
  const res = await listBookshelf()
  list.value = res.data
}

function goDetail(bookId) {
  router.push(`/novel/${bookId}`)
}

function continueRead(b) {
  if (b.lastChapterId) {
    router.push(`/novel/${b.bookId}/${b.lastChapterId}`)
  } else {
    router.push(`/novel/${b.bookId}`)
  }
}

async function remove(b) {
  await ElMessageBox.confirm(`确定将「${b.name}」移出书架？`, '提示', { type: 'warning' })
  await removeFromBookshelf(b.bookId)
  ElMessage.success('已移除')
  load()
}

onMounted(load)
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
</style>
