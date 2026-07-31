import test from 'node:test'
import assert from 'node:assert/strict'

import { resolvePaginateTarget, resolveItemTarget } from './agentActions.js'

const searched = { hasSearched: true, displayMode: '10', currentPage: 2, totalPages: 5 }

test('resolvePaginateTarget: absolute page within range', () => {
  const r = resolvePaginateTarget({ page: 3 }, searched)
  assert.deepEqual(r, { ok: true, targetPage: 3 })
})

test('resolvePaginateTarget: direction next/prev relative to currentPage', () => {
  assert.deepEqual(resolvePaginateTarget({ direction: 'next' }, searched), { ok: true, targetPage: 3 })
  assert.deepEqual(resolvePaginateTarget({ direction: 'prev' }, searched), { ok: true, targetPage: 1 })
})

test('resolvePaginateTarget: next at last page is rejected with a friendly message', () => {
  const r = resolvePaginateTarget({ direction: 'next' }, { ...searched, currentPage: 5 })
  assert.equal(r.ok, false)
  assert.match(r.message, /해당 페이지는 없어요/)
})

test('resolvePaginateTarget: prev at first page reports first page', () => {
  const r = resolvePaginateTarget({ direction: 'prev' }, { ...searched, currentPage: 1 })
  assert.equal(r.ok, false)
  assert.equal(r.message, '첫 페이지예요.')
})

test('resolvePaginateTarget: absolute page beyond totalPages is rejected', () => {
  const r = resolvePaginateTarget({ page: 99 }, searched)
  assert.equal(r.ok, false)
  assert.match(r.message, /전체 5페이지/)
})

test('resolvePaginateTarget: same page is a no-op message', () => {
  const r = resolvePaginateTarget({ page: 2 }, searched)
  assert.equal(r.ok, false)
  assert.match(r.message, /이미 2페이지/)
})

test('resolvePaginateTarget: requires a prior search', () => {
  const r = resolvePaginateTarget({ direction: 'next' }, { ...searched, hasSearched: false })
  assert.equal(r.ok, false)
  assert.match(r.message, /먼저 검색/)
})

test('resolvePaginateTarget: no page nor direction asks for clarification', () => {
  const r = resolvePaginateTarget({}, searched)
  assert.equal(r.ok, false)
  assert.match(r.message, /몇 페이지로/)
})

test('resolvePaginateTarget: "all" display mode has no pagination', () => {
  const r = resolvePaginateTarget({ direction: 'next' }, { ...searched, displayMode: 'all' })
  assert.equal(r.ok, false)
  assert.match(r.message, /전체 보기/)
})

test('resolveItemTarget: converts 1-based itemIndex to 0-based index', () => {
  assert.deepEqual(resolveItemTarget(1, 10), { ok: true, index: 0 })
  assert.deepEqual(resolveItemTarget(3, 10), { ok: true, index: 2 })
})

test('resolveItemTarget: out-of-range index reports the current count', () => {
  const r = resolveItemTarget(11, 10)
  assert.equal(r.ok, false)
  assert.match(r.message, /11번째 매물이 없어요. \(현재 10건\)/)
})

test('resolveItemTarget: index below 1 is rejected', () => {
  assert.equal(resolveItemTarget(0, 10).ok, false)
})

test('resolveItemTarget: empty result list prompts a search', () => {
  const r = resolveItemTarget(1, 0)
  assert.equal(r.ok, false)
  assert.match(r.message, /표시할 매물이 없어요/)
})
