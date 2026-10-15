import react from '@vitejs/plugin-react'
import { isIP } from 'node:net'
import { defineConfig, loadEnv } from 'vite'

const testApiTarget = process.env.CRERAL_TEST_API_TARGET

function validarApiInternet(valor) {
  const recusar = () => {
    throw new Error('Build internet recusado. Informe VITE_API_URL HTTPS pública terminando em /api, sem credenciais, query ou fragmento.')
  }
  let api
  try { api = new URL(valor) } catch { recusar() }
  if (typeof valor !== 'string' || valor !== valor.trim() || api.protocol !== 'https:'
      || !api.hostname || !valor.endsWith('/api') || api.username || api.password
      || valor.includes('?') || valor.includes('#')) recusar()

  const host = api.hostname.replace(/^\[|\]$/g, '').replace(/\.$/, '').toLowerCase()
  const ipv4 = isIP(host) === 4 ? host.split('.').map(Number) : null
  const loopback = host === 'localhost' || host === '::1' || ipv4?.[0] === 127
  const privado = loopback || host.endsWith('.localhost') || host.endsWith('.local') || host.endsWith('.localdomain') || host.endsWith('.lan')
    || host.endsWith('.internal') || !host.includes('.') && isIP(host) === 0
    || ipv4 && (ipv4[0] === 0 || ipv4[0] === 10 || ipv4[0] === 169 && ipv4[1] === 254
      || ipv4[0] === 172 && ipv4[1] >= 16 && ipv4[1] <= 31 || ipv4[0] === 192 && ipv4[1] === 168
      || ipv4[0] === 100 && ipv4[1] >= 64 && ipv4[1] <= 127 || ipv4[0] >= 224)
    || isIP(host) === 6 && (host === '::' || /^(fc|fd|fe[89ab])/.test(host) || host.startsWith('::ffff:'))
  // Exceção apenas do processo de build da fixture HTTPS; não é uma variável VITE nem entra no bundle.
  if (privado && !(process.env.CRERAL_TEST_INTERNET === 'true' && loopback)) recusar()
}

// https://vite.dev/config/
export default defineConfig(({ command, mode }) => {
  const env = loadEnv(mode, process.cwd(), 'VITE_')
  if (command === 'build' && env.VITE_MODO_INTERNET === 'true') validarApiInternet(env.VITE_API_URL)
  return {
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
  }
})
