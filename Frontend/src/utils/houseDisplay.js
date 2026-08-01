const windows1252Bytes = {
  0x20AC: 0x80,
  0x201A: 0x82,
  0x0192: 0x83,
  0x201E: 0x84,
  0x2026: 0x85,
  0x2020: 0x86,
  0x2021: 0x87,
  0x02C6: 0x88,
  0x2030: 0x89,
  0x0160: 0x8A,
  0x2039: 0x8B,
  0x0152: 0x8C,
  0x017D: 0x8E,
  0x2018: 0x91,
  0x2019: 0x92,
  0x201C: 0x93,
  0x201D: 0x94,
  0x2022: 0x95,
  0x2013: 0x96,
  0x2014: 0x97,
  0x02DC: 0x98,
  0x2122: 0x99,
  0x0161: 0x9A,
  0x203A: 0x9B,
  0x0153: 0x9C,
  0x017E: 0x9E,
  0x0178: 0x9F,
}

function looksLikeMojibake(value) {
  for (const char of value) {
    const code = char.charCodeAt(0)
    if ((code >= 0x80 && code <= 0xff) || windows1252Bytes[code]) return true
  }
  return false
}

export function repairMojibake(value) {
  if (!looksLikeMojibake(value) || /[\uAC00-\uD7A3]/.test(value) || typeof TextDecoder === 'undefined') {
    return value
  }

  const bytes = []
  for (const char of value) {
    const code = char.charCodeAt(0)
    if (code <= 0xff) {
      bytes.push(code)
    } else if (windows1252Bytes[code]) {
      bytes.push(windows1252Bytes[code])
    } else {
      return value
    }
  }

  try {
    return new TextDecoder('utf-8', { fatal: true }).decode(new Uint8Array(bytes))
  } catch {
    return value
  }
}

export function fieldText(value, fallback = '-') {
  if (value === null || value === undefined || value === '') return fallback
  return repairMojibake(String(value))
}

export function displayKoreanPrice(value) {
  const numeric = Number(value)
  if (!Number.isFinite(numeric)) return '-'

  const manwon = Math.trunc(numeric)
  if (manwon < 10000) return `${manwon.toLocaleString()}만`

  const eok = Math.floor(manwon / 10000)
  const remainder = manwon % 10000
  return [`${eok.toLocaleString()}억`, remainder > 0 ? `${remainder.toLocaleString()}만` : ''].filter(Boolean).join(' ')
}

export function displayManwon(value) {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? displayKoreanPrice(numeric) : '-'
}

export function itemKey(item) {
  return item?.resultKey ?? item?.apiRowHash ?? item?.dealId ?? `${item?.houseId ?? 'house'}-${item?.dealDate ?? 'date'}-${item?.floor ?? 'floor'}`
}

export function displayAptName(item) {
  return fieldText(item?.aptNm, '아파트명 없음')
}

export function displayAddress(item) {
  if (item?.roadnm) return fieldText(item.roadnm)
  return [fieldText(item?.umdNm, ''), fieldText(item?.jibun, '')].filter(Boolean).join(' ') || '-'
}

export function displayDealType(item) {
  if (item?.dealType === 'jeonse') return '전세'
  if (item?.dealType === 'monthly') return '월세'
  return '매매'
}

export function displayDealAmount(item) {
  if (item?.dealType === 'jeonse' || item?.dealType === 'monthly') {
    const deposit = displayManwon(item?.depositManwon ?? String(item?.deposit ?? '').replace(/,/g, ''))
    if (item?.dealType === 'monthly') return `보증금 ${deposit} / 월세 ${displayManwon(item?.monthlyRentManwon ?? item?.monthlyRent)}`
    return `보증금 ${deposit}`
  }
  return displayKoreanPrice(Number(item?.dealAmountManwon ?? String(item?.dealAmount ?? '').replace(/,/g, '')))
}

export function displayArea(item) {
  return item?.excluUseAr ? `${item.excluUseAr}㎡` : '-'
}

export function displayFloor(item) {
  return item?.floor || item?.floor === 0 ? `${item.floor}층` : '-'
}

export function displayDealDate(item) {
  if (item?.dealDate) return item.dealDate
  if (item?.dealYmd && item.dealYmd.length === 6) return `${item.dealYmd.slice(0, 4)}-${item.dealYmd.slice(4, 6)}`
  return '-'
}

export function displayBuildYear(item) {
  return item?.buildYear ? `${item.buildYear}년식` : '-'
}

export function displayRegion(item) {
  return [fieldText(item?.sido, ''), fieldText(item?.sigungu, '')].filter(Boolean).join(' ') || '-'
}

export function mapAddress(item) {
  if (item?.roadnm) {
    return [fieldText(item?.sido, ''), fieldText(item?.sigungu, ''), fieldText(item?.roadnm, '')].filter(Boolean).join(' ')
  }
  return [fieldText(item?.sido, ''), fieldText(item?.sigungu, ''), fieldText(item?.umdNm, ''), fieldText(item?.jibun, '')]
    .filter(Boolean)
    .join(' ')
}
