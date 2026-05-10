import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import Admin from '../views/Admin.vue'

const mockGetAdminVideos = vi.fn()
const mockDeleteVideo = vi.fn()
const mockUploadVideo = vi.fn()
const mockGetComments = vi.fn()

vi.mock('../api', () => ({
  getAdminVideos: (...args) => mockGetAdminVideos(...args),
  deleteVideo: (...args) => mockDeleteVideo(...args),
  uploadVideo: (...args) => mockUploadVideo(...args),
  getComments: (...args) => mockGetComments(...args)
}))

describe('Admin.vue', () => {
  let router

  beforeEach(async () => {
    vi.clearAllMocks()
    localStorage.clear()
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/admin', component: Admin },
        { path: '/login', component: { template: '<div>Login</div>' } }
      ]
    })
  })

  const mountAdmin = async (user = { id: 1, nickname: '管理员', isAdmin: true }) => {
    localStorage.setItem('user', JSON.stringify(user))
    await router.push('/admin')
    await router.isReady()
    const wrapper = mount(Admin, { global: { plugins: [router] } })
    await flushPromises()
    return wrapper
  }

  it('should redirect to login if user is not admin', async () => {
    const pushSpy = vi.spyOn(router, 'push')
    localStorage.setItem('user', JSON.stringify({ id: 2, isAdmin: false }))
    mockGetAdminVideos.mockResolvedValue({ data: { code: 200, data: [] } })

    await router.push('/admin')
    await router.isReady()
    mount(Admin, { global: { plugins: [router] } })
    await flushPromises()

    expect(pushSpy).toHaveBeenCalledWith('/login')
  })

  it('should redirect to login if no user', async () => {
    const pushSpy = vi.spyOn(router, 'push')
    mockGetAdminVideos.mockResolvedValue({ data: { code: 200, data: [] } })

    await router.push('/admin')
    await router.isReady()
    mount(Admin, { global: { plugins: [router] } })
    await flushPromises()

    expect(pushSpy).toHaveBeenCalledWith('/login')
  })

  it('should load videos on mount for admin user', async () => {
    mockGetAdminVideos.mockResolvedValue({ data: { code: 200, data: [] } })
    await mountAdmin()

    expect(mockGetAdminVideos).toHaveBeenCalled()
  })

  it('should display video list', async () => {
    const videos = [
      { id: 1, title: '测试视频1', description: '描述1', videoUrl: '/v1.mp4', likeCount: 5, favoriteCount: 3, commentCount: 2, viewCount: 10 },
      { id: 2, title: '测试视频2', description: '描述2', videoUrl: '/v2.mp4', likeCount: 1, favoriteCount: 0, commentCount: 0, viewCount: 2 }
    ]
    mockGetAdminVideos.mockResolvedValue({ data: { code: 200, data: videos } })

    const wrapper = await mountAdmin()

    expect(wrapper.text()).toContain('测试视频1')
    expect(wrapper.text()).toContain('测试视频2')
  })

  it('should show empty message when no videos', async () => {
    mockGetAdminVideos.mockResolvedValue({ data: { code: 200, data: [] } })

    const wrapper = await mountAdmin()

    expect(wrapper.text()).toContain('暂无视频')
  })

  it('should display admin header with user nickname', async () => {
    mockGetAdminVideos.mockResolvedValue({ data: { code: 200, data: [] } })
    const wrapper = await mountAdmin({ id: 1, nickname: '超级管理员', isAdmin: true })

    expect(wrapper.text()).toContain('超级管理员')
  })

  it('should clear localStorage and redirect on logout', async () => {
    mockGetAdminVideos.mockResolvedValue({ data: { code: 200, data: [] } })
    const pushSpy = vi.spyOn(router, 'push')
    const wrapper = await mountAdmin()

    await wrapper.find('.user-info button').trigger('click')

    expect(localStorage.getItem('user')).toBeNull()
    expect(pushSpy).toHaveBeenCalledWith('/login')
  })

  it('should open upload modal', async () => {
    mockGetAdminVideos.mockResolvedValue({ data: { code: 200, data: [] } })
    const wrapper = await mountAdmin()

    expect(wrapper.find('.upload-modal').exists()).toBe(false)

    await wrapper.find('.btn-upload-trigger').trigger('click')

    expect(wrapper.find('.upload-modal').exists()).toBe(true)
  })

  it('should close upload modal on cancel', async () => {
    mockGetAdminVideos.mockResolvedValue({ data: { code: 200, data: [] } })
    const wrapper = await mountAdmin()

    await wrapper.find('.btn-upload-trigger').trigger('click')
    expect(wrapper.find('.upload-modal').exists()).toBe(true)

    await wrapper.find('.btn-cancel').trigger('click')
    expect(wrapper.find('.upload-modal').exists()).toBe(false)
  })

  it('should show error when uploading without file and title', async () => {
    mockGetAdminVideos.mockResolvedValue({ data: { code: 200, data: [] } })
    const wrapper = await mountAdmin()

    await wrapper.find('.btn-upload-trigger').trigger('click')
    await wrapper.find('.btn-upload').trigger('click')

    expect(wrapper.find('.msg.error').exists()).toBe(true)
  })

  it('should format file size correctly', async () => {
    mockGetAdminVideos.mockResolvedValue({ data: { code: 200, data: [] } })
    const wrapper = await mountAdmin()
    const vm = wrapper.vm

    expect(vm.formatFileSize(0)).toBe('0 B')
    expect(vm.formatFileSize(1024)).toBe('1 KB')
    expect(vm.formatFileSize(1048576)).toBe('1 MB')
    expect(vm.formatFileSize(1073741824)).toBe('1 GB')
  })
})
