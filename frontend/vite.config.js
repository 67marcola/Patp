import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

const testApiTarget = process.env.CRERAL_TEST_API_TARGET

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  ...(testApiTarget ? {
    server: {
      proxy: {
        '/api': {
          target: testApiTarget,
          changeOrigin: true,
          configure(proxy) {
            // Apenas o preview isolado usa 4173; o CORS normal do Spring permanece intacto.
            proxy.on('proxyReq', (proxyReq, request) => {
              if (request.headers.origin === 'http://localhost:4173') {
                proxyReq.setHeader('Origin', 'http://localhost:5173')
              }
            })
          },
        },
      },
    },
  } : {}),
})
