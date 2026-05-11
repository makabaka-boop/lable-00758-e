import { describe, it, expect } from 'vitest'
import router from '../index'

describe('Router 配置测试', () => {
  it('应该配置正确的路由数量', () => {
    expect(router.options.routes).toHaveLength(4)
  })

  it('根路径应该重定向到 /mobile', () => {
    const rootRoute = router.options.routes.find(r => r.path === '/')
    expect(rootRoute).toBeDefined()
    expect(rootRoute.redirect).toBe('/mobile')
  })

  it('应该有 /mobile 路由', () => {
    const mobileRoute = router.options.routes.find(r => r.path === '/mobile')
    expect(mobileRoute).toBeDefined()
    expect(typeof mobileRoute.component).toBe('function')
  })

  it('应该有 /login 路由', () => {
    const loginRoute = router.options.routes.find(r => r.path === '/login')
    expect(loginRoute).toBeDefined()
    expect(typeof loginRoute.component).toBe('function')
  })

  it('应该有 /admin 路由', () => {
    const adminRoute = router.options.routes.find(r => r.path === '/admin')
    expect(adminRoute).toBeDefined()
    expect(typeof adminRoute.component).toBe('function')
  })

  it('应该使用 history 模式', () => {
    expect(router.options.history).toBeDefined()
  })
})
