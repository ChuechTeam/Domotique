import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    // Enable all incoming hosts since Vert.x uses the local ip address (127.0.0.1) and not localhost.
    host: true,
    hmr: {
      // Specify a different port for HMR since Vert.x doesn't proxy WebSockets properly for some reason.
      port: 5180
    }
  }
})
