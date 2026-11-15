<template>
  <transition name="tts-rise">
    <div v-if="isSpeaking || isPaused" class="tts-player">
      <div class="tts-player-inner">
        <el-button class="tts-play" circle type="primary" :icon="isPaused ? VideoPlay : VideoPause" @click="togglePause" />
        <el-button class="tts-stop" circle text :icon="Close" title="停止" @click="stop" />

        <div class="tts-now" :title="nowText">{{ nowText || '正在朗读…' }}</div>

        <div class="tts-spacer" />

        <el-select v-model="settings.rate" size="small" class="tts-rate">
          <el-option v-for="r in rates" :key="r" :label="r + 'x'" :value="r" />
        </el-select>

        <el-select v-model="settings.voiceURI" size="small" class="tts-voice" clearable placeholder="自动音色">
          <el-option v-for="v in voices" :key="v.voiceURI" :label="v.name" :value="v.voiceURI" />
        </el-select>

        <router-link to="/settings?tab=tts" class="tts-settings" title="朗读设置">
          <el-icon><Setting /></el-icon>
        </router-link>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { VideoPlay, VideoPause, Close, Setting } from '@element-plus/icons-vue'
import { useTts } from '../composables/useTts'

defineProps({
  nowText: { type: String, default: '' }
})

const { isSpeaking, isPaused, settings, voices, pause, resume, stop } = useTts()

const rates = [0.5, 0.75, 1, 1.25, 1.5, 1.75, 2]

function togglePause() {
  if (isPaused.value) resume()
  else pause()
}
</script>

<style scoped>
.tts-player {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2000;
  width: max-content;
  max-width: calc(100vw - 40px);
}
.tts-player-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 999px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}
.tts-play {
  flex-shrink: 0;
}
.tts-stop {
  flex-shrink: 0;
}
.tts-now {
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  color: #606266;
}
.tts-spacer {
  flex: 1;
  min-width: 8px;
}
.tts-rate {
  width: 82px;
}
.tts-voice {
  width: 150px;
}
.tts-settings {
  display: flex;
  align-items: center;
  color: #909399;
  font-size: 18px;
  text-decoration: none;
  transition: color 0.2s;
}
.tts-settings:hover {
  color: #409eff;
}
.tts-rise-enter-active,
.tts-rise-leave-active {
  transition: all 0.25s ease;
}
.tts-rise-enter-from,
.tts-rise-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(16px);
}
</style>
