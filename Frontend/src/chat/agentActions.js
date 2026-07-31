// 에이전트 '액션' 명령(paginate/mapFocus/selectItem)의 순수 결정 로직.
// App.vue가 라이브 상태를 넘기면 목표 페이지·항목 인덱스와 사용자 안내 문구를 돌려준다.
// DOM/지도/검색 호출은 App.vue가 수행하고, 여기서는 계산·검증만 해서 단위 테스트가 가능하다.

// 페이지 이동 목표를 정한다. 상한/하한은 프론트 라이브 상태로 clamp·검사(백엔드는 하한만 검증).
export const resolvePaginateTarget = (command = {}, state = {}) => {
  const { hasSearched, displayMode, currentPage, totalPages } = state
  if (!hasSearched) {
    return { ok: false, message: '먼저 검색을 해주세요. 예: "강남구 2024년 5월 검색해줘"' }
  }
  if (displayMode === 'all') {
    return { ok: false, message: '전체 보기 상태에서는 페이지 이동이 없어요.' }
  }

  let targetPage
  if (command.page !== undefined && command.page !== null) {
    targetPage = Number(command.page)
  } else if (command.direction === 'next') {
    targetPage = Number(currentPage) + 1
  } else if (command.direction === 'prev') {
    targetPage = Number(currentPage) - 1
  }

  if (!Number.isFinite(targetPage)) {
    return { ok: false, message: '몇 페이지로 갈까요? "다음 페이지"나 "3페이지"처럼 알려주세요.' }
  }
  if (targetPage < 1) {
    return { ok: false, message: '첫 페이지예요.' }
  }
  if (targetPage > totalPages) {
    return { ok: false, message: `해당 페이지는 없어요. (전체 ${totalPages}페이지)` }
  }
  if (targetPage === Number(currentPage)) {
    return { ok: false, message: `이미 ${targetPage}페이지예요.` }
  }
  return { ok: true, targetPage }
}

// 에이전트 계약은 1-based(1=첫 번째)다. 0-based 인덱스로 변환·검증한다.
export const resolveItemTarget = (itemIndex, itemCount) => {
  const count = Number(itemCount) || 0
  if (count === 0) {
    return { ok: false, message: '표시할 매물이 없어요. 먼저 검색해 주세요.' }
  }
  const index = Number(itemIndex) - 1
  if (!Number.isInteger(index) || index < 0 || index >= count) {
    return { ok: false, message: `${itemIndex}번째 매물이 없어요. (현재 ${count}건)` }
  }
  return { ok: true, index }
}
