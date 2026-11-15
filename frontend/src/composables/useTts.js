import { reactive, ref, watch } from 'vue'

/**
 * 浏览器 Web Speech API 朗读（TTS）单例。
 * - 播放状态全局共享，页面和播放条组件拿到的是同一份状态
 * - 长文本按段落/句子分块，逐块朗读，避免 utterance 过长被截断
 * - 通过 onChunk 回调上报当前朗读块，供页面做「当前句高亮」
 *
 * Phase 2（云端 TTS / 用户自带 Key）预留：上层只调用 speak(text, onChunk)，
 * 后续可把 speak 内部替换成调用后端 /api/tts，页面代码无需改动。
 */

const DEFAULT_SETTINGS = { rate: 1, volume: 1, voiceURI: '' }

function loadSettings() {
  try {
    return { ...DEFAULT_SETTINGS, ...JSON.parse(localStorage.getItem('tts-settings') || '{}') }
  } catch (e) {
    return { ...DEFAULT_SETTINGS }
  }
}

// 全局共享设置与音色列表
const settings = reactive(loadSettings())
const voices = ref([])

// 全局共享播放状态（单例播放器）
const isSpeaking = ref(false)
const isPaused = ref(false)
const currentIndex = ref(-1)
const currentChunk = ref(null)

let queue = []
let cursor = 0
let onChunkCb = null
let session = 0 // 每次 speak/stop 递增，作废旧 utterance 的异步回调

function refreshVoices() {
  if (!window.speechSynthesis) return
  const all = window.speechSynthesis.getVoices() || []
  const zh = all.filter((v) => v.lang && v.lang.toLowerCase().startsWith('zh'))
  voices.value = zh.length ? zh : all
}

if (typeof window !== 'undefined' && window.speechSynthesis) {
  refreshVoices()
  window.speechSynthesis.onvoiceschanged = refreshVoices
}

function saveSettings() {
  localStorage.setItem('tts-settings', JSON.stringify({
    rate: settings.rate, volume: settings.volume, voiceURI: settings.voiceURI
  }))
}
watch(settings, saveSettings)

/** 把超长段落按句号等标点切成小段，控制单次 utterance 长度。 */
function splitLong(p, limit = 180) {
  const res = []
  let buf = ''
  for (const ch of p) {
    buf += ch
    if (buf.length >= limit && '。！？；!?;'.includes(ch)) {
      res.push(buf.trim())
      buf = ''
    }
  }
  if (buf.trim()) res.push(buf.trim())
  return res.length ? res : [p]
}

function buildChunks(text) {
  const paras = String(text || '').split(/\n+/).map((s) => s.trim()).filter(Boolean)
  const chunks = []
  paras.forEach((p, pi) => {
    if (p.length <= 180) {
      chunks.push({ text: p, paraIndex: pi })
    } else {
      for (const sub of splitLong(p)) chunks.push({ text: sub, paraIndex: pi })
    }
  })
  return chunks
}

/** 把 HTML 正文转成带换行的纯文本，供朗读用。 */
export function htmlToPlainText(html) {
  if (!html) return ''
  const div = document.createElement('div')
  div.innerHTML = html
  const blockTags = ['p', 'div', 'br', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'li', 'blockquote', 'section']
  function walk(node) {
    let text = ''
    node.childNodes.forEach((child) => {
      if (child.nodeType === 3) {
        text += child.textContent
      } else if (child.nodeType === 1) {
        const tag = child.tagName.toLowerCase()
        if (blockTags.includes(tag)) {
          text += '\n' + walk(child) + '\n'
        } else {
          text += walk(child)
        }
      }
    })
    return text
  }
  return walk(div).replace(/\n{3,}/g, '\n\n').trim()
}

function getVoice() {
  if (!window.speechSynthesis) return null
  const all = window.speechSynthesis.getVoices()
  if (settings.voiceURI) return all.find((v) => v.voiceURI === settings.voiceURI) || null
  return all.find((v) => v.lang && v.lang.toLowerCase().startsWith('zh')) || null
}

function speakNext(mySession) {
  if (session !== mySession) return
  if (cursor >= queue.length) {
    isSpeaking.value = false
    isPaused.value = false
    currentIndex.value = -1
    currentChunk.value = null
    return
  }
  const chunk = queue[cursor]
  cursor++
  currentIndex.value = cursor - 1
  currentChunk.value = chunk
  if (onChunkCb) onChunkCb(chunk, cursor - 1)

  const u = new SpeechSynthesisUtterance(chunk.text)
  u.lang = 'zh-CN'
  u.rate = Number(settings.rate) || 1
  u.volume = settings.volume == null ? 1 : Number(settings.volume)
  const v = getVoice()
  if (v) u.voice = v
  u.onend = () => speakNext(mySession)
  u.onerror = (e) => {
    if (e.error === 'canceled' || e.error === 'interrupted') return
    speakNext(mySession) // 单个块出错则跳过继续
  }
  window.speechSynthesis.speak(u)
}

function speak(text, onChunk) {
  if (!window.speechSynthesis) return
  stop()
  const mySession = session
  queue = buildChunks(text)
  cursor = 0
  onChunkCb = onChunk || null
  if (!queue.length) return
  isSpeaking.value = true
  isPaused.value = false
  speakNext(mySession)
}

function pause() {
  if (!window.speechSynthesis || !isSpeaking.value) return
  window.speechSynthesis.pause()
  isPaused.value = true
}

function resume() {
  if (!window.speechSynthesis || !isSpeaking.value) return
  window.speechSynthesis.resume()
  isPaused.value = false
}

function stop() {
  session++
  if (window.speechSynthesis) window.speechSynthesis.cancel()
  isSpeaking.value = false
  isPaused.value = false
  currentIndex.value = -1
  currentChunk.value = null
  queue = []
  cursor = 0
}

export function useTts() {
  return {
    isSpeaking, isPaused, currentIndex, currentChunk,
    settings, voices,
    speak, pause, resume, stop,
    saveSettings, refreshVoices
  }
}
