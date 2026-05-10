import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'

vi.mock('../api', () => ({
  login: vi.fn()
}))

import { login } from '../api'
import Login from './Login.vue'

const routes = [
  { path: '/login', component: Login },
  { path: '/mobile', component: { template: '<div>Mobile</div>' } },
  { path: '/admin', component: { template: '<div>Admin</div>' } }
]

function createWrapper() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes
  })
  return mount(Login, {
    global: {
      plugins: [router]
    }
  })
}

describe('Login.vue 登录组件', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('表单渲染', () => {
    it('渲染用户名输入框', () => {
      const wrapper = createWrapper()
      const usernameInput = wrapper.find('input[type="text"]')
      expect(usernameInput.exists()).toBe(true)
    })

    it('渲染密码输入框', () => {
      const wrapper = createWrapper()
      const passwordInput = wrapper.find('input[type="password"]')
      expect(passwordInput.exists()).toBe(true)
    })

    it('渲染登录按钮', () => {
      const wrapper = createWrapper()
      const loginButton = wrapper.find('button[type="submit"]')
      expect(loginButton.exists()).toBe(true)
    })
  })

  describe('前端表单校验', () => {
    it('用户名为空时显示错误', async () => {
      const wrapper = createWrapper()
      await wrapper.find('input[type="text"]').setValue('')
      await wrapper.find('input[type="password"]').setValue('password123')
      await wrapper.find('form').trigger('submit.prevent')
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.error').exists()).toBe(true)
    })

    it('密码为空时显示错误', async () => {
      const wrapper = createWrapper()
      await wrapper.find('input[type="text"]').setValue('testuser')
      await wrapper.find('input[type="password"]').setValue('')
      await wrapper.find('form').trigger('submit.prevent')
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.error').exists()).toBe(true)
    })
  })

  describe('登录功能', () => {
    it('普通用户登录成功后跳转到 /mobile', async () => {
      const wrapper = createWrapper()
      const router = wrapper.vm.$router

      login.mockResolvedValue({
        data: {
          code: 200,
          data: {
            id: 1,
            username: 'testuser',
            nickname: '测试用户',
            isAdmin: false,
            token: 'mock-token'
          }
        }
      })

      await wrapper.find('input[type="text"]').setValue('testuser')
      await wrapper.find('input[type="password"]').setValue('password123')
      await wrapper.find('form').trigger('submit.prevent')
      await wrapper.vm.$nextTick()
      await router.isReady()

      expect(login).toHaveBeenCalledWith('testuser', 'password123')
      expect(localStorage.setItem).toHaveBeenCalled()
      expect(router.currentRoute.value.path).toBe('/mobile')
    })

    it('管理员登录成功后跳转到 /admin', async () => {
      const wrapper = createWrapper()
      const router = wrapper.vm.$router

      login.mockResolvedValue({
        data: {
          code: 200,
          data: {
            id: 2,
            username: 'admin',
            nickname: '管理员',
            isAdmin: true,
            token: 'admin-token'
          }
        }
      })

      await wrapper.find('input[type="text"]').setValue('admin')
      await wrapper.find('input[type="password"]').setValue('admin123')
      await wrapper.find('form').trigger('submit.prevent')
      await wrapper.vm.$nextTick()
      await router.isReady()

      expect(router.currentRoute.value.path).toBe('/admin')
    })

    it('登录失败时显示错误信息', async () => {
      const wrapper = createWrapper()

      login.mockResolvedValue({
        data: {
          code: 401,
          message: '用户名或密码错误'
        }
      })

      await wrapper.find('input[type="text"]').setValue('testuser')
      await wrapper.find('input[type="password"]').setValue('wrongpassword')
      await wrapper.find('form').trigger('submit.prevent')
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.error').text()).toBe('用户名或密码错误')
    })
  })
})
