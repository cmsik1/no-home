import { isSeoul, seoulLawdCodes } from '../../../houseSearchParams'

export function RegionFields({
  filters,
  updateFilter,
  updateFilters,
  seoulDistricts,
  legalDongs,
  legalDongLoading,
  selectedLawdCd,
  loadLegalDongs,
}) {
  const legalDongDisabled = !selectedLawdCd || legalDongLoading || legalDongs.length === 0

  return (
    <>
      <label>
        <span>시도</span>
        <select
          value={filters.sido}
          onChange={(event) => {
            const sido = event.target.value
            updateFilters({ sido, sigungu: isSeoul(sido) ? filters.sigungu : '', umdNm: '', sort: sido ? filters.sort : 'latest' })
            loadLegalDongs(isSeoul(sido) ? selectedLawdCd : '')
          }}
        >
          <option value="">전체</option>
          <option value="서울특별시">서울특별시</option>
        </select>
      </label>
      <label>
        <span>시군구</span>
        <select
          value={filters.sigungu}
          disabled={!isSeoul(filters.sido)}
          onChange={(event) => {
            const sigungu = event.target.value
            updateFilters({ sigungu, umdNm: '' })
            loadLegalDongs(seoulLawdCodes[sigungu] || '')
          }}
        >
          <option value="">선택</option>
          {seoulDistricts.map((district) => <option key={district} value={district}>{district}</option>)}
        </select>
      </label>
      <label>
        <span>읍면동</span>
        <select value={filters.umdNm} disabled={legalDongDisabled} onChange={(event) => updateFilter('umdNm', event.target.value)}>
          <option value="">{legalDongLoading ? '불러오는 중' : '전체'}</option>
          {legalDongs.map((dong) => <option key={dong.value} value={dong.value}>{dong.label}</option>)}
        </select>
      </label>
    </>
  )
}
