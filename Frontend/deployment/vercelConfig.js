const BACKEND_ORIGIN_VARIABLE = 'BACKEND_ORIGIN'

/**
 * Vercel의 외부 Rewrite가 credential이나 하위 경로를 암묵적으로 물려받지 않도록
 * Backend 주소를 HTTPS origin 하나로 제한한다.
 */
export function normalizeBackendOrigin(rawValue) {
  const value = rawValue?.trim()
  if (!value) {
    throw new Error(`${BACKEND_ORIGIN_VARIABLE} is required for Vercel deployment`)
  }

  let url
  try {
    url = new URL(value)
  } catch {
    throw new Error(`${BACKEND_ORIGIN_VARIABLE} must be a valid absolute URL`)
  }

  if (url.protocol !== 'https:') {
    throw new Error(`${BACKEND_ORIGIN_VARIABLE} must use HTTPS`)
  }
  if (url.username || url.password) {
    throw new Error(`${BACKEND_ORIGIN_VARIABLE} must not contain credentials`)
  }
  if (url.pathname !== '/' || url.search || url.hash) {
    throw new Error(`${BACKEND_ORIGIN_VARIABLE} must be an origin without path, query, or fragment`)
  }

  return url.origin
}

/**
 * API Rewrite를 SPA fallback보다 먼저 선언하는 순서는 인증 API 응답이
 * index.html로 바뀌지 않게 하는 배포 계약이므로 테스트로 고정한다.
 */
export function createVercelConfig(environment = process.env) {
  const backendOrigin = normalizeBackendOrigin(environment[BACKEND_ORIGIN_VARIABLE])

  return {
    framework: 'vite',
    buildCommand: 'npm run build',
    outputDirectory: 'dist',
    rewrites: [
      {
        source: '/api/:path*',
        destination: `${backendOrigin}/api/:path*`,
      },
      {
        source: '/(.*)',
        destination: '/index.html',
      },
    ],
  }
}
