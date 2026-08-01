import { fieldText } from '../../../utils/houseDisplay'

export function InterestRegions({ regions, loading, message, error, canSave, onSave, onApply, onDelete }) {
  return (
    <section className="interest-panel">
      <div className="interest-header">
        <strong>관심 지역</strong>
        <button className="secondary-button compact-button" type="button" disabled={!canSave || loading} onClick={onSave}>저장</button>
      </div>
      {message && <p className="account-message">{message}</p>}
      {error && <p className="account-message is-error">{error}</p>}
      {regions.length > 0 && (
        <ul className="interest-list">
          {regions.map((region) => (
            <li key={region.interestRegionId}>
              <button type="button" onClick={() => onApply(region)}>{[region.sido, region.sigungu, region.umdNm].filter(Boolean).map((value) => fieldText(value)).join(' ')}</button>
              <button className="danger-button compact-button" type="button" disabled={loading} onClick={() => onDelete(region)}>삭제</button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
