import { useCallback, useEffect, useMemo, useState } from 'react'
import { createInterestRegion, fetchInterestRegions, removeInterestRegion } from '../services/interestRegionService'
import { fieldText } from '../utils/houseDisplay'

/**
 * 로그인 회원의 관심지역 목록을 API와 동기화한다. 저장된 지역을 적용할 때 검색 필터와
 * 법정동 목록을 함께 교체하고 진행 중인 검색을 취소해 서로 다른 지역 상태가 섞이지 않게 한다.
 */
export function useInterestRegions({ member, selectedLawdCd, filters, updateFilters, loadLegalDongs, cancelSearch }) {
  const [interestRegions, setInterestRegions] = useState([])
  const [interestRegionLoading, setInterestRegionLoading] = useState(false)
  const [interestRegionMessage, setInterestRegionMessage] = useState('')
  const [interestRegionError, setInterestRegionError] = useState('')

  const selectedInterestRegionLabel = useMemo(
    () => [filters.sido, filters.sigungu, filters.umdNm].filter(Boolean).join(' ') || '관심지역',
    [filters.sido, filters.sigungu, filters.umdNm],
  )
  const canSaveInterestRegion = Boolean(member && selectedLawdCd && filters.umdNm && !interestRegionLoading)

  const loadInterestRegions = useCallback(async ({ silent = false } = {}) => {
    if (!member) {
      setInterestRegions([])
      setInterestRegionLoading(false)
      return
    }
    setInterestRegionLoading(true)
    if (!silent) {
      setInterestRegionError('')
      setInterestRegionMessage('')
    }
    try {
      setInterestRegions(await fetchInterestRegions())
    } catch (exception) {
      setInterestRegions([])
      if (!silent) setInterestRegionError(exception instanceof Error ? exception.message : '관심지역을 불러오지 못했습니다.')
    } finally {
      setInterestRegionLoading(false)
    }
  }, [member])

  useEffect(() => {
    if (member) {
      loadInterestRegions({ silent: true })
    } else {
      setInterestRegions([])
      setInterestRegionMessage('')
      setInterestRegionError('')
    }
  }, [member, loadInterestRegions])

  const saveInterestRegion = async () => {
    if (!member) {
      setInterestRegionError('로그인해야 관심지역을 저장할 수 있습니다.')
      return
    }
    if (!selectedLawdCd || !filters.umdNm) {
      setInterestRegionError('시군구와 읍면동을 선택해 주세요.')
      return
    }
    setInterestRegionLoading(true)
    setInterestRegionError('')
    setInterestRegionMessage('')
    try {
      await createInterestRegion({ lawdCd: selectedLawdCd, sido: filters.sido, sigungu: filters.sigungu, umdNm: filters.umdNm })
      setInterestRegionMessage(`${selectedInterestRegionLabel}을 관심지역으로 저장했습니다.`)
      await loadInterestRegions({ silent: true })
    } catch (exception) {
      setInterestRegionError(exception instanceof Error ? exception.message : '관심지역 저장에 실패했습니다.')
    } finally {
      setInterestRegionLoading(false)
    }
  }

  const applyInterestRegion = (region) => {
    if (!region) return
    updateFilters({ sido: region.sido || '', sigungu: region.sigungu || '', umdNm: region.umdNm || '' })
    cancelSearch?.()
    loadLegalDongs(region.lawdCd || selectedLawdCd)
    setInterestRegionMessage(`${[region.sido, region.sigungu, region.umdNm].filter(Boolean).map((value) => fieldText(value)).join(' ')}을 검색 조건에 적용했습니다.`)
    setInterestRegionError('')
  }

  const deleteInterestRegion = async (region) => {
    if (!region?.interestRegionId) return
    setInterestRegionLoading(true)
    setInterestRegionError('')
    setInterestRegionMessage('')
    try {
      await removeInterestRegion(region.interestRegionId)
      setInterestRegionMessage('관심지역을 삭제했습니다.')
      await loadInterestRegions({ silent: true })
    } catch (exception) {
      setInterestRegionError(exception instanceof Error ? exception.message : '관심지역 삭제에 실패했습니다.')
    } finally {
      setInterestRegionLoading(false)
    }
  }

  return {
    interestRegions,
    interestRegionLoading,
    interestRegionMessage,
    interestRegionError,
    canSaveInterestRegion,
    loadInterestRegions,
    saveInterestRegion,
    applyInterestRegion,
    deleteInterestRegion,
  }
}
