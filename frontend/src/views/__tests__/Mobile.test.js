import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import Mobile from '../Mobile.vue'
import * as api from '../../api'

vi.mock('../../api')

vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRouter: () => ({
      push: vi.fn()
    })
  }
})

describe('Mobile.vue 组件测试', () => {
  let wrapper
  let router

  const createWrapper = () => {
    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', redirect: '/mobile' },
        { path: '/mobile', name: 'Mobile', component: Mobile },
        { path: '/login', name: 'Login' }
      ]
    })
    vi.spyOn(router, 'push')
    return mount(Mobile, {
      global: {
        plugins: [router]
      }
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    Object.defineProperty(window, 'location', {
      value: { reload: vi.fn() },
      writable: true
    })
    api.getVideos.mockResolvedValue({
      data: {
        code: 200,
        data: {
          content: [
            { id: 1, title: '测试视频1', username: '用户1', liked: false, likeCount: 10, favoriteCount: 5, commentCount: 3 },
            { id: 2, title: '测试视频2', username: '用户2', liked: true, likeCount: 20, favoriteCount: 15, commentCount: 8 }
          ]
        }
      }
    })
    api.getComments.mockResolvedValue({
      data: {
        code: 200,
        data: {
          content: []
        }
      }
    })
  })

  it('应该正确渲染移动端页面', async () => {
    wrapper = createWrapper()
    await new Promise(resolve => setTimeout(resolve, 100))
    expect(wrapper.find('.mobile-app').exists()).toBe(true)
  })

  it('未登录时应该显示登录按钮', async () => {
    localStorage.getItem.mockReturnValue(null)
    wrapper = createWrapper()
    await new Promise(resolve => setTimeout(resolve, 100))
    expect(wrapper.find('.header span').text()).toBe('登录')
  })

  it('已登录时应该显示退出按钮', async () => {
    const mockUser = { id: 1, username: 'testuser', token: 'token' }
    localStorage.getItem.mockReturnValue(JSON.stringify(mockUser))
    wrapper = createWrapper()
    await new Promise(resolve => setTimeout(resolve, 100))
    expect(wrapper.find('.header span').text()).toBe('退出')
  })

  it('点击登录按钮应该跳转到登录页', async () => {
    localStorage.getItem.mockReturnValue(null)
    wrapper = createWrapper()
    await new Promise(resolve => setTimeout(resolve, 100))
    await wrapper.find('.header span').trigger('click')
    expect(router.push).toHaveBeenCalledWith('/login')
  })

  it('点击退出按钮应该清除用户信息并刷新页面', async () => {
    const mockUser = { id: 1, username: 'testuser', token: 'token' }
    localStorage.getItem.mockReturnValue(JSON.stringify(mockUser))
    wrapper = createWrapper()
    await new Promise(resolve => setTimeout(resolve, 100))
    await wrapper.find('.header span').trigger('click')
    expect(localStorage.removeItem).toHaveBeenCalledWith('user')
    expect(window.location.reload).toHaveBeenCalled()
  })

  it('应该加载视频列表', async () => {
    wrapper = createWrapper()
    await new Promise(resolve => setTimeout(resolve, 100))
    expect(api.getVideos).toHaveBeenCalled()
  })

  it('未登录用户点赞应该显示登录提示', async () => {
    localStorage.getItem.mockReturnValue(null)
    wrapper = createWrapper()
    await new Promise(resolve => setTimeout(resolve, 100))
    const vm = wrapper.vm
    const video = { id: 1, liked: false, likeCount: 10 }
    await vm.handleLike(video)
    expect(vm.toastMessage).toBe('请先登录才能点赞')
    expect(api.likeVideo).not.toHaveBeenCalled()
  })

  it('已登录用户点赞应该调用API', async () => {
    const mockUser = { id: 1, username: 'testuser', token: 'token' }
    localStorage.getItem.mockReturnValue(JSON.stringify(mockUser))
    api.likeVideo.mockResolvedValue({
      data: { code: 200, data: true }
    })
    wrapper = createWrapper()
    await new Promise(resolve => setTimeout(resolve, 100))
    const vm = wrapper.vm
    const video = { id: 1, liked: false, likeCount: 10 }
    await vm.handleLike(video)
    expect(api.likeVideo).toHaveBeenCalledWith(1, 1)
  })

  it('未登录用户收藏应该显示登录提示', async () => {
    localStorage.getItem.mockReturnValue(null)
    wrapper = createWrapper()
    await new Promise(resolve => setTimeout(resolve, 100))
    const vm = wrapper.vm
    const video = { id: 1, favorited: false, favoriteCount: 5 }
    await vm.handleFavorite(video)
    expect(vm.toastMessage).toBe('请先登录才能收藏')
    expect(api.favoriteVideo).not.toHaveBeenCalled()
  })

  it('已登录用户收藏应该调用API', async () => {
    const mockUser = { id: 1, username: 'testuser', token: 'token' }
    localStorage.getItem.mockReturnValue(JSON.stringify(mockUser))
    api.favoriteVideo.mockResolvedValue({
      data: { code: 200, data: true }
    })
    wrapper = createWrapper()
    await new Promise(resolve => setTimeout(resolve, 100))
    const vm = wrapper.vm
    const video = { id: 1, favorited: false, favoriteCount: 5 }
    await vm.handleFavorite(video)
    expect(api.favoriteVideo).toHaveBeenCalledWith(1, 1)
  })

  it('取消提示应该关闭弹窗', async () => {
    wrapper = createWrapper()
    await new Promise(resolve => setTimeout(resolve, 100))
    const vm = wrapper.vm
    vm.toastMessage = '测试提示'
    vm.pendingAction = 'like'
    vm.cancelToast()
    expect(vm.toastMessage).toBe('')
    expect(vm.pendingAction).toBeNull()
  })

  it('确认提示应该跳转到登录页', async () => {
    wrapper = createWrapper()
    await new Promise(resolve => setTimeout(resolve, 100))
    const vm = wrapper.vm
    vm.toastMessage = '测试提示'
    vm.pendingAction = 'like'
    vm.confirmToast()
    expect(vm.toastMessage).toBe('')
  })
})
