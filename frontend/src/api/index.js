import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器 - 添加token
api.interceptors.request.use(config => {
  const user = localStorage.getItem('user')
  if (user) {
    const { token } = JSON.parse(user)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
  }
  return config
})

// 响应拦截器 - 处理401
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const login = (username, password) => api.post('/user/login', { username, password })
export const register = (username, password, nickname) => api.post('/user/register', { username, password, nickname })

export const getVideos = (page, size, userId) => api.get('/video/list', { params: { page, size, userId } })
export const uploadVideo = (formData) => api.post('/video/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
export const likeVideo = (videoId, userId) => api.post('/video/like', { videoId, userId })
export const favoriteVideo = (videoId, userId) => api.post('/video/favorite', { videoId, userId })
export const addComment = (videoId, userId, content) => api.post('/video/comment', { videoId, userId, content })
export const getComments = (videoId, page, size) => api.get(`/video/comments/${videoId}`, { params: { page, size } })
export const viewVideo = (videoId) => api.post(`/video/view/${videoId}`)
export const getAdminVideos = () => api.get('/video/admin/list')
export const deleteVideo = (id) => api.delete(`/video/admin/${id}`)

export default api
