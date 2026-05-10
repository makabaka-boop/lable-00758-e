import axios from 'axios'
import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('axios', () => {
  const mockAxiosInstance = {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() }
    }
  }
  const mockCreate = vi.fn(() => mockAxiosInstance)
  return {
    default: {
      create: mockCreate,
      ...mockAxiosInstance
    },
    create: mockCreate
  }
})

describe('API Module', () => {
  let api
  let mockAxiosInstance

  beforeEach(async () => {
    vi.resetModules()
    localStorage.clear()
    const axiosModule = await import('axios')
    mockAxiosInstance = axiosModule.default.create()
  })

  describe('axios instance configuration', () => {
    it('should create axios instance with correct baseURL', async () => {
      await import('../api')
      expect(axios.create).toHaveBeenCalledWith({
        baseURL: '/api',
        timeout: 30000
      })
    })
  })

  describe('request interceptor - token attachment', () => {
    it('should add Authorization header when user exists in localStorage', async () => {
      localStorage.setItem('user', JSON.stringify({ token: 'test-token-123' }))

      await import('../api')

      const requestInterceptor = axios.create().interceptors.request.use
      expect(requestInterceptor).toHaveBeenCalled()

      const config = { headers: {} }
      const callback = requestInterceptor.mock.calls[0][0]
      const result = callback(config)

      expect(result.headers.Authorization).toBe('Bearer test-token-123')
    })

    it('should not add Authorization header when no user in localStorage', async () => {
      await import('../api')

      const requestInterceptor = axios.create().interceptors.request.use
      const config = { headers: {} }
      const callback = requestInterceptor.mock.calls[0][0]
      const result = callback(config)

      expect(result.headers.Authorization).toBeUndefined()
    })

    it('should not add Authorization header when user has no token', async () => {
      localStorage.setItem('user', JSON.stringify({ id: 1 }))

      await import('../api')

      const requestInterceptor = axios.create().interceptors.request.use
      const config = { headers: {} }
      const callback = requestInterceptor.mock.calls[0][0]
      const result = callback(config)

      expect(result.headers.Authorization).toBeUndefined()
    })
  })

  describe('response interceptor - 401 handling', () => {
    it('should register response interceptor', async () => {
      await import('../api')

      const responseInterceptor = axios.create().interceptors.response.use
      expect(responseInterceptor).toHaveBeenCalled()
    })

    it('should handle 401 error by removing user and redirecting', async () => {
      Object.defineProperty(window, 'location', {
        value: { href: '' },
        writable: true
      })

      await import('../api')

      const responseInterceptor = axios.create().interceptors.response.use
      const errorCallback = responseInterceptor.mock.calls[0][1]

      const error = {
        response: { status: 401 }
      }

      localStorage.setItem('user', 'some-data')

      await expect(errorCallback(error)).rejects.toEqual(error)
      expect(localStorage.getItem('user')).toBeNull()
      expect(window.location.href).toBe('/login')
    })

    it('should pass through non-401 errors', async () => {
      await import('../api')

      const responseInterceptor = axios.create().interceptors.response.use
      const errorCallback = responseInterceptor.mock.calls[0][1]

      const error = {
        response: { status: 500, data: { message: 'Server Error' } }
      }

      await expect(errorCallback(error)).rejects.toEqual(error)
    })

    it('should handle error without response', async () => {
      await import('../api')

      const responseInterceptor = axios.create().interceptors.response.use
      const errorCallback = responseInterceptor.mock.calls[0][1]

      const error = new Error('Network Error')

      await expect(errorCallback(error)).rejects.toThrow('Network Error')
    })
  })
})
