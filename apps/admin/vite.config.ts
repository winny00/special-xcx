import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5174,
    proxy: {
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/special': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/system': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/resource': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
