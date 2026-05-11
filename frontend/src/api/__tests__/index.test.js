import { describe, it, expect, vi, beforeEach } from 'vitest'
import api, {
  login,
  register,
  getVideos,
  uploadVideo,
  likeVideo,
  favoriteVideo,
  addComment,
  getComments,
  viewVideo,
  getAdminVideos,
  deleteVideo
} from '../index'

vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      defaults: { baseURL: '/api', timeout: 30000 },
      interceptors: {
        request: { use: vi.fn(), handlers: [] },
        response: { use: vi.fn(), handlers: [] }
      },
      get: vi.fn(),
      post: vi.fn(),
      delete: vi.fn()
    }))
  }
}))

describe('API 封装测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('应该正确配置 axios 实例', () => {
    expect(api.defaults.baseURL).toBe('/api')
    expect(api.defaults.timeout).toBe(30000)
  })

  it('login 应该发送正确的POST请求', async () => {
    const mockResponse = { data: { code: 200, data: { token: 'token' } } }
    api.post = vi.fn().mockResolvedValue(mockResponse)

    const result = await login('admin', 'password123')

    expect(api.post).toHaveBeenCalledWith('/user/login', { username: 'admin', password: 'password123' })
    expect(result).toEqual(mockResponse)
  })

  it('register 应该发送正确的POST请求', async () => {
    const mockResponse = { data: { code: 200 } }
    api.post = vi.fn().mockResolvedValue(mockResponse)

    const result = await register('testuser', 'password123', 'Test User')

    expect(api.post).toHaveBeenCalledWith('/user/register', {
      username: 'testuser',
      password: 'password123',
      nickname: 'Test User'
    })
    expect(result).toEqual(mockResponse)
  })

  it('getVideos 应该发送正确的GET请求', async () => {
    const mockResponse = { data: { code: 200, data: { content: [] } } }
    api.get = vi.fn().mockResolvedValue(mockResponse)

    const result = await getVideos(0, 10, 1)

    expect(api.get).toHaveBeenCalledWith('/video/list', { params: { page: 0, size: 10, userId: 1 } })
    expect(result).toEqual(mockResponse)
  })

  it('uploadVideo 应该发送正确的POST请求', async () => {
    const mockFormData = new FormData()
    const mockResponse = { data: { code: 200 } }
    api.post = vi.fn().mockResolvedValue(mockResponse)

    const result = await uploadVideo(mockFormData)

    expect(api.post).toHaveBeenCalledWith('/video/upload', mockFormData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    expect(result).toEqual(mockResponse)
  })

  it('likeVideo 应该发送正确的POST请求', async () => {
    const mockResponse = { data: { code: 200, data: true } }
    api.post = vi.fn().mockResolvedValue(mockResponse)

    const result = await likeVideo(1, 1)

    expect(api.post).toHaveBeenCalledWith('/video/like', { videoId: 1, userId: 1 })
    expect(result).toEqual(mockResponse)
  })

  it('favoriteVideo 应该发送正确的POST请求', async () => {
    const mockResponse = { data: { code: 200, data: false } }
    api.post = vi.fn().mockResolvedValue(mockResponse)

    const result = await favoriteVideo(1, 1)

    expect(api.post).toHaveBeenCalledWith('/video/favorite', { videoId: 1, userId: 1 })
    expect(result).toEqual(mockResponse)
  })

  it('addComment 应该发送正确的POST请求', async () => {
    const mockResponse = { data: { code: 200 } }
    api.post = vi.fn().mockResolvedValue(mockResponse)

    const result = await addComment(1, 1, '测试评论')

    expect(api.post).toHaveBeenCalledWith('/video/comment', {
      videoId: 1,
      userId: 1,
      content: '测试评论'
    })
    expect(result).toEqual(mockResponse)
  })

  it('getComments 应该发送正确的GET请求', async () => {
    const mockResponse = { data: { code: 200, data: { content: [] } } }
    api.get = vi.fn().mockResolvedValue(mockResponse)

    const result = await getComments(1, 0, 20)

    expect(api.get).toHaveBeenCalledWith('/video/comments/1', { params: { page: 0, size: 20 } })
    expect(result).toEqual(mockResponse)
  })

  it('viewVideo 应该发送正确的POST请求', async () => {
    const mockResponse = { data: { code: 200 } }
    api.post = vi.fn().mockResolvedValue(mockResponse)

    const result = await viewVideo(1)

    expect(api.post).toHaveBeenCalledWith('/video/view/1')
    expect(result).toEqual(mockResponse)
  })

  it('getAdminVideos 应该发送正确的GET请求', async () => {
    const mockResponse = { data: { code: 200, data: [] } }
    api.get = vi.fn().mockResolvedValue(mockResponse)

    const result = await getAdminVideos()

    expect(api.get).toHaveBeenCalledWith('/video/admin/list')
    expect(result).toEqual(mockResponse)
  })

  it('deleteVideo 应该发送正确的DELETE请求', async () => {
    const mockResponse = { data: { code: 200 } }
    api.delete = vi.fn().mockResolvedValue(mockResponse)

    const result = await deleteVideo(1)

    expect(api.delete).toHaveBeenCalledWith('/video/admin/1')
    expect(result).toEqual(mockResponse)
  })
})
