import {
  KAKAO_MAP_API_KEY,
  KAKAO_MAP_SDK_ERROR_MESSAGE,
} from '../config/appConfig'

let kakaoMapsSdkPromise = null

/**
 * Kakao Maps 스크립트를 페이지당 한 번만 삽입하고 모든 호출자가 같은 Promise를 공유하게 한다.
 * autoload=false로 받은 뒤 services 라이브러리까지 준비된 kakao 객체만 반환한다.
 */
export function loadKakaoMapsSdk() {
  if (!KAKAO_MAP_API_KEY) {
    return Promise.reject(new Error('VITE_KAKAO_MAP_API_KEY가 설정되지 않았습니다.'))
  }
  if (typeof window === 'undefined') {
    return Promise.reject(new Error('브라우저 환경에서만 지도를 불러올 수 있습니다.'))
  }
  if (window.kakao?.maps?.services) {
    return Promise.resolve(window.kakao)
  }
  if (kakaoMapsSdkPromise) {
    return kakaoMapsSdkPromise
  }

  kakaoMapsSdkPromise = new Promise((resolve, reject) => {
    const existingScript = document.querySelector('script[data-kakao-map-sdk="true"]')
    const finishLoad = () => {
      if (!window.kakao?.maps) {
        reject(new Error(KAKAO_MAP_SDK_ERROR_MESSAGE))
        return
      }
      window.kakao.maps.load(() => resolve(window.kakao))
    }

    if (existingScript) {
      existingScript.addEventListener('load', finishLoad, { once: true })
      existingScript.addEventListener('error', () => reject(new Error(KAKAO_MAP_SDK_ERROR_MESSAGE)), { once: true })
      return
    }

    const script = document.createElement('script')
    script.dataset.kakaoMapSdk = 'true'
    script.async = true
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${encodeURIComponent(KAKAO_MAP_API_KEY)}&libraries=services&autoload=false`
    script.addEventListener('load', finishLoad, { once: true })
    script.addEventListener('error', () => reject(new Error(KAKAO_MAP_SDK_ERROR_MESSAGE)), { once: true })
    document.head.appendChild(script)
  })

  return kakaoMapsSdkPromise
}
