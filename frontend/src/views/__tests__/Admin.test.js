import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import Admin from '../Admin.vue'
import * as api from '../../api'

vi.mock('../../api')

const mockPush = vi.fn()

vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRouter: () => ({
      push: mockPush
    })
  }
})

describe('Admin.vue 组件测试', () => {
  let wrapper

  const createWrapper = () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', redirect: '/admin' },
        { path: '/admin', name: 'Admin', component: Admin },
        { path: '/login', name: 'Login' }
      ]
    })
    return mount(Admin, {
      global: {
        plugins: [router]
      }
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    window.confirm = vi.fn(() => true)
  })

  it('非管理员用户应该重定向到登录页', async () => {
    const mockUser = { id: 1, username: 'testuser', isAdmin: false, token: 'token' }
    localStorage.getItem.mockReturnValue(JSON.stringify(mockUser))
    api.getAdminVideos.mockResolvedValue({ data: { code: 200, data: [] } })
    wrapper = createWrapper()
    await new Promise(resolve => setTimeout(resolve, 0))
    expect(mockPush).toHaveBeenCalledWith('/login')
  })

  it('未登录用户应该重定向到登录页', async () => {
    localStorage.getItem.mockReturnValue(null)
    wrapper = createWrapper()
    await new Promise(resolve => setTimeout(resolve, 0))
    expect(mockPush).toHaveBeenCalledWith('/login')
  })

  describe('已登录管理员', () => {
    beforeEach(async () => {
      const mockUser = { id: 1, username: 'admin', nickname: '管理员', isAdmin: true, token: 'token' }
      localStorage.getItem.mockReturnValue(JSON.stringify(mockUser))
      api.getAdminVideos.mockResolvedValue({
        data: {
          code: 200,
          data: [
            { id: 1, title: '测试视频1', likeCount: 10, favoriteCount: 5, commentCount: 3, viewCount: 100 }
          ]
        }
      })
      wrapper = createWrapper()
      await new Promise(resolve => setTimeout(resolve, 0))
    })

    it('应该正确渲染管理员页面', () => {
      expect(wrapper.find('.admin-page').exists()).toBe(true)
    })

    it('应该显示用户信息', () => {
      expect(wrapper.find('.user-info span').text()).toBe('管理员')
    })

    it('应该加载视频列表', () => {
      expect(api.getAdminVideos).toHaveBeenCalled()
    })

    it('退出登录应该清除用户信息并重定向', async () => {
      await wrapper.find('.user-info button').trigger('click')
      expect(localStorage.removeItem).toHaveBeenCalledWith('user')
      expect(mockPush).toHaveBeenCalledWith('/login')
    })
  })

  describe('删除功能', () => {
    beforeEach(async () => {
      const mockUser = { id: 1, username: 'admin', isAdmin: true, token: 'token' }
      localStorage.getItem.mockReturnValue(JSON.stringify(mockUser))
      api.getAdminVideos.mockResolvedValue({
        data: {
          code: 200,
          data: [{ id: 1, title: '测试视频' }]
        }
      })
      api.deleteVideo.mockResolvedValue({ data: { code: 200 } })
      wrapper = createWrapper()
      await new Promise(resolve => setTimeout(resolve, 0))
    })

    it('删除视频应该调用API并重新加载列表', async () => {
      await wrapper.find('.btn-delete').trigger('click')
      expect(window.confirm).toHaveBeenCalled()
      expect(api.deleteVideo).toHaveBeenCalledWith(1)
      expect(api.getAdminVideos).toHaveBeenCalledTimes(2)
    })
  })
})
