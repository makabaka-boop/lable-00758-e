import { vi } from 'vitest'

Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
})

Storage.prototype.getItem = vi.fn()
Storage.prototype.setItem = vi.fn()
Storage.prototype.removeItem = vi.fn()
Storage.prototype.clear = vi.fn()

if (window.HTMLMediaElement) {
  Object.defineProperty(window.HTMLMediaElement.prototype, 'play', {
    writable: true,
    value: vi.fn(() => Promise.resolve())
  })
  Object.defineProperty(window.HTMLMediaElement.prototype, 'pause', {
    writable: true,
    value: vi.fn()
  })
}
