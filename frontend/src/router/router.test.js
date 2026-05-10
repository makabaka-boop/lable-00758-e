import { describe, it, expect, beforeEach } from 'vitest'
import { createRouter, createMemoryHistory } from 'vue-router'
import routerConfig from './index'

describe('路由配置', () => {
  let router

  beforeEach(() => {
    router = createRouter({
      history: createMemoryHistory(),
      routes: routerConfig.options.routes
    })
  })

  describe('路由路径配置', () => {
    it('包含根路径重定向到 /mobile', () => {
      const rootRoute = router.options.routes.find(r => r.path === '/')
      expect(rootRoute).toBeDefined()
      expect(rootRoute.redirect).toBe('/mobile')
    })

    it('包含 /mobile 路由', () => {
      const mobileRoute = router.options.routes.find(r => r.path === '/mobile')
      expect(mobileRoute).toBeDefined()
      expect(mobileRoute.component).toBeDefined()
    })

    it('包含 /login 路由', () => {
      const loginRoute = router.options.routes.find(r => r.path === '/login')
      expect(loginRoute).toBeDefined()
      expect(loginRoute.component).toBeDefined()
    })

    it('包含 /admin 路由', () => {
      const adminRoute = router.options.routes.find(r => r.path === '/admin')
      expect(adminRoute).toBeDefined()
      expect(adminRoute.component).toBeDefined()
    })

    it('路由数量为 4', () => {
      expect(router.options.routes.length).toBe(4)
    })
  })

  describe('使用 createWebHistory', () => {
    it('路由配置存在 history 对象', () => {
      expect(routerConfig.options.history).toBeDefined()
      expect(typeof routerConfig.options.history).toBe('object')
    })
  })
})
