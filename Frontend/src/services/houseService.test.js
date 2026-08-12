import assert from 'node:assert/strict'
import test from 'node:test'

globalThis.window = {
  setTimeout,
  clearTimeout,
}

const { fetchHousePriceRange, fetchHouseSearch } = await import('./houseService.js')

function jsonResponse(body, { ok = true, status = 200 } = {}) {
  return {
    ok,
    status,
    json: async () => body,
  }
}

test('fetchHouseSearch serializes non-empty fields and unwraps ApiResponse data', async () => {
  let requestedUrl = ''
  globalThis.fetch = async (url) => {
    requestedUrl = String(url)
    return jsonResponse({ success: true, data: { items: [{ aptName: '테스트 아파트' }], totalCount: 1 } })
  }

  const result = await fetchHouseSearch({ lawdCd: '11680', dealYmd: '202607', aptName: '', autoImport: 'false' })

  assert.equal(requestedUrl, '/api/houses/search?lawdCd=11680&dealYmd=202607&autoImport=false')
  assert.equal(result.totalCount, 1)
})

test('fetchHousePriceRange uses the price-range endpoint and preserves server errors', async () => {
  let requestedUrl = ''
  globalThis.fetch = async (url) => {
    requestedUrl = String(url)
    return jsonResponse({ success: false, message: '가격 조건 오류' }, { ok: false, status: 400 })
  }

  await assert.rejects(
    fetchHousePriceRange({ lawdCd: '11680', dealMode: 'monthly' }),
    /가격 조건 오류/,
  )
  assert.equal(requestedUrl, '/api/houses/price-range?lawdCd=11680&dealMode=monthly')
})
