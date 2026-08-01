import { MIN_SEARCH_LOADING_MS } from '../config/appConfig'

export function wait(ms) {
  return new Promise((resolve) => window.setTimeout(resolve, ms))
}

export function waitForPaint() {
  return new Promise((resolve) => {
    window.requestAnimationFrame(() => window.requestAnimationFrame(resolve))
  })
}

export async function keepSearchLoadingVisible(startedAt) {
  const remainingMs = MIN_SEARCH_LOADING_MS - (Date.now() - startedAt)
  if (remainingMs > 0) {
    await wait(remainingMs)
  }
}
