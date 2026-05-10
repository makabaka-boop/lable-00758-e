import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import Login from '../views/Login.vue'

const mockLogin = vi.fn()
vi.mock('../api', () => ({
  login: (...args) => mockLogin(...args)
}))

describe('Login.vue', () => {
  let router

  beforeEach(async () => {
    vi.clearAllMocks()
    localStorage.clear()
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/login', component: Login },
        { path: '/mobile', component: { template: '<div>Mobile</div>' } },
        { path: '/admin', component: { template: '<div>Admin</div>' } }
      ]
    })
    await router.push('/login')
    await router.isReady()
  })

  const mountLogin = () => mount(Login, {
    global: {
      plugins: [router]
    }
  })

  it('should render login form', () => {
    const wrapper = mountLogin()
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
  })

  it('should show error when username is empty', async () => {
    const wrapper = mountLogin()
    const form = wrapper.find('form')

    await form.trigger('submit.prevent')

    expect(wrapper.find('.error').exists()).toBe(true)
  })

  it('should show error when username is too short', async () => {
    const wrapper = mountLogin()

    await wrapper.find('input[type="text"]').setValue('a')
    await wrapper.find('input[type="password"]').setValue('password123')
    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.find('.error').text()).toContain('用户名')
  })

  it('should show error when username is too long', async () => {
    const wrapper = mountLogin()

    await wrapper.find('input[type="text"]').setValue('a'.repeat(21))
    await wrapper.find('input[type="password"]').setValue('password123')
    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.find('.error').text()).toContain('用户名')
  })

  it('should show error when password is empty', async () => {
    const wrapper = mountLogin()

    await wrapper.find('input[type="text"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('')
    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.find('.error').exists()).toBe(true)
  })

  it('should show error when password is too short', async () => {
    const wrapper = mountLogin()

    await wrapper.find('input[type="text"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('12345')
    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.find('.error').text()).toContain('密码')
  })

  it('should call login API with correct params on valid form', async () => {
    const wrapper = mountLogin()

    mockLogin.mockResolvedValue({
      data: { code: 200, data: { id: 1, username: 'admin', isAdmin: true, token: 'jwt' } }
    })

    await wrapper.find('input[type="text"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('admin123')
    await wrapper.find('form').trigger('submit.prevent')

    expect(mockLogin).toHaveBeenCalledWith('admin', 'admin123')
  })

  it('should show error on login failure (wrong credentials)', async () => {
    const wrapper = mountLogin()

    mockLogin.mockResolvedValue({
      data: { code: 401, message: '用户名或密码错误' }
    })

    await wrapper.find('input[type="text"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('wrongpass')
    await wrapper.find('form').trigger('submit.prevent')

    await vi.waitFor(() => {
      expect(wrapper.find('.error').exists()).toBe(true)
    })
  })

  it('should show error on network error', async () => {
    const wrapper = mountLogin()

    mockLogin.mockRejectedValue(new Error('Network Error'))

    await wrapper.find('input[type="text"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('admin123')
    await wrapper.find('form').trigger('submit.prevent')

    await vi.waitFor(() => {
      expect(wrapper.find('.error').exists()).toBe(true)
    })
  })

  it('should store user data in localStorage on successful login', async () => {
    const wrapper = mountLogin()
    const userData = { id: 1, username: 'admin', isAdmin: true, token: 'jwt' }

    mockLogin.mockResolvedValue({
      data: { code: 200, data: userData }
    })

    await wrapper.find('input[type="text"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('admin123')
    await wrapper.find('form').trigger('submit.prevent')

    await vi.waitFor(() => {
      const stored = JSON.parse(localStorage.getItem('user'))
      expect(stored).toEqual(userData)
    })
  })

  it('should disable submit button while loading', async () => {
    const wrapper = mountLogin()

    mockLogin.mockReturnValue(new Promise(() => {}))

    await wrapper.find('input[type="text"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('admin123')
    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.find('button').attributes('disabled')).toBeDefined()
  })

  it('should trim username before submitting', async () => {
    const wrapper = mountLogin()

    mockLogin.mockResolvedValue({
      data: { code: 200, data: { id: 1, username: 'admin', isAdmin: true, token: 'jwt' } }
    })

    await wrapper.find('input[type="text"]').setValue('  admin  ')
    await wrapper.find('input[type="password"]').setValue('admin123')
    await wrapper.find('form').trigger('submit.prevent')

    expect(mockLogin).toHaveBeenCalledWith('admin', 'admin123')
  })
})
