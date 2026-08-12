import { useEffect, useMemo, useRef, useState } from 'react'
import { clampPanelSize, loadPanelSize, savePanelSize } from '../utils/chatPanel'

const VIEWPORT_MARGIN = 40

/** 포인터 이동량을 패널 크기로 변환하고 화면 경계로 제한한 뒤 마지막 크기를 저장한다. */
export function useResizableChatPanel() {
  const initialSize = useMemo(() => loadPanelSize(), [])
  const [size, setSize] = useState(initialSize)
  const resizeStateRef = useRef(null)
  const sizeRef = useRef(initialSize)

  useEffect(() => { sizeRef.current = size }, [size])

  useEffect(() => {
    function resize(event) {
      const state = resizeStateRef.current
      if (!state) return
      if (event.cancelable) event.preventDefault()
      const point = event.touches ? event.touches[0] : event
      setSize(clampPanelSize(
        state.startWidth + state.startX - point.clientX,
        state.startHeight + state.startY - point.clientY,
        window.innerWidth - VIEWPORT_MARGIN,
        window.innerHeight - VIEWPORT_MARGIN,
      ))
    }
    function stopResize() {
      if (!resizeStateRef.current) return
      resizeStateRef.current = null
      document.body.style.userSelect = ''
      savePanelSize(sizeRef.current.width, sizeRef.current.height)
    }
    document.addEventListener('mousemove', resize)
    document.addEventListener('mouseup', stopResize)
    document.addEventListener('touchmove', resize, { passive: false })
    document.addEventListener('touchend', stopResize)
    return () => {
      document.removeEventListener('mousemove', resize)
      document.removeEventListener('mouseup', stopResize)
      document.removeEventListener('touchmove', resize)
      document.removeEventListener('touchend', stopResize)
      document.body.style.userSelect = ''
    }
  }, [])

  function startResize(event) {
    const point = event.touches ? event.touches[0] : event
    resizeStateRef.current = {
      startX: point.clientX,
      startY: point.clientY,
      startWidth: size.width,
      startHeight: size.height,
    }
    document.body.style.userSelect = 'none'
    event.preventDefault()
  }

  return { panelStyle: { width: `${size.width}px`, height: `${size.height}px` }, startResize }
}
