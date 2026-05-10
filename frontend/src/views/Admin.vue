<template>
  <div class="admin-page">
    <header class="admin-header">
      <h1>🎬 视频管理后台</h1>
      <div class="user-info">
        <span>{{ user?.nickname }}</span>
        <button @click="logout">退出</button>
      </div>
    </header>

    <main class="admin-main">
      <section class="list-section">
        <div class="section-header">
          <h2>视频列表</h2>
          <button class="btn-upload-trigger" @click="showUploadModal = true">
            上传视频
          </button>
        </div>
        <div class="video-list">
          <div class="video-card" v-for="video in videos" :key="video.id">
            <video :src="video.videoUrl" controls></video>
            <div class="video-meta">
              <h3>{{ video.title }}</h3>
              <p>{{ video.description || '无描述' }}</p>
              <div class="stats">
                <span>❤ {{ video.likeCount }}</span>
                <span>★ {{ video.favoriteCount }}</span>
                <span>💬 {{ video.commentCount }}</span>
                <span>👁 {{ video.viewCount }}</span>
              </div>
            </div>
            <div class="video-actions">
              <button class="btn-comments" @click="showComments(video)">查看评论</button>
              <button class="btn-delete" @click="handleDelete(video.id)">删除</button>
            </div>
          </div>
          <p class="empty" v-if="videos.length === 0">暂无视频</p>
        </div>
      </section>
    </main>

    <!-- 评论弹窗 -->
    <div class="comments-modal" v-if="showCommentsModal" @click.self="closeCommentsModal">
      <div class="comments-modal-content">
        <div class="modal-header">
          <h3>评论列表 ({{ comments.length }})</h3>
          <span class="close-btn" @click="closeCommentsModal">✕</span>
        </div>
        <div class="comments-list">
          <div class="comment-item" v-for="comment in comments" :key="comment.id">
            <div class="comment-avatar">{{ (comment.username || '用户')[0] }}</div>
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-name">{{ comment.username || '用户' }}</span>
                <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
              </div>
              <div class="comment-text">{{ comment.content }}</div>
            </div>
          </div>
          <div class="no-comments" v-if="comments.length === 0">暂无评论</div>
        </div>
      </div>
    </div>

    <!-- 上传视频弹窗 -->
    <div class="upload-modal" v-if="showUploadModal" @click.self="closeUploadModal">
      <div class="upload-modal-content">
        <div class="modal-header">
          <h3>上传视频</h3>
          <span class="close-btn" @click="closeUploadModal">✕</span>
        </div>
        <div class="upload-form">
          <div class="form-row">
            <label>视频文件</label>
            <div class="file-upload-area" @click="fileInput?.click()" @dragover.prevent @drop.prevent="handleDrop">
              <input 
                type="file" 
                @change="onFileChange" 
                accept="video/*" 
                ref="fileInput" 
                class="file-input-hidden"
              />
              <div class="file-upload-content" v-if="!file">
                <div class="upload-icon">📹</div>
                <p class="upload-text">点击选择视频文件</p>
                <p class="upload-hint">或拖拽文件到此处</p>
              </div>
              <div class="file-selected" v-else>
                <div class="file-icon">🎬</div>
                <div class="file-info">
                  <p class="file-name">{{ file.name }}</p>
                  <p class="file-size">{{ formatFileSize(file.size) }}</p>
                </div>
                <button class="file-remove" @click.stop="removeFile">✕</button>
              </div>
            </div>
          </div>
          <div class="form-row">
            <label>标题</label>
            <input v-model="form.title" type="text" placeholder="输入视频标题" />
          </div>
          <div class="form-row">
            <label>描述</label>
            <textarea v-model="form.description" placeholder="输入视频描述(可选)"></textarea>
          </div>
          <p v-if="uploadMsg" :class="['msg', uploadMsg.type]">{{ uploadMsg.text }}</p>
          <div class="modal-footer">
            <button class="btn-cancel" @click="closeUploadModal">取消</button>
            <button class="btn-upload" @click="handleUpload" :disabled="uploading">
              {{ uploading ? '上传中...' : '确认上传' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { uploadVideo, getAdminVideos, deleteVideo, getComments } from '../api'

const router = useRouter()
const user = computed(() => {
  const u = localStorage.getItem('user')
  return u ? JSON.parse(u) : null
})

const videos = ref([])
const form = ref({ title: '', description: '' })
const file = ref(null)
const fileInput = ref(null)
const uploading = ref(false)
const uploadMsg = ref(null)
const showUploadModal = ref(false)
const showCommentsModal = ref(false)
const comments = ref([])
const currentVideo = ref(null)

onMounted(() => {
  if (!user.value?.isAdmin) {
    router.push('/login')
    return
  }
  loadVideos()
})

const loadVideos = async () => {
  const res = await getAdminVideos()
  if (res.data.code === 200) {
    videos.value = res.data.data
  }
}

const onFileChange = (e) => {
  const selectedFile = e.target.files[0]
  if (selectedFile) {
    if (selectedFile.type.startsWith('video/')) {
      file.value = selectedFile
    } else {
      uploadMsg.value = { type: 'error', text: '请选择视频文件' }
    }
  }
}

const handleDrop = (e) => {
  const droppedFile = e.dataTransfer.files[0]
  if (droppedFile && droppedFile.type.startsWith('video/')) {
    file.value = droppedFile
    if (fileInput.value) {
      const dataTransfer = new DataTransfer()
      dataTransfer.items.add(droppedFile)
      fileInput.value.files = dataTransfer.files
    }
  } else {
    uploadMsg.value = { type: 'error', text: '请拖拽视频文件' }
  }
}

const removeFile = () => {
  file.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const closeUploadModal = () => {
  showUploadModal.value = false
  form.value = { title: '', description: '' }
  file.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  uploadMsg.value = null
}

const handleUpload = async () => {
  if (!file.value || !form.value.title) {
    uploadMsg.value = { type: 'error', text: '请选择文件并输入标题' }
    return
  }
  uploading.value = true
  uploadMsg.value = null
  try {
    const formData = new FormData()
    formData.append('file', file.value)
    formData.append('title', form.value.title)
    formData.append('description', form.value.description)
    formData.append('userId', user.value.id)
    const res = await uploadVideo(formData)
    if (res.data.code === 200) {
      uploadMsg.value = { type: 'success', text: '上传成功！' }
      setTimeout(() => {
        closeUploadModal()
        loadVideos()
      }, 1000)
    } else {
      uploadMsg.value = { type: 'error', text: res.data.message }
    }
  } catch (e) {
    uploadMsg.value = { type: 'error', text: '上传失败' }
  } finally {
    uploading.value = false
  }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除此视频？')) return
  await deleteVideo(id)
  loadVideos()
}

const showComments = async (video) => {
  currentVideo.value = video
  showCommentsModal.value = true
  const res = await getComments(video.id, 0, 100)
  if (res.data.code === 200) {
    comments.value = res.data.data.content || []
  }
}

const closeCommentsModal = () => {
  showCommentsModal.value = false
  comments.value = []
  currentVideo.value = null
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

const logout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.admin-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #0f0f23 0%, #1a1a2e 100%);
}
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  background: rgba(0,0,0,0.3);
  border-bottom: 1px solid rgba(255,255,255,0.1);
  flex-shrink: 0;
}
.admin-header h1 {
  font-size: 24px;
  font-weight: 600;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-info button {
  padding: 8px 20px;
  background: rgba(255,255,255,0.1);
  border-radius: 8px;
  color: #fff;
  transition: background 0.2s;
}
.user-info button:hover {
  background: rgba(255,255,255,0.2);
}
.admin-main {
  flex: 1;
  overflow-y: auto;
  padding: 40px;
}
/* 主区域滚动条样式 */
.admin-main::-webkit-scrollbar {
  width: 10px;
}
.admin-main::-webkit-scrollbar-track {
  background: rgba(255,255,255,0.03);
  border-radius: 10px;
}
.admin-main::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.15);
  border-radius: 10px;
  transition: background 0.2s;
}
.admin-main::-webkit-scrollbar-thumb:hover {
  background: rgba(255,255,255,0.25);
}
.admin-main {
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.15) rgba(255,255,255,0.03);
}
.admin-main > * {
  max-width: 1200px;
  margin: 0 auto;
}
section {
  background: rgba(255,255,255,0.05);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 32px;
  border: 1px solid rgba(255,255,255,0.1);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
section h2 {
  font-size: 20px;
  margin: 0;
}
.btn-upload-trigger {
  padding: 10px 24px;
  background: linear-gradient(135deg, var(--primary), #ff6b6b);
  border-radius: 10px;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  transition: transform 0.2s, box-shadow 0.2s;
}
.btn-upload-trigger:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(254,44,85,0.4);
}
.upload-form {
  max-width: 100%;
}
.form-row {
  margin-bottom: 20px;
}
.form-row label {
  display: block;
  margin-bottom: 8px;
  color: rgba(255,255,255,0.7);
  font-size: 14px;
}
.form-row input[type="text"],
.form-row textarea {
  width: 100%;
  padding: 14px 16px;
  background: rgba(255,255,255,0.08);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 10px;
  color: #fff;
  font-size: 15px;
}
.form-row input:focus,
.form-row textarea:focus {
  outline: none;
  border-color: var(--primary);
}
.form-row textarea {
  min-height: 100px;
  resize: vertical;
}
/* 文件上传区域 */
.file-input-hidden {
  display: none;
}
.file-upload-area {
  width: 100%;
  min-height: 160px;
  border: 2px dashed rgba(255,255,255,0.2);
  border-radius: 12px;
  padding: 24px;
  background: rgba(255,255,255,0.03);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.file-upload-area:hover {
  border-color: var(--primary);
  background: rgba(254,44,85,0.05);
}
.file-upload-content {
  text-align: center;
}
.upload-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.upload-text {
  font-size: 16px;
  color: rgba(255,255,255,0.9);
  margin-bottom: 8px;
  font-weight: 500;
}
.upload-hint {
  font-size: 13px;
  color: rgba(255,255,255,0.5);
}
.file-selected {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: rgba(255,255,255,0.05);
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.1);
}
.file-icon {
  font-size: 32px;
  flex-shrink: 0;
}
.file-info {
  flex: 1;
  min-width: 0;
}
.file-name {
  font-size: 15px;
  color: rgba(255,255,255,0.9);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
}
.file-size {
  font-size: 13px;
  color: rgba(255,255,255,0.5);
}
.file-remove {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(254,44,85,0.2);
  color: var(--primary);
  border-radius: 50%;
  border: none;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.2s;
  flex-shrink: 0;
}
.file-remove:hover {
  background: rgba(254,44,85,0.3);
  transform: scale(1.1);
}
.btn-upload {
  padding: 14px 32px;
  background: linear-gradient(135deg, var(--primary), #ff6b6b);
  border-radius: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  transition: transform 0.2s, opacity 0.2s;
}
.btn-upload:hover:not(:disabled) {
  transform: translateY(-2px);
}
.btn-upload:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.msg {
  margin-top: 16px;
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
}
.msg.success {
  background: rgba(37,244,238,0.1);
  color: var(--secondary);
}
.msg.error {
  background: rgba(254,44,85,0.1);
  color: var(--primary);
}
.video-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}
.video-card {
  background: rgba(0,0,0,0.3);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(255,255,255,0.05);
}
.video-card video {
  width: 100%;
  height: 200px;
  object-fit: cover;
  background: #000;
}
.video-meta {
  padding: 16px;
}
.video-meta h3 {
  font-size: 16px;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.video-meta p {
  font-size: 13px;
  color: rgba(255,255,255,0.5);
  margin-bottom: 12px;
}
.stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: rgba(255,255,255,0.6);
}
.video-actions {
  display: flex;
  gap: 8px;
  padding: 0 16px 16px;
}
.btn-comments {
  flex: 1;
  padding: 12px;
  background: rgba(37,244,238,0.1);
  color: var(--secondary);
  font-size: 14px;
  border-radius: 8px;
  transition: background 0.2s;
  border: none;
  cursor: pointer;
}
.btn-comments:hover {
  background: rgba(37,244,238,0.2);
}
.btn-delete {
  flex: 1;
  padding: 12px;
  background: rgba(254,44,85,0.1);
  color: var(--primary);
  font-size: 14px;
  border-radius: 8px;
  transition: background 0.2s;
  border: none;
  cursor: pointer;
}
.btn-delete:hover {
  background: rgba(254,44,85,0.2);
}
.empty {
  text-align: center;
  color: rgba(255,255,255,0.4);
  padding: 40px;
  grid-column: 1 / -1;
}
/* 上传弹窗样式 */
.upload-modal {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
.upload-modal-content {
  background: linear-gradient(180deg, #1a1a2e 0%, #0f0f23 100%);
  border-radius: 20px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  border: 1px solid rgba(255,255,255,0.1);
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
  animation: slideUp 0.3s ease-out;
}
/* 自定义滚动条样式 */
.upload-modal-content::-webkit-scrollbar,
.comments-list::-webkit-scrollbar {
  width: 8px;
}
.upload-modal-content::-webkit-scrollbar-track,
.comments-list::-webkit-scrollbar-track {
  background: rgba(255,255,255,0.05);
  border-radius: 10px;
}
.upload-modal-content::-webkit-scrollbar-thumb,
.comments-list::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.2);
  border-radius: 10px;
  transition: background 0.2s;
}
.upload-modal-content::-webkit-scrollbar-thumb:hover,
.comments-list::-webkit-scrollbar-thumb:hover {
  background: rgba(255,255,255,0.3);
}
/* Firefox 滚动条样式 */
.upload-modal-content,
.comments-list {
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.2) rgba(255,255,255,0.05);
}
@keyframes slideUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.modal-header h3 {
  font-size: 22px;
  font-weight: 600;
  margin: 0;
}
.close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.1);
  border-radius: 50%;
  cursor: pointer;
  font-size: 20px;
  transition: background 0.2s;
}
.close-btn:hover {
  background: rgba(255,255,255,0.2);
}
.upload-modal-content .upload-form {
  padding: 32px;
}
.modal-footer {
  display: flex;
  gap: 16px;
  margin-top: 24px;
  justify-content: flex-end;
}
.btn-cancel {
  padding: 12px 28px;
  background: rgba(255,255,255,0.1);
  border-radius: 10px;
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  transition: background 0.2s;
}
.btn-cancel:hover {
  background: rgba(255,255,255,0.2);
}
.upload-modal-content .btn-upload {
  padding: 12px 28px;
  background: linear-gradient(135deg, var(--primary), #ff6b6b);
  border-radius: 10px;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  transition: transform 0.2s, opacity 0.2s;
}
.upload-modal-content .btn-upload:hover:not(:disabled) {
  transform: translateY(-2px);
}
.upload-modal-content .btn-upload:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
/* 评论弹窗样式 */
.comments-modal {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease-out;
}
.comments-modal-content {
  background: linear-gradient(180deg, #1a1a2e 0%, #0f0f23 100%);
  border-radius: 20px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(255,255,255,0.1);
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
  animation: slideUp 0.3s ease-out;
}
.comments-modal-content .modal-header {
  padding: 24px 32px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  flex-shrink: 0;
}
.comments-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px 32px;
}
.comment-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}
.comment-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}
.comment-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--primary), #ff6b6b);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}
.comment-body {
  flex: 1;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.comment-name {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255,255,255,0.9);
}
.comment-time {
  font-size: 12px;
  color: rgba(255,255,255,0.4);
}
.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: rgba(255,255,255,0.8);
  word-break: break-word;
}
.no-comments {
  text-align: center;
  color: rgba(255,255,255,0.4);
  padding: 60px 0;
  font-size: 15px;
}
</style>
