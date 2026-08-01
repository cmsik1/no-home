import { SearchForm } from './SearchForm.jsx'
import { MapPanel } from './components/MapPanel.jsx'
import { ResultList } from './components/ResultList.jsx'

export function SearchPage(props) {
  const {
    searchPanelCollapsed,
    setSearchPanelCollapsed,
    hasSearched,
    loading,
    error,
    items,
    selectedItem,
    selectItem,
    pageSummary,
    resultMetaLabel,
    canGoPreviousPage,
    canGoNextPage,
    goPreviousPage,
    goNextPage,
    mapCanvasRef,
    mapReady,
    mapError,
    mapStatusLabel,
    selectedMapItem,
    backToList,
  } = props

  return (
    <main className="workspace" aria-label="주택 실거래가 검색 화면">
      <section className="left-panel" aria-label="검색과 결과">
        <SearchForm {...props} onSubmit={props.searchFirstPage} />
        <ResultList
          hasSearched={hasSearched}
          loading={loading}
          error={error}
          items={items}
          selectedItem={selectedItem}
          selectItem={selectItem}
          searchPanelCollapsed={searchPanelCollapsed}
          setSearchPanelCollapsed={setSearchPanelCollapsed}
          pageSummary={pageSummary}
          resultMetaLabel={resultMetaLabel}
          canGoPreviousPage={canGoPreviousPage}
          canGoNextPage={canGoNextPage}
          goPreviousPage={goPreviousPage}
          goNextPage={goNextPage}
          mapError={mapError}
          mapReady={mapReady}
          mapStatusLabel={mapStatusLabel}
        />
      </section>

      <MapPanel
        mapCanvasRef={mapCanvasRef}
        mapReady={mapReady}
        mapError={mapError}
        mapStatusLabel={mapStatusLabel}
        selectedItem={selectedMapItem}
        backToList={backToList}
      />
    </main>
  )
}
