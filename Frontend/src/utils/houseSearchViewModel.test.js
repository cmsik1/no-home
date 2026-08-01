import test from 'node:test'
import assert from 'node:assert/strict'
import { emptyFilters } from '../houseSearchParams.js'
import { createHouseSearchViewModel } from './houseSearchViewModel.js'

test('creates pagination and readable labels from search state', () => {
  const filters = { ...emptyFilters(), sido: '서울특별시', sigungu: '동작구', dealMode: 'sale', sort: 'latest' }
  const viewModel = createHouseSearchViewModel({ filters, priceRange: { min: 1, max: 2 }, resultDisplayMode: '10', totalCount: 21, items: Array(10), searchPage: 2, loading: false, hasSearched: true })
  assert.equal(viewModel.totalPages, 3)
  assert.equal(viewModel.canGoPreviousPage, true)
  assert.equal(viewModel.canGoNextPage, true)
  assert.equal(viewModel.visibleCountLabel, '21건')
  assert.ok(viewModel.activeFilterSummary.includes('서울특별시 동작구'))
})
