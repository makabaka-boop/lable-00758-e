import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('./index', () => ({
  login: vi.fn(),
  register: vi.fn(),
  getVideos: vi.fn(),
  uploadVideo: vi.fn(),
  likeVideo: vi.fn(),
  favoriteVideo: vi.fn(),
  addComment: vi.fn(),
  getComments: vi.fn(),
  viewVideo: vi.fn(),
  getAdminVideos: vi.fn(),
  deleteVideo: vi.fn(),
  default: {
    post: vi.fn(),
    get: vi.fn(),
    delete: vi.fn()
  }
}))

import {
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
} from './index'

describe('API 封装模块', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('HTTP 方法', () => {
    it('login 调用 POST /user/login', async () => {
      const mockResponse = { data: { code: 200, message: 'success', data: { token: 'mock-token' } } }
      login.mockResolvedValue(mockResponse)

      const result = await login('testuser', 'password123')

      expect(login).toHaveBeenCalled()
      expect(result).toEqual(mockResponse)
    })

    it('register 调用 POST /user/register', async () => {
      const mockResponse = { data: { code: 200, message: 'success' } }
      register.mockResolvedValue(mockResponse)

      const result = await register('newuser', 'password123', '昵称')

      expect(register).toHaveBeenCalled()
      expect(result).toEqual(mockResponse)
    })

    it('getVideos 调用 GET /video/list', async () => {
      const mockResponse = { data: { code: 200, data: { content: [] } } }
      getVideos.mockResolvedValue(mockResponse)

      const result = await getVideos(0, 10, 1)

      expect(getVideos).toHaveBeenCalled()
      expect(result).toEqual(mockResponse)
    })

    it('uploadVideo 调用 POST /video/upload', async () => {
      const mockResponse = { data: { code: 200, data: { id: 1 } } }
      uploadVideo.mockResolvedValue(mockResponse)

      const formData = new FormData()
      formData.append('file', new Blob(['video content'], { type: 'video/mp4' }))

      const result = await uploadVideo(formData)

      expect(uploadVideo).toHaveBeenCalled()
      expect(result).toEqual(mockResponse)
    })

    it('likeVideo 调用 POST /video/like', async () => {
      const mockResponse = { data: { code: 200, data: true } }
      likeVideo.mockResolvedValue(mockResponse)

      const result = await likeVideo(1, 2)

      expect(likeVideo).toHaveBeenCalled()
      expect(result).toEqual(mockResponse)
    })

    it('favoriteVideo 调用 POST /video/favorite', async () => {
      const mockResponse = { data: { code: 200, data: true } }
      favoriteVideo.mockResolvedValue(mockResponse)

      const result = await favoriteVideo(1, 2)

      expect(favoriteVideo).toHaveBeenCalled()
      expect(result).toEqual(mockResponse)
    })

    it('addComment 调用 POST /video/comment', async () => {
      const mockResponse = { data: { code: 200, data: { id: 1 } } }
      addComment.mockResolvedValue(mockResponse)

      const result = await addComment(1, 2, '评论内容')

      expect(addComment).toHaveBeenCalled()
      expect(result).toEqual(mockResponse)
    })

    it('getComments 调用 GET /video/comments/{id}', async () => {
      const mockResponse = { data: { code: 200, data: { content: [] } } }
      getComments.mockResolvedValue(mockResponse)

      const result = await getComments(1, 0, 10)

      expect(getComments).toHaveBeenCalled()
      expect(result).toEqual(mockResponse)
    })

    it('viewVideo 调用 POST /video/view/{id}', async () => {
      const mockResponse = { data: { code: 200 } }
      viewVideo.mockResolvedValue(mockResponse)

      const result = await viewVideo(1)

      expect(viewVideo).toHaveBeenCalled()
      expect(result).toEqual(mockResponse)
    })

    it('getAdminVideos 调用 GET /video/admin/list', async () => {
      const mockResponse = { data: { code: 200, data: [] } }
      getAdminVideos.mockResolvedValue(mockResponse)

      const result = await getAdminVideos()

      expect(getAdminVideos).toHaveBeenCalled()
      expect(result).toEqual(mockResponse)
    })

    it('deleteVideo 调用 DELETE /video/admin/{id}', async () => {
      const mockResponse = { data: { code: 200 } }
      deleteVideo.mockResolvedValue(mockResponse)

      const result = await deleteVideo(1)

      expect(deleteVideo).toHaveBeenCalled()
      expect(result).toEqual(mockResponse)
    })
  })
})
