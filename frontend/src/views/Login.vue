<template>
  <div class="login-page">
    <!-- 背景装饰元素 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
      <div class="circle circle-4"></div>
      <div class="particle particle-1"></div>
      <div class="particle particle-2"></div>
      <div class="particle particle-3"></div>
      <div class="particle particle-4"></div>
      <div class="particle particle-5"></div>
      <div class="gradient-blob blob-1"></div>
      <div class="gradient-blob blob-2"></div>
      <div class="gradient-blob blob-3"></div>
    </div>
    
    <div class="login-container">
      <div class="logo">
        <span class="logo-icon">▶</span>
        <h1>短视频</h1>
      </div>
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <input v-model="form.username" type="text" placeholder="用户名" required />
        </div>
        <div class="form-group">
          <input v-model="form.password" type="password" placeholder="密码" required />
        </div>
        <button type="submit" class="btn-primary" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api'

const router = useRouter()
const error = ref('')
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const handleSubmit = async () => {
  if (loading.value) return
  
  // 前端校验
  if (!form.username.trim()) {
    error.value = '请输入用户名'
    return
  }
  if (form.username.length < 2 || form.username.length > 20) {
    error.value = '用户名长度2-20个字符'
    return
  }
  if (!form.password) {
    error.value = '请输入密码'
    return
  }
  if (form.password.length < 6) {
    error.value = '密码至少6个字符'
    return
  }

  error.value = ''
  loading.value = true
  try {
    const res = await login(form.username.trim(), form.password)
    if (res.data.code === 200) {
      localStorage.setItem('user', JSON.stringify(res.data.data))
      router.push(res.data.data.isAdmin ? '/admin' : '/mobile')
    } else {
      error.value = res.data.message || '登录失败'
    }
  } catch (e) {
    error.value = e.response?.data?.message || '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f0f23 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

/* 浮动圆圈 */
.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.05);
  animation: float 20s infinite ease-in-out;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -150px;
  left: -150px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -100px;
  right: -100px;
  animation-delay: 5s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 20%;
  right: 10%;
  animation-delay: 10s;
}

.circle-4 {
  width: 100px;
  height: 100px;
  bottom: 30%;
  left: 15%;
  animation-delay: 15s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(30px, -30px) rotate(120deg);
  }
  66% {
    transform: translate(-20px, 20px) rotate(240deg);
  }
}

/* 粒子动画 */
.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(254,44,85,0.6);
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(254,44,85,0.5);
  animation: particleFloat 15s infinite ease-in-out;
}

.particle-1 {
  top: 20%;
  left: 20%;
  animation-delay: 0s;
}

.particle-2 {
  top: 60%;
  left: 80%;
  animation-delay: 3s;
}

.particle-3 {
  top: 80%;
  left: 30%;
  animation-delay: 6s;
}

.particle-4 {
  top: 40%;
  left: 70%;
  animation-delay: 9s;
}

.particle-5 {
  top: 10%;
  left: 50%;
  animation-delay: 12s;
}

@keyframes particleFloat {
  0%, 100% {
    transform: translate(0, 0) scale(1);
    opacity: 0.6;
  }
  25% {
    transform: translate(20px, -30px) scale(1.2);
    opacity: 1;
  }
  50% {
    transform: translate(-15px, -20px) scale(0.8);
    opacity: 0.4;
  }
  75% {
    transform: translate(25px, 15px) scale(1.1);
    opacity: 0.8;
  }
}

/* 渐变光晕 */
.gradient-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.3;
  animation: blobMove 25s infinite ease-in-out;
}

.blob-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(254,44,85,0.4), transparent);
  top: -200px;
  left: -200px;
  animation-delay: 0s;
}

.blob-2 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(37,244,238,0.3), transparent);
  bottom: -175px;
  right: -175px;
  animation-delay: 8s;
}

.blob-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255,107,107,0.3), transparent);
  top: 50%;
  left: 50%;
  margin-top: -150px;
  margin-left: -150px;
  animation-delay: 15s;
}

@keyframes blobMove {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(50px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-30px, 40px) scale(0.9);
  }
}
.login-container {
  width: 100%;
  max-width: 380px;
  background: rgba(255,255,255,0.05);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 48px 32px;
  border: 1px solid rgba(255,255,255,0.1);
  position: relative;
  z-index: 1;
  box-shadow: 0 8px 32px rgba(0,0,0,0.3);
}
.logo {
  text-align: center;
  margin-bottom: 40px;
}
.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, var(--primary), #ff6b6b);
  border-radius: 16px;
  font-size: 28px;
  margin-bottom: 16px;
}
.logo h1 {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(90deg, var(--primary), var(--secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.form-group {
  margin-bottom: 20px;
}
.form-group input {
  width: 100%;
  padding: 16px 20px;
  background: rgba(255,255,255,0.08);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 12px;
  color: #fff;
  font-size: 16px;
  transition: all 0.3s;
}
.form-group input:focus {
  outline: none;
  border-color: var(--primary);
  background: rgba(255,255,255,0.12);
}
.form-group input::placeholder {
  color: rgba(255,255,255,0.4);
}
.btn-primary {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, var(--primary), #ff6b6b);
  border-radius: 12px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  transition: transform 0.2s, box-shadow 0.2s;
}
.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(254,44,85,0.4);
}
.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}
.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-right: 8px;
  vertical-align: middle;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.toggle-text {
  text-align: center;
  margin-top: 24px;
  color: rgba(255,255,255,0.6);
}
.toggle-text span {
  color: var(--secondary);
  cursor: pointer;
  font-weight: 500;
}
.error {
  text-align: center;
  color: var(--primary);
  margin-top: 16px;
}
</style>
