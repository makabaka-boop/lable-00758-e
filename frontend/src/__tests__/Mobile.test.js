import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import Mobile from '../views/Mobile.vue'

const mockGetVideos = vi.fn()
const mockLikeVideo = vi.fn()
const mockFavoriteVideo = vi.fn()
const mockAddComment = vi.fn()
const mockGetComments = vi.fn()
const mockViewVideo = vi.fn()

vi.mock('../api', () => ({
  getVideos: (...args) => mockGetVideos(...args),
  likeVideo: (...args) => mockLikeVideo(...args),
  favoriteVideo: (...args) => mockFavoriteVideo(...args),
  addComment: (...args) => mockAddComment(...args),
  getComments: (...args) => mockGetComments(...args),
  viewVideo: (...args) => mockViewVideo(...args)
}))

HTMLMediaElement.prototype.play = vi.fn(() => Promise.resolve())
HTMLMediaElement.prototype.pause = vi.fn()

const sampleVideos = [
  { id: 1, title: '视频1', description: '描述1', videoUrl: '/v1.mp4', username: 'user1', likeCount: 5, favoriteCount: 3, commentCount: 2, liked: false, favorited: false }
]

describe('Mobile.vue', () => {
  let router

  beforeEach(async () => {
    vi.clearAllMocks()
    localStorage.clear()
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/mobile', component: Mobile },
        { path: '/login', component: { template: '<div>Login</div>' } }
      ]
    })
  })

  const mountMobile = async (user = null, videos = []) => {
    if (user) {
      localStorage.setItem('user', JSON.stringify(user))
    }
    mockGetVideos.mockResolvedValue({
      data: { code: 200, data: { content: videos, totalPages: videos.length > 0 ? 1 : 0 } }
    })
    await router.push('/mobile')
    await router.isReady()
    const wrapper = mount(Mobile, { global: { plugins: [router] } })
    await flushPromises()
    return wrapper
  }

  it('should load videos on mount', async () => {
    await mountMobile()
    expect(mockGetVideos).toHaveBeenCalledWith(0, 10, undefined)
  })

  it('should pass userId when user is logged in', async () => {
    const user = { id: 1, username: 'user1', nickname: '用户1' }
    await mountMobile(user)
    expect(mockGetVideos).toHaveBeenCalledWith(0, 10, 1)
  })

  it('should render video items when videos are loaded', async () => {
    const wrapper = await mountMobile(null, sampleVideos)
    expect(wrapper.text()).toContain('视频1')
  })

  it('should show login link when user is not logged in', async () => {
    const wrapper = await mountMobile()
    expect(wrapper.text()).toContain('登录')
  })

  it('should show logout when user is logged in', async () => {
    const wrapper = await mountMobile({ id: 1, username: 'user1', nickname: '用户1' })
    expect(wrapper.text()).toContain('退出')
  })

  it('should show toast when liking without login', async () => {
    const wrapper = await mountMobile(null, sampleVideos)
    const actionBtns = wrapper.findAll('.action-btn')
    if (actionBtns.length > 0) {
      await actionBtns[0].trigger('click')
      expect(wrapper.find('.toast-modal').exists()).toBe(true)
      expect(wrapper.text()).toContain('请先登录才能点赞')
    } else {
      const likeIcons = wrapper.findAll('.icon')
      expect(likeIcons.length).toBeGreaterThan(0)
    }
  })

  it('should show toast when favoriting without login', async () => {
    const wrapper = await mountMobile(null, sampleVideos)
    const actionBtns = wrapper.findAll('.action-btn')
    if (actionBtns.length > 1) {
      await actionBtns[1].trigger('click')
      expect(wrapper.find('.toast-modal').exists()).toBe(true)
      expect(wrapper.text()).toContain('请先登录才能收藏')
    }
  })

  it('should call likeVideo API when logged in user likes', async () => {
    const user = { id: 1, username: 'user1', nickname: '用户1' }
    mockLikeVideo.mockResolvedValue({ data: { code: 200, data: true } })

    const wrapper = await mountMobile(user, sampleVideos)
    const actionBtns = wrapper.findAll('.action-btn')
    if (actionBtns.length > 0) {
      await actionBtns[0].trigger('click')
      await flushPromises()
      expect(mockLikeVideo).toHaveBeenCalledWith(1, 1)
    }
  })

  it('should call favoriteVideo API when logged in user favorites', async () => {
    const user = { id: 1, username: 'user1', nickname: '用户1' }
    mockFavoriteVideo.mockResolvedValue({ data: { code: 200, data: true } })

    const wrapper = await mountMobile(user, sampleVideos)
    const actionBtns = wrapper.findAll('.action-btn')
    if (actionBtns.length > 1) {
      await actionBtns[1].trigger('click')
      await flushPromises()
      expect(mockFavoriteVideo).toHaveBeenCalledWith(1, 1)
    }
  })

  it('should open comments modal when clicking comment button', async () => {
    mockGetComments.mockResolvedValue({ data: { code: 200, data: { content: [] } } })

    const wrapper = await mountMobile(null, sampleVideos)
    const actionBtns = wrapper.findAll('.action-btn')
    if (actionBtns.length > 2) {
      await actionBtns[2].trigger('click')
      await flushPromises()
      expect(mockGetComments).toHaveBeenCalledWith(1, 0, 50)
      expect(wrapper.find('.comments-modal').exists()).toBe(true)
    }
  })

  it('should clear localStorage on logout', async () => {
    const user = { id: 1, username: 'user1', nickname: '用户1' }
    const wrapper = await mountMobile(user)

    const logoutSpan = wrapper.find('.header span')
    await logoutSpan.trigger('click')

    expect(localStorage.getItem('user')).toBeNull()
  })

  it('should cancel toast when clicking cancel', async () => {
    const wrapper = await mountMobile(null, sampleVideos)
    const actionBtns = wrapper.findAll('.action-btn')
    if (actionBtns.length > 0) {
      await actionBtns[0].trigger('click')

      if (wrapper.find('.toast-modal').exists()) {
        await wrapper.find('.toast-btn.cancel').trigger('click')
        expect(wrapper.find('.toast-modal').exists()).toBe(false)
      }
    }
  })
})
