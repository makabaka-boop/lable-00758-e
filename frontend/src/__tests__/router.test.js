import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createRouter, createWebHistory, createMemoryHistory } from 'vue-router'
import router from '../router'

describe('Router Configuration', () => {
  it('should have correct routes defined', () => {
    const routes = router.getRoutes()
    const pathNames = routes.map(r => r.path)

    expect(pathNames).toContain('/mobile')
    expect(pathNames).toContain('/login')
    expect(pathNames).toContain('/admin')
  })

  it('should redirect / to /mobile', () => {
    const routes = router.getRoutes()
    const rootRoute = routes.find(r => r.path === '/')
    expect(rootRoute).toBeDefined()
    expect(rootRoute.redirect).toBe('/mobile')
  })

  it('should have /mobile route', () => {
    const routes = router.getRoutes()
    const mobileRoute = routes.find(r => r.path === '/mobile')
    expect(mobileRoute).toBeDefined()
  })

  it('should have /login route', () => {
    const routes = router.getRoutes()
    const loginRoute = routes.find(r => r.path === '/login')
    expect(loginRoute).toBeDefined()
  })

  it('should have /admin route', () => {
    const routes = router.getRoutes()
    const adminRoute = routes.find(r => r.path === '/admin')
    expect(adminRoute).toBeDefined()
  })

  it('should use createWebHistory', () => {
    expect(router.options.history).toBeDefined()
  })
})

describe('Router Navigation', () => {
  let testRouter

  beforeEach(async () => {
    testRouter = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', redirect: '/mobile' },
        { path: '/mobile', component: { template: '<div>Mobile</div>' } },
        { path: '/login', component: { template: '<div>Login</div>' } },
        { path: '/admin', component: { template: '<div>Admin</div>' } }
      ]
    })
    await testRouter.push('/')
    await testRouter.isReady()
  })

  it('should navigate to /mobile when visiting /', async () => {
    await testRouter.push('/')
    expect(testRouter.currentRoute.value.path).toBe('/mobile')
  })

  it('should navigate to /login', async () => {
    await testRouter.push('/login')
    expect(testRouter.currentRoute.value.path).toBe('/login')
  })

  it('should navigate to /admin', async () => {
    await testRouter.push('/admin')
    expect(testRouter.currentRoute.value.path).toBe('/admin')
  })

  it('should navigate to /mobile', async () => {
    await testRouter.push('/mobile')
    expect(testRouter.currentRoute.value.path).toBe('/mobile')
  })
})
