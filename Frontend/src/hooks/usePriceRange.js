import { useCallback, useState } from 'react'
import { fetchPriceRangeResults } from '../services/houseService'

const emptyPriceRange = () => ({ min: null, max: null, monthlyMin: null, monthlyMax: null })

export function usePriceRange({ filters, updateFilters, validateRegionForSearch }) {
  const [priceRange, setPriceRange] = useState(emptyPriceRange)
  const [priceRangeLoading, setPriceRangeLoading] = useState(false)
  const [priceRangeError, setPriceRangeError] = useState('')

  const resetPriceRange = useCallback(() => {
    setPriceRange(emptyPriceRange())
    setPriceRangeError('')
  }, [])

  const setPriceRangeFromResults = useCallback((results, { resetSelection = false, sourceFilters = filters } = {}) => {
    const minField = sourceFilters.dealMode === 'sale' ? 'minDealAmountManwon' : 'minDepositManwon'
    const maxField = sourceFilters.dealMode === 'sale' ? 'maxDealAmountManwon' : 'maxDepositManwon'
    const mins = results.map((payload) => Number(payload?.[minField])).filter(Number.isFinite)
    const maxes = results.map((payload) => Number(payload?.[maxField])).filter(Number.isFinite)
    if (!mins.length || !maxes.length) return false

    const nextRange = { min: Math.min(...mins), max: Math.max(...maxes), monthlyMin: null, monthlyMax: null }
    const patch = {}
    const minKey = sourceFilters.dealMode === 'sale' ? 'minPrice' : 'minDeposit'
    const maxKey = sourceFilters.dealMode === 'sale' ? 'maxPrice' : 'maxDeposit'
    if (resetSelection || sourceFilters[minKey] === '') patch[minKey] = nextRange.min
    if (resetSelection || sourceFilters[maxKey] === '') patch[maxKey] = nextRange.max

    if (sourceFilters.dealMode === 'monthly') {
      const monthlyMins = results.map((payload) => Number(payload?.minMonthlyRentManwon)).filter(Number.isFinite)
      const monthlyMaxes = results.map((payload) => Number(payload?.maxMonthlyRentManwon)).filter(Number.isFinite)
      if (monthlyMins.length && monthlyMaxes.length) {
        nextRange.monthlyMin = Math.min(...monthlyMins)
        nextRange.monthlyMax = Math.max(...monthlyMaxes)
        if (resetSelection || sourceFilters.minMonthlyRent === '') patch.minMonthlyRent = nextRange.monthlyMin
        if (resetSelection || sourceFilters.maxMonthlyRent === '') patch.maxMonthlyRent = nextRange.monthlyMax
      }
    }

    setPriceRange(nextRange)
    if (Object.keys(patch).length) updateFilters(patch)
    setPriceRangeError('')
    return true
  }, [filters, updateFilters])

  const loadPriceRange = useCallback(async () => {
    if (!validateRegionForSearch() || priceRangeLoading) return
    setPriceRangeLoading(true)
    setPriceRangeError('')
    try {
      const results = await fetchPriceRangeResults(filters)
      if (!setPriceRangeFromResults(results, { resetSelection: true })) {
        setPriceRangeError('조건에 맞는 가격 범위가 없습니다.')
      }
    } catch (exception) {
      setPriceRangeError(exception instanceof Error ? exception.message : '가격 범위를 불러오지 못했습니다.')
    } finally {
      setPriceRangeLoading(false)
    }
  }, [filters, priceRangeLoading, setPriceRangeFromResults, validateRegionForSearch])

  return { priceRange, priceRangeLoading, priceRangeError, loadPriceRange, resetPriceRange, setPriceRangeFromResults }
}
