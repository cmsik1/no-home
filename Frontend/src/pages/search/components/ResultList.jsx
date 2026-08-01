import {
  displayAddress,
  displayAptName,
  displayArea,
  displayBuildYear,
  displayDealAmount,
  displayDealDate,
  displayDealType,
  displayFloor,
  displayRegion,
  fieldText,
  itemKey,
} from '../../../utils/houseDisplay'

export function ResultList({ hasSearched, loading, error, items, selectedItem, selectItem, searchPanelCollapsed, setSearchPanelCollapsed, pageSummary, resultMetaLabel, canGoPreviousPage, canGoNextPage, goPreviousPage, goNextPage, mapError, mapReady, mapStatusLabel }) {
  return (
    <div className="list-panel" aria-live="polite">
      {hasSearched && (
        <button
          className={`list-filter-toggle${searchPanelCollapsed ? ' is-collapsed' : ''}`}
          type="button"
          aria-expanded={!searchPanelCollapsed}
          aria-controls="search-panel-body"
          aria-label={searchPanelCollapsed ? '검색 조건 펼치기' : '검색 조건 접기'}
          title={searchPanelCollapsed ? '검색 조건 펼치기' : '검색 조건 접기'}
          onClick={() => setSearchPanelCollapsed(!searchPanelCollapsed)}
        >
          <span className="filter-handle-icon" aria-hidden="true"><span></span><span></span><span></span></span>
        </button>
      )}
      <div className="result-toolbar">
        <div><strong>{pageSummary}</strong><span>{resultMetaLabel}</span></div>
        <div className="pagination-controls">
          <button className="secondary-button compact-button" type="button" disabled={!canGoPreviousPage} onClick={goPreviousPage}>이전</button>
          <button className="secondary-button compact-button" type="button" disabled={!canGoNextPage} onClick={goNextPage}>다음</button>
        </div>
      </div>
      {(mapError || !mapReady) && (
        <div className={`map-inline-status${mapError ? ' is-error' : ''}`}>
          <span>지도</span>
          <strong>{mapStatusLabel}</strong>
        </div>
      )}
      {loading ? (
        <div className="state-box loading-state"><strong>검색 중입니다.</strong><span>조건에 맞는 실거래가 목록을 불러오고 있습니다.</span><span className="loading-dots" aria-hidden="true"><i></i><i></i><i></i></span></div>
      ) : error ? (
        <div className="state-box danger-state"><strong>검색에 실패했습니다.</strong><span>{error}</span></div>
      ) : hasSearched && items.length === 0 ? (
        <div className="state-box"><strong>검색 결과가 없습니다.</strong><span>지역명, 아파트명, 거래월 조건을 조정해 다시 검색해 주세요.</span></div>
      ) : !hasSearched ? (
        <div className="state-box"><strong>검색 조건을 입력해 주세요.</strong><span>검색을 실행하면 목록과 지도에 결과가 표시됩니다.</span></div>
      ) : (
        <div className="result-list">
          {items.map((item) => {
            const selected = itemKey(selectedItem) === itemKey(item)
            return (
              <button key={itemKey(item)} className={`result-item${selected ? ' is-selected' : ''}`} type="button" onClick={() => selectItem(item)}>
                <span className="item-main">
                  <span className="item-title"><span className="deal-type-badge">{displayDealType(item)}</span>{displayAptName(item)}</span>
                  <strong>{displayDealAmount(item)}</strong>
                </span>
                <span className="item-address">{displayRegion(item)} {displayAddress(item)}</span>
                <span className="item-date">{displayDealDate(item)}</span>
                <span className="item-meta">
                  <span>{displayArea(item)}</span>
                  <span>{displayFloor(item)}</span>
                  <span>{displayBuildYear(item)}</span>
                  <span>{fieldText(item?.umdNm)}</span>
                </span>
              </button>
            )
          })}
        </div>
      )}
    </div>
  )
}
