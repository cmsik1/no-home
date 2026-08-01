import { useState } from 'react'
import { applyAgentFilters, isSeoul, seoulLawdCodes } from '../houseSearchParams'
import { resolveItemTarget, resolvePaginateTarget } from '../services/agentActions'
import { displayAptName } from '../utils/houseDisplay'

export function useAgentCommands({
  filters,
  setFilters,
  loadLegalDongs,
  searchHouses,
  resetSearch,
  setSearchPanelCollapsed,
  hasSearched,
  resultDisplayMode,
  searchPage,
  totalPages,
  items,
  selectItem,
  focusMapItem,
}) {
  const [agentResult, setAgentResult] = useState(null)
  const [agentSeq, setAgentSeq] = useState(0)

  const reportAgent = (text) => {
    setAgentSeq((seq) => {
      const nextSeq = seq + 1
      setAgentResult({ text, seq: nextSeq })
      return nextSeq
    })
  }

  const handleAgentCommand = async (command) => {
    try {
      if (!command || !command.action) {
        reportAgent('명령을 이해하지 못했습니다. 예: 강남구 2024년 5월 매매 검색')
        return
      }
      if (command.action === 'clarify') {
        reportAgent(command.clarify || command.summary || '조건을 조금 더 알려 주세요.')
        return
      }
      if (command.action === 'reset') {
        resetSearch()
        reportAgent('검색 조건을 초기화했습니다.')
        return
      }
      if (command.action === 'search' || command.action === 'setFilters') {
        const nextFilters = { ...filters }
        const { applied, ignored } = applyAgentFilters(nextFilters, command.filters || {})
        if (applied.sigungu && nextFilters.sigungu && !isSeoul(nextFilters.sido)) nextFilters.sido = '서울특별시'
        if (('sido' in applied || 'sigungu' in applied) && !('umdNm' in applied)) nextFilters.umdNm = ''
        if (!nextFilters.sido) nextFilters.sort = 'latest'
        setFilters(nextFilters)
        if ('sido' in applied || 'sigungu' in applied) loadLegalDongs(seoulLawdCodes[nextFilters.sigungu] || '')
        if (command.action === 'search') await searchHouses(1, nextFilters)
        else setSearchPanelCollapsed(false)
        const condition = [applied.sigungu || applied.sido, applied.umdNm, applied.aptName, applied.startDealMonth || applied.endDealMonth]
          .filter(Boolean)
          .join(' · ') || '입력한 조건'
        reportAgent(`${condition}으로 ${command.action === 'search' ? '검색했습니다' : '검색 조건을 적용했습니다'}.${ignored?.length ? ` (${ignored.join(', ')} 조건은 지원하지 않아 제외했습니다.)` : ''}`)
        return
      }
      if (command.action === 'paginate') {
        const decision = resolvePaginateTarget(command, { hasSearched, displayMode: resultDisplayMode, currentPage: searchPage, totalPages })
        if (!decision.ok) {
          reportAgent(decision.message)
          return
        }
        await searchHouses(decision.targetPage)
        reportAgent(`${decision.targetPage}페이지로 이동했습니다.`)
        return
      }
      if (command.action === 'mapFocus' || command.action === 'selectItem') {
        const decision = resolveItemTarget(command.itemIndex, items.length)
        if (!decision.ok) {
          reportAgent(decision.message)
          return
        }
        const item = items[decision.index]
        if (command.action === 'selectItem') {
          selectItem(item)
          reportAgent(`${command.itemIndex}번째 매물(${displayAptName(item)})을 선택했습니다.`)
        } else {
          focusMapItem(item)
          reportAgent(`지도에서 ${command.itemIndex}번째 매물로 이동했습니다.`)
        }
        return
      }
      reportAgent('지원하지 않는 명령입니다. 검색, 페이지 이동, 매물 선택처럼 말해 주세요.')
    } catch {
      reportAgent('명령 처리 중 오류가 발생했습니다. 조건을 다시 확인해 주세요.')
    }
  }

  return { agentResult, handleAgentCommand }
}
