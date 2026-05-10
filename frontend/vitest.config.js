import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'jsdom',
    testTimeout: 10000,
    include: ['src/**/*.test.js', 'src/**/*.spec.js'],
    setupFiles: ['./src/test/setup.js'],
    coverage: {
      include: ['src/api/**/*.js', 'src/router/**/*.js', 'src/views/**/*.vue'],
      exclude: ['src/main.js', 'src/App.vue', 'src/style.css'],
      reporter: ['text', 'html'],
      reportsDirectory: './coverage'
    }
  }
})
