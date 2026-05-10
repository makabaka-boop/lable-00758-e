import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'

vi.mock('../api', () => ({
  getVideos: vi.fn(),
  likeVideo: vi.fn(),
  favoriteVideo: vi.fn(),
  addComment: vi.fn(),
  getComments: vi.fn(),
  viewVideo: vi.fn()
}))

import { getVideos, likeVideo, favoriteVideo, addComment, getComments } from '../api'
import Mobile from './Mobile.vue'

const routes = [
  { path: '/mobile', component: Mobile },
  { path: '/login', component: { template: '<div>Login</div>' } }
]

function createWrapper(userData) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes
  })

  if (userData) {
    localStorage.getItem.mockReturnValue(JSON.stringify(userData))
  }

  return mount(Mobile, {
    global: {
      plugins: [router]
    },
    attachTo: document.body
  })
}

describe('Mobile.vue 移动端组件', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    Object.defineProperty(HTMLMediaElement.prototype, 'pause', { value: vi.fn() })
    Object.defineProperty(HTMLMediaElement.prototype, 'play', {
      value: vi.fn(() => Promise.resolve())
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('视频列表加载', () => {
    it('组件加载时调用 getVideos', async () => {
      getVideos.mockResolvedValue({
        data: { code: 200, data: { content: [] } }
      })

      const wrapper = createWrapper(null)
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 400))
      await wrapper.vm.$nextTick()

      expect(getVideos).toHaveBeenCalled()
    })

    it('加载视频列表成功', async () => {
      const mockVideos = [
        { id: 1, title: '视频1', description: '描述1', videoUrl: '/v1.mp4', likeCount: 10, favoriteCount: 5, commentCount: 3, viewCount: 100 }
      ]

      getVideos.mockResolvedValue({
        data: { code: 200, data: { content: mockVideos } }
      })

      const wrapper = createWrapper(null)
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 400))
      await wrapper.vm.$nextTick()

      const videoItems = wrapper.findAll('.video-item')
      expect(videoItems.length).toBe(1)
    })
  })

  describe('点赞功能', () => {
    it('未登录时点击点赞显示提示', async () => {
      const mockVideos = [
        { id: 1, title: '视频1', videoUrl: '/v1.mp4', likeCount: 10, favoriteCount: 5, commentCount: 3, viewCount: 100 }
      ]

      getVideos.mockResolvedValue({
        data: { code: 200, data: { content: mockVideos } }
      })

      const wrapper = createWrapper(null)
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 400))
      await wrapper.vm.$nextTick()

      const actionBtns = wrapper.findAll('.action-btn')
      const likeBtn = actionBtns[0]
      await likeBtn.trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.toast-modal').exists()).toBe(true)
    })

    it('已登录时点赞成功', async () => {
      const mockVideos = [
        { id: 1, title: '视频1', videoUrl: '/v1.mp4', likeCount: 10, favoriteCount: 5, commentCount: 3, viewCount: 100 }
      ]

      getVideos.mockResolvedValue({
        data: { code: 200, data: { content: mockVideos } }
      })
      likeVideo.mockResolvedValue({
        data: { code: 200, data: true }
      })

      const wrapper = createWrapper({ id: 2, username: 'testuser', nickname: '测试用户', token: 'mock-token' })
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 400))
      await wrapper.vm.$nextTick()

      const actionBtns = wrapper.findAll('.action-btn')
      const likeBtn = actionBtns[0]
      await likeBtn.trigger('click')
      await wrapper.vm.$nextTick()

      expect(likeVideo).toHaveBeenCalledWith(1, 2)
    })
  })

  describe('收藏功能', () => {
    it('未登录时点击收藏显示提示', async () => {
      const mockVideos = [
        { id: 1, title: '视频1', videoUrl: '/v1.mp4', likeCount: 10, favoriteCount: 5, commentCount: 3, viewCount: 100 }
      ]

      getVideos.mockResolvedValue({
        data: { code: 200, data: { content: mockVideos } }
      })

      const wrapper = createWrapper(null)
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 400))
      await wrapper.vm.$nextTick()

      const actionBtns = wrapper.findAll('.action-btn')
      const favoriteBtn = actionBtns[1]
      await favoriteBtn.trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.toast-modal').exists()).toBe(true)
    })
  })

  describe('评论功能', () => {
    it('点击评论按钮打开评论弹窗', async () => {
      const mockVideos = [
        { id: 1, title: '视频1', videoUrl: '/v1.mp4', likeCount: 10, favoriteCount: 5, commentCount: 3, viewCount: 100 }
      ]

      getVideos.mockResolvedValue({
        data: { code: 200, data: { content: mockVideos } }
      })
      getComments.mockResolvedValue({
        data: { code: 200, data: { content: [] } }
      })

      const wrapper = createWrapper(null)
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 400))
      await wrapper.vm.$nextTick()

      const actionBtns = wrapper.findAll('.action-btn')
      const commentBtn = actionBtns[2]
      await commentBtn.trigger('click')
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 100))
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.comments-modal').exists()).toBe(true)
    })
  })

  describe('退出登录', () => {
    it('已登录时显示退出按钮', async () => {
      getVideos.mockResolvedValue({
        data: { code: 200, data: { content: [] } }
      })

      const wrapper = createWrapper({ id: 2, username: 'testuser', nickname: '测试用户', token: 'mock-token' })
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 400))
      await wrapper.vm.$nextTick()

      const headerSpan = wrapper.find('.header span')
      expect(headerSpan.text()).toBe('退出')
    })

    it('未登录时显示登录按钮', async () => {
      getVideos.mockResolvedValue({
        data: { code: 200, data: { content: [] } }
      })

      const wrapper = createWrapper(null)
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 400))
      await wrapper.vm.$nextTick()

      const headerSpan = wrapper.find('.header span')
      expect(headerSpan.text()).toBe('登录')
    })
  })
})
