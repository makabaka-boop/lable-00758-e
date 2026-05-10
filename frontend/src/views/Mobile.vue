<template>
  <div class="mobile-app">
    <div 
      class="video-container" 
      ref="containerRef" 
      @touchstart.passive="onTouchStart" 
      @touchmove.passive="onTouchMove"
      @touchend="onTouchEnd"
      @wheel="onWheel"
      @mousedown="onMouseDown"
      @mousemove="onMouseMove"
      @mouseup="onMouseUp"
      @mouseleave="onMouseUp"
    >
      <div 
        class="video-wrapper" 
        :style="wrapperStyle"
        :class="{ 'no-transition': isDragging }"
      >
        <div class="video-item" v-for="(video, index) in videos" :key="video.id">
          <video
            v-if="Math.abs(index - currentIndex) <= 1"
            :ref="el => videoRefs[index] = el"
            :src="video.videoUrl"
            :preload="Math.abs(index - currentIndex) <= 1 ? 'auto' : 'none'"
            loop
            playsinline
            webkit-playsinline
            x5-playsinline
            @click="togglePlay(index)"
          />
          <div class="video-info">
            <h3>@{{ video.username || '用户' }}</h3>
            <p>{{ video.title }}</p>
            <p class="desc">{{ video.description }}</p>
          </div>
          <div class="video-actions">
            <div class="action-btn" @click.stop="handleLike(video)">
              <span :class="['icon', { active: video.liked }]">❤</span>
              <span>{{ video.likeCount }}</span>
            </div>
            <div class="action-btn" @click.stop="handleFavorite(video)">
              <span :class="['icon', { active: video.favorited }]">★</span>
              <span>{{ video.favoriteCount }}</span>
            </div>
            <div class="action-btn" @click.stop="showComments(video)">
              <span class="icon">💬</span>
              <span>{{ video.commentCount }}</span>
            </div>
          </div>
          <div class="play-btn" v-if="!playing && index === currentIndex">▶</div>
        </div>
      </div>
    </div>

    <div class="header">
      <span v-if="user" @click="logout">退出</span>
      <span v-else @click="$router.push('/login')">登录</span>
    </div>

    <!-- 提示消息 -->
    <div class="toast-modal" v-if="toastMessage" @click.self="cancelToast">
      <div class="toast-content">
        <p class="toast-text">{{ toastMessage }}</p>
        <div class="toast-buttons">
          <button class="toast-btn cancel" @click="cancelToast">取消</button>
          <button class="toast-btn confirm" @click="confirmToast">前往登录</button>
        </div>
      </div>
    </div>

    <!-- 评论弹窗 -->
    <div class="comments-modal" v-if="showCommentsModal" @click.self="showCommentsModal = false">
      <div class="comments-content">
        <div class="comments-header">
          <span>{{ comments.length }} 条评论</span>
          <span class="close" @click="showCommentsModal = false">✕</span>
        </div>
        <div class="comments-list">
          <div class="comment-item" v-for="c in comments" :key="c.id">
            <div class="comment-avatar">{{ (c.username || '用户')[0] }}</div>
            <div class="comment-body">
              <div class="comment-name">{{ c.username || '用户' }}</div>
              <div class="comment-text">{{ c.content }}</div>
            </div>
          </div>
          <div class="no-comments" v-if="comments.length === 0">暂无评论</div>
        </div>
        <div class="comment-input" v-if="user">
          <input v-model="commentText" placeholder="写评论..." @keyup.enter="submitComment" />
          <button @click="submitComment">发送</button>
        </div>
        <div class="comment-input login-tip" v-else>
          <span @click="$router.push('/login')">登录后发表评论</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
 import { ref, onMounted, watch, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getVideos, likeVideo, favoriteVideo, addComment, getComments, viewVideo } from '../api'

const router = useRouter()

const videos = ref([])
const currentIndex = ref(0)
const videoRefs = ref([])
const playing = ref(false)
const containerRef = ref(null)
const user = computed(() => {
  const u = localStorage.getItem('user')
  return u ? JSON.parse(u) : null
})

const showCommentsModal = ref(false)
const comments = ref([])
const commentText = ref('')
const currentVideo = ref(null)
const toastMessage = ref('')
const pendingAction = ref(null)

let touchStartY = 0
let touchStartTime = 0
let page = 0
const loading = ref(false)

// 滑动相关状态
const isDragging = ref(false)
const dragOffset = ref(0)
let mouseStartY = 0
let wheelTimeout = null

const wrapperStyle = computed(() => {
  const baseOffset = -currentIndex.value * 100
  return {
    transform: `translateY(calc(${baseOffset}vh + ${dragOffset.value}px))`,
    willChange: isDragging.value ? 'transform' : 'auto'
  }
})

const loadVideos = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const res = await getVideos(page, 10, user.value?.id)
    if (res.data.code === 200) {
      videos.value = [...videos.value, ...res.data.data.content]
      page++
    }
  } finally {
    loading.value = false
  }
}

const pauseAllVideos = () => {
  const root = containerRef.value || document
  const nodes = root.querySelectorAll ? root.querySelectorAll('video') : []
  nodes.forEach((v) => {
    try {
      v.pause()
      v.currentTime = 0
    } catch {}
  })
}

// 防止 playVideo 并发：只允许“最后一次切换”触发播放
let playSeq = 0

const playVideo = async (index) => {
  const seq = ++playSeq
  // 先强制暂停页面上所有 video，防止“上一个还在播放”
  pauseAllVideos()
  playing.value = false

  // 等待 DOM / ref 更新后再播放当前
  await nextTick()
  // 如果期间又切换了 index，本次播放作废
  if (seq !== playSeq) return

  const v = videoRefs.value[index]
  if (v) {
    // 再保险：播放前再暂停一次，避免其它来源触发 play
    pauseAllVideos()
    v.play().catch(() => {})
    playing.value = true
    if (videos.value[index]?.id) viewVideo(videos.value[index].id)
  } else {
    playing.value = false
  }
}

const togglePlay = (index) => {
  const v = videoRefs.value[index]
  if (v) {
    if (v.paused) {
      // 点击播放时也确保只有一个在播，并作废任何 pending 的自动播放
      playSeq++
      pauseAllVideos()
      v.play()
      playing.value = true
    } else {
      v.pause()
      playing.value = false
    }
  }
}

const onTouchStart = (e) => {
  touchStartY = e.touches[0].clientY
  touchStartTime = Date.now()
  isDragging.value = true
  dragOffset.value = 0
}

const onTouchMove = (e) => {
  if (!isDragging.value) return
  const currentY = e.touches[0].clientY
  let diff = currentY - touchStartY
  
  // 边界阻尼效果
  if ((currentIndex.value === 0 && diff > 0) || 
      (currentIndex.value === videos.value.length - 1 && diff < 0)) {
    diff = diff * 0.3
  }
  
  dragOffset.value = diff
}

const onTouchEnd = (e) => {
  isDragging.value = false
  const diff = touchStartY - e.changedTouches[0].clientY
  const duration = Date.now() - touchStartTime
  const velocity = Math.abs(diff) / duration
  
  // 快速滑动或滑动距离超过阈值
  const threshold = velocity > 0.3 ? 50 : 120
  
  if (Math.abs(diff) > threshold) {
    if (diff > 0 && currentIndex.value < videos.value.length - 1) {
      currentIndex.value++
      if (currentIndex.value >= videos.value.length - 2) loadVideos()
    } else if (diff < 0 && currentIndex.value > 0) {
      currentIndex.value--
    }
  }
  
  dragOffset.value = 0
}

// PC端鼠标滚轮支持
const onWheel = (e) => {
  e.preventDefault()
  if (wheelTimeout) return
  
  wheelTimeout = setTimeout(() => {
    wheelTimeout = null
  }, 500)
  
  const delta = e.deltaY > 0 ? 1 : -1
  if (delta > 0 && currentIndex.value < videos.value.length - 1) {
    currentIndex.value++
    if (currentIndex.value >= videos.value.length - 2) loadVideos()
  } else if (delta < 0 && currentIndex.value > 0) {
    currentIndex.value--
  }
}

// PC端鼠标拖拽支持
const onMouseDown = (e) => {
  isDragging.value = true
  mouseStartY = e.clientY
  dragOffset.value = 0
}

const onMouseMove = (e) => {
  if (!isDragging.value) return
  const diff = e.clientY - mouseStartY
  
  // 边界阻尼效果
  if ((currentIndex.value === 0 && diff > 0) || 
      (currentIndex.value === videos.value.length - 1 && diff < 0)) {
    dragOffset.value = diff * 0.3
  } else {
    dragOffset.value = diff
  }
}

const onMouseUp = () => {
  if (!isDragging.value) return
  isDragging.value = false
  
  const threshold = 50
  if (Math.abs(dragOffset.value) > threshold) {
    if (dragOffset.value < 0 && currentIndex.value < videos.value.length - 1) {
      currentIndex.value++
      if (currentIndex.value >= videos.value.length - 2) loadVideos()
    } else if (dragOffset.value > 0 && currentIndex.value > 0) {
      currentIndex.value--
    }
  }
  
  dragOffset.value = 0
}

watch(currentIndex, (index) => {
  playVideo(index)
})

const showToast = (message) => {
  toastMessage.value = message
}

const cancelToast = () => {
  toastMessage.value = ''
  pendingAction.value = null
}

const confirmToast = () => {
  toastMessage.value = ''
  if (pendingAction.value) {
    router.push('/login')
  }
  pendingAction.value = null
}

const handleLike = async (video) => {
  if (!user.value) {
    pendingAction.value = 'like'
    showToast('请先登录才能点赞')
    return
  }
  const res = await likeVideo(video.id, user.value.id)
  if (res.data.code === 200) {
    video.liked = res.data.data
    video.likeCount += video.liked ? 1 : -1
  }
}

const handleFavorite = async (video) => {
  if (!user.value) {
    pendingAction.value = 'favorite'
    showToast('请先登录才能收藏')
    return
  }
  const res = await favoriteVideo(video.id, user.value.id)
  if (res.data.code === 200) {
    video.favorited = res.data.data
    video.favoriteCount += video.favorited ? 1 : -1
  }
}

const showComments = async (video) => {
  currentVideo.value = video
  showCommentsModal.value = true
  const res = await getComments(video.id, 0, 50)
  if (res.data.code === 200) {
    comments.value = res.data.data.content
  }
}

const submitComment = async () => {
  if (!commentText.value.trim() || !user.value) return
  const res = await addComment(currentVideo.value.id, user.value.id, commentText.value)
  if (res.data.code === 200) {
    comments.value.unshift({ ...res.data.data, username: user.value.nickname })
    currentVideo.value.commentCount++
    commentText.value = ''
  }
}

const logout = () => {
  localStorage.removeItem('user')
  window.location.reload()
}

onMounted(() => {
  loadVideos().then(() => {
    setTimeout(() => playVideo(0), 300)
  })
})
</script>

<style scoped>
.mobile-app {
  width: 100vw;
  height: 100vh;
  background: #000;
  overflow: hidden;
  position: relative;
}
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  padding: 16px 20px;
  display: flex;
  justify-content: flex-end;
  z-index: 100;
  background: linear-gradient(to bottom, rgba(0,0,0,0.5), transparent);
}
.header span {
  padding: 8px 16px;
  background: rgba(255,255,255,0.15);
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}
.video-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  cursor: grab;
}
.video-container:active {
  cursor: grabbing;
}
.video-wrapper {
  display: flex;
  flex-direction: column;
  transition: transform 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.video-wrapper.no-transition {
  transition: none;
}
.video-item {
  width: 100vw;
  height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #000;
}
.video-item video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.play-btn {
  position: absolute;
  width: 80px;
  height: 80px;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  pointer-events: none;
}
.video-info {
  position: absolute;
  bottom: 40px;
  left: 16px;
  right: 80px;
}
.video-info h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}
.video-info p {
  font-size: 15px;
  line-height: 1.4;
}
.video-info .desc {
  color: rgba(255,255,255,0.7);
  font-size: 14px;
  margin-top: 4px;
}
.video-actions {
  position: absolute;
  right: 12px;
  bottom: 120px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}
.action-btn .icon {
  width: 48px;
  height: 48px;
  background: rgba(255,255,255,0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  transition: all 0.2s;
}
.action-btn .icon.active {
  color: var(--primary);
  background: rgba(254,44,85,0.2);
}
.action-btn span:last-child {
  font-size: 12px;
}
/* 评论弹窗 */
.comments-modal {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  z-index: 200;
  display: flex;
  align-items: flex-end;
}
.comments-content {
  width: 100%;
  max-height: 70vh;
  background: var(--bg-card);
  border-radius: 16px 16px 0 0;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease-out;
}
@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
.comments-header {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}
.comments-header .close {
  cursor: pointer;
  padding: 4px 8px;
}
.comments-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}
/* 评论列表滚动条样式 */
.comments-list::-webkit-scrollbar {
  width: 6px;
}
.comments-list::-webkit-scrollbar-track {
  background: rgba(255,255,255,0.05);
  border-radius: 10px;
}
.comments-list::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.2);
  border-radius: 10px;
  transition: background 0.2s;
}
.comments-list::-webkit-scrollbar-thumb:hover {
  background: rgba(255,255,255,0.3);
}
.comments-list {
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.2) rgba(255,255,255,0.05);
}
.comment-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.comment-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--primary), #ff6b6b);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}
.comment-body {
  flex: 1;
}
.comment-name {
  font-size: 13px;
  color: rgba(255,255,255,0.6);
  margin-bottom: 4px;
}
.comment-text {
  font-size: 14px;
  line-height: 1.5;
}
.no-comments {
  text-align: center;
  color: rgba(255,255,255,0.4);
  padding: 40px 0;
}
.comment-input {
  padding: 12px 16px;
  border-top: 1px solid rgba(255,255,255,0.1);
  display: flex;
  gap: 12px;
  background: var(--bg-dark);
}
.comment-input input {
  flex: 1;
  padding: 12px 16px;
  background: rgba(255,255,255,0.1);
  border: none;
  border-radius: 24px;
  color: #fff;
  font-size: 14px;
}
.comment-input input:focus {
  outline: none;
}
.comment-input button {
  padding: 12px 20px;
  background: var(--primary);
  border-radius: 24px;
  color: #fff;
  font-weight: 500;
}
.comment-input.login-tip {
  justify-content: center;
  color: var(--secondary);
  cursor: pointer;
}
/* 提示消息 */
.toast-modal {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 300;
  animation: toastFadeIn 0.2s ease-out;
}
@keyframes toastFadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
.toast-content {
  background: linear-gradient(180deg, #1a1a2e 0%, #0f0f23 100%);
  border-radius: 16px;
  padding: 32px;
  min-width: 280px;
  max-width: 90%;
  border: 1px solid rgba(255,255,255,0.1);
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
  animation: toastSlideUp 0.3s ease-out;
}
@keyframes toastSlideUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
.toast-text {
  font-size: 16px;
  color: #fff;
  text-align: center;
  margin-bottom: 24px;
  line-height: 1.5;
}
.toast-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: nowrap;
  align-items: center;
}
.toast-btn {
  flex: 1;
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s;
  border: none;
  cursor: pointer;
  white-space: nowrap;
  min-width: 0;
}
.toast-btn.cancel {
  background: rgba(255,255,255,0.1);
  color: rgba(255,255,255,0.8);
}
.toast-btn.cancel:hover {
  background: rgba(255,255,255,0.2);
}
.toast-btn.confirm {
  background: linear-gradient(135deg, var(--primary), #ff6b6b);
  color: #fff;
}
.toast-btn.confirm:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(254,44,85,0.4);
}
</style>
