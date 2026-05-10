import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'

vi.mock('../api', () => ({
  getAdminVideos: vi.fn(),
  uploadVideo: vi.fn(),
  deleteVideo: vi.fn(),
  getComments: vi.fn()
}))

import { getAdminVideos } from '../api'
import Admin from './Admin.vue'

const routes = [
  { path: '/admin', component: Admin },
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

  return mount(Admin, {
    global: {
      plugins: [router]
    },
    attachTo: document.body
  })
}

describe('Admin.vue 管理后台组件', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('权限控制', () => {
    it('未登录时跳转到登录页', async () => {
      const wrapper = createWrapper(null)
      const router = wrapper.vm.$router
      await wrapper.vm.$nextTick()
      await router.isReady()
      expect(router.currentRoute.value.path).toBe('/login')
    })

    it('非管理员时跳转到登录页', async () => {
      const wrapper = createWrapper({ id: 1, username: 'user', isAdmin: false })
      const router = wrapper.vm.$router
      await wrapper.vm.$nextTick()
      await router.isReady()
      expect(router.currentRoute.value.path).toBe('/login')
    })

    it('管理员时正常加载', async () => {
      getAdminVideos.mockResolvedValue({
        data: { code: 200, data: [] }
      })

      const wrapper = createWrapper({ id: 2, username: 'admin', nickname: '管理员', isAdmin: true })

      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 100))
      await wrapper.vm.$nextTick()
      
      expect(getAdminVideos).toHaveBeenCalled()
    })
  })

  describe('视频列表', () => {
    it('加载视频列表', async () => {
      const mockVideos = [
        { id: 1, title: '视频1', description: '描述1', videoUrl: '/v1.mp4', likeCount: 10, favoriteCount: 5, commentCount: 3, viewCount: 100 }
      ]

      getAdminVideos.mockResolvedValue({
        data: { code: 200, data: mockVideos }
      })

      const wrapper = createWrapper({ id: 2, username: 'admin', isAdmin: true })
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 100))
      await wrapper.vm.$nextTick()

      const videoCards = wrapper.findAll('.video-card')
      expect(videoCards.length).toBe(1)
    })
  })

  describe('退出登录', () => {
    it('点击退出清除用户信息清除 localStorage', async () => {
      getAdminVideos.mockResolvedValue({
        data: { code: 200, data: [] }
      })

      const wrapper = createWrapper({ id: 2, username: 'admin', isAdmin: true })
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 100))
      await wrapper.vm.$nextTick()

      const logoutBtn = wrapper.find('.user-info button')
      await logoutBtn.trigger('click')
      await wrapper.vm.$nextTick()
      
      expect(localStorage.removeItem).toHaveBeenCalledWith('user')
    })
  })

  describe('文件上传', () => {
    it('点击上传按钮打开弹窗', async () => {
      getAdminVideos.mockResolvedValue({
        data: { code: 200, data: [] }
      })

      const wrapper = createWrapper({ id: 2, username: 'admin', isAdmin: true })
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 100))
      await wrapper.vm.$nextTick()

      const uploadTriggerBtn = wrapper.find('.btn-upload-trigger')
      await uploadTriggerBtn.trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.upload-modal').exists()).toBe(true)
    })
  })
})
