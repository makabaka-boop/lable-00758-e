import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import Login from '../Login.vue'
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

describe('Login.vue 组件测试', () => {
  let wrapper

  const createWrapper = () => {
    return mount(Login, {
      global: {
        plugins: [
          createRouter({
            history: createWebHistory(),
            routes: [{ path: '/login', name: 'Login' }, { path: '/mobile', name: 'Mobile' }, { path: '/admin', name: 'Admin' }]
          })
        ]
      }
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    wrapper = createWrapper()
  })

  it('应该正确渲染登录表单', () => {
    expect(wrapper.find('form').exists()).toBe(true)
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
  })

  it('应该有正确的初始状态', () => {
    const vm = wrapper.vm
    expect(vm.form.username).toBe('')
    expect(vm.form.password).toBe('')
    expect(vm.loading).toBe(false)
    expect(vm.error).toBe('')
  })

  it('用户名空时应该显示错误', async () => {
    await wrapper.find('input[type="text"]').setValue('')
    await wrapper.find('input[type="password"]').setValue('password123')
    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.vm.error).toBe('请输入用户名')
  })

  it('密码空时应该显示错误', async () => {
    await wrapper.find('input[type="text"]').setValue('testuser')
    await wrapper.find('input[type="password"]').setValue('')
    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.vm.error).toBe('请输入密码')
  })

  it('登录成功时应该保存用户信息并重定向到mobile', async () => {
    const mockResponse = {
      data: {
        code: 200,
        data: {
          id: 1,
          username: 'testuser',
          nickname: '测试用户',
          isAdmin: false,
          token: 'test-token-123'
        }
      }
    }

    api.login.mockResolvedValue(mockResponse)

    await wrapper.find('input[type="text"]').setValue('testuser')
    await wrapper.find('input[type="password"]').setValue('password123')
    await wrapper.find('form').trigger('submit.prevent')

    await new Promise(resolve => setTimeout(resolve, 0))

    expect(api.login).toHaveBeenCalledWith('testuser', 'password123')
    expect(localStorage.setItem).toHaveBeenCalledWith(
      'user',
      JSON.stringify(mockResponse.data.data)
    )
    expect(mockPush).toHaveBeenCalledWith('/mobile')
  })

  it('管理员登录成功时应该重定向到admin', async () => {
    const mockResponse = {
      data: {
        code: 200,
        data: {
          id: 1,
          username: 'admin',
          nickname: '管理员',
          isAdmin: true,
          token: 'admin-token-123'
        }
      }
    }

    api.login.mockResolvedValue(mockResponse)

    await wrapper.find('input[type="text"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('admin123')
    await wrapper.find('form').trigger('submit.prevent')

    await new Promise(resolve => setTimeout(resolve, 0))

    expect(mockPush).toHaveBeenCalledWith('/admin')
  })

  it('登录失败时应该显示错误信息', async () => {
    const mockResponse = {
      data: {
        code: 401,
        message: '用户名或密码错误'
      }
    }

    api.login.mockResolvedValue(mockResponse)

    await wrapper.find('input[type="text"]').setValue('testuser')
    await wrapper.find('input[type="password"]').setValue('wrongpassword')
    await wrapper.find('form').trigger('submit.prevent')

    await new Promise(resolve => setTimeout(resolve, 0))

    expect(wrapper.vm.error).toBe('用户名或密码错误')
    expect(localStorage.setItem).not.toHaveBeenCalled()
    expect(mockPush).not.toHaveBeenCalled()
  })

  it('loading时按钮应该禁用', async () => {
    const vm = wrapper.vm
    vm.loading = true
    await wrapper.vm.$nextTick()

    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined()
  })
})
