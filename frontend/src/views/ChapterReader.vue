<template>
  <div class="reader" v-if="content" :style="{ background: theme.bg, color: theme.color }">
    <div class="reader-top" :style="{ background: theme.bg, color: theme.color, borderColor: theme.color + '22' }">
      <el-button link @click="backToBook">
        <el-icon><ArrowLeft /></el-icon> 目录
      </el-button>
      <div class="chapter-title" :title="content.name">{{ content.name }}</div>
      <div class="top-actions">
        <el-button link @click="toggleTts">
          <el-icon><Headset /></el-icon> {{ isSpeaking ? '停止' : '朗读' }}
        </el-button>
        <el-button link @click="settingsVisible = true">
          <el-icon><Setting /></el-icon> 设置
        </el-button>
      </div>
    </div>

    <div class="reader-body" :style="{ maxWidth: widthValue }">
      <div class="chapter-content" :style="contentStyle">
        <p v-for="(p, i) in currentParagraphs" :key="i" :class="{ 'is-reading': i === activePara }" :style="{ marginBottom: settings.paragraphSpacing + 'em' }">{{ p }}</p>
      </div>

      <div v-if="totalPages > 1" class="page-nav">
        <el-button :disabled="currentPage <= 0" @click="goPage(currentPage - 1)">上一页</el-button>
        <span class="page-indicator">{{ currentPage + 1 }} / {{ totalPages }}</span>
        <el-button :disabled="currentPage >= totalPages - 1" @click="goPage(currentPage + 1)">下一页</el-button>
      </div>

      <div class="chapter-nav">
        <el-button :disabled="!content.prevId" @click="goChapter(content.prevId)">上一章</el-button>
        <el-button type="primary" :disabled="!content.nextId" @click="goChapter(content.nextId)">下一章</el-button>
      </div>
    </div>

    <el-drawer v-model="settingsVisible" title="阅读设置" size="300px">
      <div class="settings">
        <div class="setting-row">
          <div class="label">字号</div>
          <div class="control">
            <el-button size="small" @click="changeFont(-1)">A-</el-button>
            <span class="font-size">{{ settings.fontSize }}</span>
            <el-button size="small" @click="changeFont(1)">A+</el-button>
          </div>
        </div>

        <div class="setting-row">
          <div class="label">行距</div>
          <el-slider v-model="settings.lineHeight" :min="1.5" :max="3" :step="0.1" @change="save" />
        </div>

        <div class="setting-row">
          <div class="label">段落间距</div>
          <el-slider v-model="settings.paragraphSpacing" :min="0" :max="2" :step="0.1" @change="save" />
        </div>

        <div class="setting-row">
          <div class="label">字间距</div>
          <el-slider v-model="settings.letterSpacing" :min="0" :max="4" :step="0.1" @change="save" />
        </div>

        <div class="setting-row">
          <div class="label">字体</div>
          <el-select v-model="settings.fontFamily" size="small" @change="save">
            <el-option v-for="f in fontOptions" :key="f.key" :label="f.label" :value="f.key" />
          </el-select>
        </div>

        <div class="setting-row">
          <div class="label">页宽</div>
          <el-radio-group v-model="settings.width" size="small" @change="save">
            <el-radio-button value="narrow">窄</el-radio-button>
            <el-radio-button value="medium">中</el-radio-button>
            <el-radio-button value="wide">宽</el-radio-button>
          </el-radio-group>
        </div>

        <div class="setting-row">
          <div class="label">背景</div>
        </div>
        <div class="theme-grid">
          <div v-for="t in themes" :key="t.key" class="theme-item"
            :class="{ active: settings.theme === t.key }"
            :style="{ background: t.bg }" @click="selectTheme(t.key)">
            <span>{{ t.name }}</span>
          </div>
        </div>
        <div class="setting-row">
          <div class="label">自定义</div>
          <el-color-picker v-model="settings.customBg" @change="onCustomColor" />
        </div>
      </div>
    </el-drawer>

    <TtsBar :now-text="nowText" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getChapterContent, reportProgress } from '../api'
import { ArrowLeft, Setting, Headset } from '@element-plus/icons-vue'
import { useTts } from '../composables/useTts'
import TtsBar from '../components/TtsBar.vue'

const route = useRoute()
const router = useRouter()
const bookId = Number(route.params.bookId)
const content = ref(null)
const settingsVisible = ref(false)

const { isSpeaking, speak, stop } = useTts()
const activePara = ref(-1)
const nowText = ref('')

function toggleTts() {
  if (isSpeaking.value) {
    stop()
    activePara.value = -1
    nowText.value = ''
    return
  }
  activePara.value = -1
  nowText.value = ''
  const text = currentParagraphs.value.join('\n')
  speak(text, (chunk) => {
    activePara.value = chunk.paraIndex
    nowText.value = chunk.text
  })
}

const themes = [
  { key: 'white', name: '白色', bg: '#ffffff', color: '#303133' },
  { key: 'sepia', name: '米黄', bg: '#f5ecd9', color: '#5b4636' },
  { key: 'green', name: '护眼绿', bg: '#cce8cf', color: '#1f3b2c' },
  { key: 'paper', name: '羊皮纸', bg: '#f3e5c8', color: '#4a3a2a' },
  { key: 'night', name: '夜间', bg: '#1e1e1e', color: '#a8a8a8' },
  { key: 'blue', name: '蓝色', bg: '#eaf3fb', color: '#1f3b5c' }
]

const fontOptions = [
  { key: 'default', label: '系统默认', value: '-apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif' },
  { key: 'songti', label: '宋体', value: '"SimSun", "Songti SC", serif' },
  { key: 'heiti', label: '黑体', value: '"SimHei", "Heiti SC", sans-serif' },
  { key: 'kaiti', label: '楷体', value: '"KaiTi", "Kaiti SC", serif' },
  { key: 'yahei', label: '微软雅黑', value: '"Microsoft YaHei", sans-serif' }
]

const defaults = {
  fontSize: 18,
  lineHeight: 2,
  paragraphSpacing: 0.8,
  letterSpacing: 0.2,
  fontFamily: 'default',
  width: 'medium',
  theme: 'white',
  customBg: '#ffffff'
}

const settings = reactive({ ...defaults, ...loadSettings() })

const theme = computed(() => {
  if (settings.theme === 'custom') {
    return { bg: settings.customBg, color: isDark(settings.customBg) ? '#d8d8d8' : '#303133' }
  }
  return themes.find((t) => t.key === settings.theme) || themes[0]
})

const widthValue = computed(() => {
  return settings.width === 'narrow' ? '640px' : settings.width === 'wide' ? '960px' : '780px'
})

const fontFamilyValue = computed(() => {
  return fontOptions.find((f) => f.key === settings.fontFamily)?.value || fontOptions[0].value
})

const contentStyle = computed(() => ({
  fontSize: settings.fontSize + 'px',
  lineHeight: settings.lineHeight,
  letterSpacing: settings.letterSpacing + 'px',
  fontFamily: fontFamilyValue.value,
  color: theme.value.color
}))

const paragraphs = computed(() => {
  const text = content.value?.content || ''
  return text.split(/\n+/).map((s) => s.trim()).filter(Boolean)
})

// 长章节分页：每页约 3000 字
const pageSize = 3000
const pages = computed(() => {
  const paras = paragraphs.value
  const result = []
  let cur = []
  let len = 0
  for (const p of paras) {
    cur.push(p)
    len += p.length + 1
    if (len >= pageSize) {
      result.push(cur)
      cur = []
      len = 0
    }
  }
  if (cur.length) result.push(cur)
  if (result.length === 0) result.push([])
  return result
})

const currentPage = ref(0)
const totalPages = computed(() => pages.value.length)
const currentParagraphs = computed(() => pages.value[currentPage.value] || [])

function goPage(p) {
  if (p < 0 || p >= totalPages.value) return
  stop()
  activePara.value = -1
  nowText.value = ''
  currentPage.value = p
  window.scrollTo(0, 0)
}

function loadSettings() {
  try {
    return JSON.parse(localStorage.getItem('reader-settings') || '{}')
  } catch (e) {
    return {}
  }
}

function save() {
  localStorage.setItem('reader-settings', JSON.stringify(settings))
}

function changeFont(delta) {
  settings.fontSize = Math.min(30, Math.max(12, settings.fontSize + delta))
  save()
}

function selectTheme(key) {
  settings.theme = key
  save()
}

function onCustomColor() {
  settings.theme = 'custom'
  save()
}

async function load(chapterId) {
  stop()
  activePara.value = -1
  nowText.value = ''
  const res = await getChapterContent(bookId, chapterId)
  content.value = res.data
  currentPage.value = 0
  reportProgress({ targetType: 1, targetId: bookId, chapterId: Number(chapterId) })
  window.scrollTo(0, 0)
}

function goChapter(id) {
  if (!id) return
  router.push(`/novel/${bookId}/${id}`)
}

function backToBook() {
  router.push(`/novel/${bookId}`)
}

function isDark(hex) {
  const c = (hex || '#ffffff').replace('#', '')
  const r = parseInt(c.substring(0, 2), 16) || 255
  const g = parseInt(c.substring(2, 4), 16) || 255
  const b = parseInt(c.substring(4, 6), 16) || 255
  return (0.299 * r + 0.587 * g + 0.114 * b) < 128
}

watch(() => route.params.chapterId, (id) => {
  if (id) load(id)
}, { immediate: true })

onBeforeUnmount(() => stop())
</script>

<style scoped>
.reader {
  min-height: calc(100vh - 100px);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}
.reader-top {
  position: sticky;
  top: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid #00000022;
  border-radius: 8px 8px 0 0;
}
.chapter-title {
  font-size: 15px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 55%;
}
.reader-body {
  margin: 0 auto;
  width: 100%;
  padding: 30px 24px 60px;
  box-sizing: border-box;
}
.chapter-content p {
  text-align: justify;
  word-break: break-word;
}
.chapter-content p.is-reading {
  background: rgba(64, 158, 255, 0.16);
  border-radius: 4px;
  transition: background 0.2s;
}
.top-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}
.chapter-nav {
  margin-top: 32px;
  display: flex;
  justify-content: center;
  gap: 24px;
}
.page-nav {
  margin-top: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}
.page-indicator {
  min-width: 72px;
  text-align: center;
  font-size: 14px;
  color: #909399;
}
.settings {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.setting-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.setting-row .label {
  width: 64px;
  flex-shrink: 0;
  color: #606266;
  font-size: 14px;
}
.setting-row .control {
  display: flex;
  align-items: center;
  gap: 8px;
}
.font-size {
  min-width: 28px;
  text-align: center;
  font-weight: 600;
}
.theme-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin: 4px 0 8px;
}
.theme-item {
  height: 52px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 2px solid transparent;
  font-size: 12px;
  color: #555;
}
.theme-item.active {
  border-color: #409eff;
  box-shadow: 0 0 0 2px #409eff33;
}
</style>
