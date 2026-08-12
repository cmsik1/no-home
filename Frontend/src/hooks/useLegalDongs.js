import { useCallback, useRef, useState } from 'react'
import { fetchLegalDongs } from '../services/regionService'

/** 선택한 자치구 코드를 법정동 선택 목록으로 바꾸며, 최신 요청의 결과만 화면에 반영한다. */
export function useLegalDongs(selectedLawdCd) {
  const [legalDongs, setLegalDongs] = useState([])
  const [legalDongLoading, setLegalDongLoading] = useState(false)
  const [legalDongError, setLegalDongError] = useState('')
  const requestIdRef = useRef(0)

  const loadLegalDongs = useCallback(async (lawdCd = selectedLawdCd) => {
    const requestId = requestIdRef.current + 1
    requestIdRef.current = requestId
    setLegalDongError('')
    setLegalDongs([])
    if (!lawdCd) {
      setLegalDongLoading(false)
      return
    }
    setLegalDongLoading(true)
    try {
      const nextDongs = await fetchLegalDongs(lawdCd)
      if (requestId !== requestIdRef.current) return
      setLegalDongs(nextDongs)
    } catch (exception) {
      if (requestId !== requestIdRef.current) return
      setLegalDongs([])
      setLegalDongError(exception instanceof Error ? exception.message : '법정동 목록을 불러오지 못했습니다.')
    } finally {
      if (requestId === requestIdRef.current) setLegalDongLoading(false)
    }
  }, [selectedLawdCd])

  const resetLegalDongs = useCallback(() => {
    requestIdRef.current += 1
    setLegalDongs([])
    setLegalDongLoading(false)
    setLegalDongError('')
  }, [])

  return {
    legalDongs,
    legalDongLoading,
    legalDongError,
    loadLegalDongs,
    resetLegalDongs,
  }
}
