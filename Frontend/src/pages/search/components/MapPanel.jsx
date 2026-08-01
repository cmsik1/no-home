import {
  displayAddress,
  displayAptName,
  displayArea,
  displayDealAmount,
  displayDealDate,
  displayFloor,
  displayRegion,
} from '../../../utils/houseDisplay'

export function MapPanel({ mapCanvasRef, mapReady, mapError, mapStatusLabel, selectedItem, backToList }) {
  return (
    <aside className="map-panel" aria-label="지도 패널">
      <div className="map-surface">
        <div ref={mapCanvasRef} className="map-canvas" aria-label="Kakao 지도"></div>
        {!mapReady && <div className="map-grid" aria-hidden="true"></div>}
        {(!mapReady || mapError) && (
          <div className={`map-overlay-state${mapError ? ' is-error' : ''}`}>
            <strong>{mapError ? '지도를 사용할 수 없습니다' : '지도 준비 중'}</strong>
            <span>{mapStatusLabel}</span>
          </div>
        )}
      </div>
      {selectedItem && (
        <div className="map-detail-card">
          <button className="map-detail-close" type="button" aria-label="상세 닫기" onClick={backToList}>x</button>
          <strong>{displayAptName(selectedItem)}</strong>
          <span className="map-detail-price">{displayDealAmount(selectedItem)}</span>
          <span>{displayRegion(selectedItem)} {displayAddress(selectedItem)}</span>
          <span>{displayArea(selectedItem)} · {displayFloor(selectedItem)} · {displayDealDate(selectedItem)}</span>
        </div>
      )}
    </aside>
  )
}
