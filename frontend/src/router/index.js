import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/mobile' },
  { path: '/mobile', component: () => import('../views/Mobile.vue') },
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/admin', component: () => import('../views/Admin.vue') }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
