import assert from 'node:assert/strict'
import test from 'node:test'

import { createVercelConfig, normalizeBackendOrigin } from './vercelConfig.js'

test('requires BACKEND_ORIGIN without exposing its value', () => {
  assert.throws(
    () => createVercelConfig({}),
    /BACKEND_ORIGIN is required/,
  )
})

test('rejects malformed and unsafe backend origins', () => {
  const invalidValues = [
    'not-a-url',
    'http://backend.example.com',
    'https://user:password@backend.example.com',
    'https://backend.example.com/api',
    'https://backend.example.com?source=vercel',
    'https://backend.example.com#fragment',
  ]

  for (const value of invalidValues) {
    assert.throws(() => normalizeBackendOrigin(value), /BACKEND_ORIGIN/)
  }
})

test('normalizes a valid HTTPS origin', () => {
  assert.equal(
    normalizeBackendOrigin('  https://backend.example.com/  '),
    'https://backend.example.com',
  )
})

test('places the API rewrite before the SPA fallback', () => {
  const config = createVercelConfig({
    BACKEND_ORIGIN: 'https://backend.example.com',
  })

  assert.equal(config.framework, 'vite')
  assert.equal(config.buildCommand, 'npm run build')
  assert.equal(config.outputDirectory, 'dist')
  assert.deepEqual(config.rewrites, [
    {
      source: '/api/:path*',
      destination: 'https://backend.example.com/api/:path*',
    },
    {
      source: '/(.*)',
      destination: '/index.html',
    },
  ])
})
