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
        <el-tab-pane label="朗读" name="tts" lazy>
          <el-form label-width="80px" class="tts-form">
            <el-form-item label="音色">
              <el-select v-model="settings.voiceURI" placeholder="自动选择中文音色" clearable style="width: 340px">
                <el-option v-for="v in voices" :key="v.voiceURI" :label="v.name" :value="v.voiceURI" />
              </el-select>
            </el-form-item>
            <el-form-item label="语速">
              <el-slider v-model="settings.rate" :min="0.5" :max="2" :step="0.1" style="width: 340px" />
            </el-form-item>
            <el-form-item label="音量">
              <el-slider v-model="settings.volume" :min="0" :max="1" :step="0.1" style="width: 340px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Headset" @click="tryVoice">试听</el-button>
              <span class="tip">音色来自浏览器/系统自带语音，不同浏览器音色不同</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Headset } from '@element-plus/icons-vue'
import NewsSources from './NewsSources.vue'
import BookSources from './BookSources.vue'
import { useTts } from '../composables/useTts'

const route = useRoute()
const tabs = ['news', 'book', 'tts']
const activeTab = ref(tabs.includes(route.query.tab) ? route.query.tab : 'news')

watch(() => route.query.tab, (t) => {
  if (tabs.includes(t)) activeTab.value = t
})

const { settings, voices, refreshVoices, speak } = useTts()

function tryVoice() {
  refreshVoices()
  speak('你好，这是朗读试听。语音音色、语速和音量都可以在这里调整。')
}
</script>

<style scoped>
.tts-form {
  max-width: 560px;
  padding-top: 8px;
}
.tip {
  color: #909399;
  font-size: 13px;
}
</style>
