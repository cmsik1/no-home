import { useCallback, useEffect, useRef, useState } from 'react'
import {
  DEFAULT_MAP_CENTER,
  KAKAO_MAP_API_KEY,
  SELECTED_MARKER_IMAGE_URL,
} from '../config/appConfig'
import { loadKakaoMapsSdk } from '../services/kakaoMapSdk'
import { itemKey, mapAddress } from '../utils/houseDisplay'

export function useKakaoHouseMap({ activePage, hasSearched, items, selectedItem, onSelectItem }) {
  const [kakao, setKakao] = useState(null)
  const [mapReady, setMapReady] = useState(false)
  const [mapLoading, setMapLoading] = useState(false)
  const [mapStatus, setMapStatus] = useState('지도 API 설정을 확인하고 있습니다.')
  const [mapError, setMapError] = useState('')
  const [markerDisplayCount, setMarkerDisplayCount] = useState(0)

  const mapCanvasRef = useRef(null)
  const mapRef = useRef(null)
  const mapMarkersRef = useRef([])
  const mapMarkerItemsRef = useRef([])
  const geocodedMapItemsRef = useRef([])
  const defaultMarkerImageRef = useRef(null)
  const selectedMarkerImageRef = useRef(null)

  const clearMapMarkers = useCallback(() => {
    mapMarkersRef.current.forEach((marker) => marker.setMap(null))
    mapMarkersRef.current = []
    mapMarkerItemsRef.current = []
    geocodedMapItemsRef.current = []
    setMarkerDisplayCount(0)
    if (hasSearched && mapReady) {
      setMapStatus('검색 결과를 지도에 표시할 준비가 되었습니다.')
    }
  }, [hasSearched, mapReady])

  const ensureMap = useCallback((force = false) => {
    if (!kakao || !mapCanvasRef.current) return
    if (force || !mapRef.current || !mapCanvasRef.current.children.length) {
      mapCanvasRef.current.innerHTML = ''
      mapRef.current = null
    }
    if (mapRef.current) return
    const center = new kakao.maps.LatLng(DEFAULT_MAP_CENTER.lat, DEFAULT_MAP_CENTER.lng)
    mapRef.current = new kakao.maps.Map(mapCanvasRef.current, { center, level: 6 })
    mapRef.current.relayout?.()
  }, [kakao])

  const focusMapItem = useCallback((item) => {
    if (!mapRef.current || !kakao) return
    const index = mapMarkerItemsRef.current.findIndex((markerItem) => itemKey(markerItem) === itemKey(item))
    if (index < 0) return
    const marker = mapMarkersRef.current[index]
    mapRef.current.setCenter(marker.getPosition())
    mapRef.current.setLevel(Math.min(mapRef.current.getLevel(), 4))
  }, [kakao])

  const updateSelectedMarker = useCallback((selected = selectedItem) => {
    if (!kakao || !selectedMarkerImageRef.current || !mapMarkersRef.current.length) return
    const selectedKey = selected ? itemKey(selected) : ''
    mapMarkersRef.current.forEach((marker, index) => {
      const isSelected = selectedKey && itemKey(mapMarkerItemsRef.current[index]) === selectedKey
      marker.setImage(isSelected ? selectedMarkerImageRef.current : defaultMarkerImageRef.current)
      marker.setZIndex(isSelected ? 10 : 1)
    })
  }, [kakao, selectedItem])

  const geocodeItem = useCallback((geocoder, item) => {
    const address = mapAddress(item)
    if (!address) return Promise.resolve(null)
    return new Promise((resolve) => {
      geocoder.addressSearch(address, (result, status) => {
        if (status !== kakao.maps.services.Status.OK || !result?.[0]) {
          resolve(null)
          return
        }
        resolve({ item, position: new kakao.maps.LatLng(Number(result[0].y), Number(result[0].x)) })
      })
    })
  }, [kakao])

  const renderMapMarkers = useCallback((successfulItems = geocodedMapItemsRef.current) => {
    if (!mapReady || !kakao || !mapCanvasRef.current) return
    mapMarkersRef.current.forEach((marker) => marker.setMap(null))
    mapMarkersRef.current = []
    mapMarkerItemsRef.current = []
    ensureMap(true)
    if (!mapRef.current) return

    if (!selectedMarkerImageRef.current) {
      selectedMarkerImageRef.current = new kakao.maps.MarkerImage(
        SELECTED_MARKER_IMAGE_URL,
        new kakao.maps.Size(42, 52),
        { offset: new kakao.maps.Point(21, 52) },
      )
    }

    const bounds = new kakao.maps.LatLngBounds()
    successfulItems.forEach(({ item, position }) => {
      const marker = new kakao.maps.Marker({ map: mapRef.current, position })
      if (!defaultMarkerImageRef.current && typeof marker.getImage === 'function') {
        defaultMarkerImageRef.current = marker.getImage()
      }
      kakao.maps.event.addListener(marker, 'click', () => onSelectItem(item))
      bounds.extend(position)
      mapMarkersRef.current.push(marker)
      mapMarkerItemsRef.current.push(item)
    })
    updateSelectedMarker()
    mapRef.current.relayout?.()
    if (mapMarkersRef.current.length > 1) {
      mapRef.current.setBounds(bounds)
    } else if (mapMarkersRef.current.length === 1) {
      mapRef.current.setCenter(mapMarkersRef.current[0].getPosition())
      mapRef.current.setLevel(4)
    }
  }, [ensureMap, kakao, mapReady, onSelectItem, updateSelectedMarker])

  const refreshMapMarkers = useCallback(async (nextItems = items) => {
    clearMapMarkers()
    if (!nextItems.length) return
    if (!mapReady || !kakao) {
      if (!KAKAO_MAP_API_KEY) setMapStatus('VITE_KAKAO_MAP_API_KEY가 설정되지 않아 지도를 표시할 수 없습니다.')
      return
    }

    setMapLoading(true)
    setMapError('')
    setMapStatus('검색 결과 주소를 좌표로 변환하고 있습니다.')
    try {
      const geocoder = new kakao.maps.services.Geocoder()
      const geocodedItems = await Promise.all(nextItems.map((item) => geocodeItem(geocoder, item)))
      const successfulItems = geocodedItems.filter(Boolean)
      geocodedMapItemsRef.current = successfulItems
      setMarkerDisplayCount(successfulItems.length)
      setMapStatus(successfulItems.length
        ? `검색 결과 ${successfulItems.length}개를 지도에 표시했습니다.`
        : '검색 결과에서 지도에 표시할 수 있는 주소를 찾지 못했습니다.')
      setMapLoading(false)
      window.requestAnimationFrame(() => renderMapMarkers(successfulItems))
    } catch (exception) {
      setMapError(exception instanceof Error ? exception.message : '지도 마커 표시 중 오류가 발생했습니다.')
      setMapLoading(false)
    }
  }, [clearMapMarkers, geocodeItem, items, kakao, mapReady, renderMapMarkers])

  useEffect(() => {
    setMapLoading(true)
    loadKakaoMapsSdk()
      .then((loadedKakao) => {
        setKakao(loadedKakao)
        setMapReady(true)
        setMapStatus('검색 결과를 지도에 표시할 수 있습니다.')
      })
      .catch((exception) => {
        setMapReady(false)
        setMapError(exception instanceof Error ? exception.message : 'Kakao Map을 불러오지 못했습니다.')
        setMapStatus('지도 설정을 확인해 주세요.')
      })
      .finally(() => setMapLoading(false))
  }, [])

  useEffect(() => {
    if (activePage === 'search' && mapReady) {
      window.requestAnimationFrame(() => {
        ensureMap(true)
        refreshMapMarkers()
      })
    }
  }, [activePage, ensureMap, mapReady, refreshMapMarkers])

  useEffect(() => {
    if (!selectedItem) {
      updateSelectedMarker(null)
      return
    }
    window.requestAnimationFrame(() => {
      updateSelectedMarker(selectedItem)
      focusMapItem(selectedItem)
    })
  }, [focusMapItem, selectedItem, updateSelectedMarker])

  return {
    mapCanvasRef,
    mapReady,
    mapLoading,
    mapStatus,
    mapError,
    markerDisplayCount,
    clearMapMarkers,
    focusMapItem,
    refreshMapMarkers,
  }
}
