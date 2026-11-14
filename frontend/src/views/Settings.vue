<template>
  <div>
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="新闻源" name="news" lazy>
          <NewsSources />
        </el-tab-pane>
        <el-tab-pane label="书源" name="book" lazy>
          <BookSources />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import NewsSources from './NewsSources.vue'
import BookSources from './BookSources.vue'

const route = useRoute()
const activeTab = ref(route.query.tab === 'book' ? 'book' : 'news')

watch(() => route.query.tab, (t) => {
  if (t === 'news' || t === 'book') activeTab.value = t
})
</script>
