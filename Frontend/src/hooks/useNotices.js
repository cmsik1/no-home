import { useState } from 'react'
import { requestJson } from '../services/apiClient'
import { fieldText } from '../utils/houseDisplay'

export function useNotices({ isNoticeAdmin }) {
  const [notices, setNotices] = useState([])
  const [noticeLoading, setNoticeLoading] = useState(false)
  const [noticeMessage, setNoticeMessage] = useState('')
  const [noticeError, setNoticeError] = useState('')
  const [noticeEditingId, setNoticeEditingId] = useState(null)
  const [noticeForm, setNoticeForm] = useState({ title: '', content: '' })

  const loadNotices = async ({ silent = false } = {}) => {
    setNoticeLoading(true)
    if (!silent) setNoticeError('')
    try {
      const nextNotices = await requestJson('/api/notices?limit=10')
      setNotices(Array.isArray(nextNotices) ? nextNotices : [])
    } catch (exception) {
      setNotices([])
      if (!silent) setNoticeError(exception instanceof Error ? exception.message : '공지사항을 불러오지 못했습니다.')
    } finally {
      setNoticeLoading(false)
    }
  }

  const resetNoticeForm = () => {
    setNoticeEditingId(null)
    setNoticeForm({ title: '', content: '' })
  }

  const saveNotice = async () => {
    if (!isNoticeAdmin) {
      setNoticeError('관리자만 공지사항을 작성할 수 있습니다.')
      return
    }
    setNoticeLoading(true)
    setNoticeError('')
    setNoticeMessage('')
    try {
      await requestJson(noticeEditingId ? `/api/notices/${noticeEditingId}` : '/api/notices', {
        method: noticeEditingId ? 'PUT' : 'POST',
        body: JSON.stringify(noticeForm),
      })
      setNoticeMessage(noticeEditingId ? '공지사항을 수정했습니다.' : '공지사항을 등록했습니다.')
      resetNoticeForm()
      await loadNotices()
    } catch (exception) {
      setNoticeError(exception instanceof Error ? exception.message : '공지사항 저장에 실패했습니다.')
    } finally {
      setNoticeLoading(false)
    }
  }

  const deleteNotice = async (notice) => {
    if (!notice?.noticeId) return
    setNoticeLoading(true)
    setNoticeError('')
    setNoticeMessage('')
    try {
      await requestJson(`/api/notices/${notice.noticeId}`, { method: 'DELETE' })
      if (noticeEditingId === notice.noticeId) resetNoticeForm()
      setNoticeMessage('공지사항을 삭제했습니다.')
      await loadNotices()
    } catch (exception) {
      setNoticeError(exception instanceof Error ? exception.message : '공지사항 삭제에 실패했습니다.')
    } finally {
      setNoticeLoading(false)
    }
  }

  const editNotice = (notice) => {
    setNoticeEditingId(notice.noticeId)
    setNoticeForm({ title: notice.title || '', content: notice.content || '' })
    setNoticeMessage('')
    setNoticeError('')
  }

  const displayNoticeDate = (notice) => {
    const value = notice?.updatedAt || notice?.createdAt
    if (!value) return '-'
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return fieldText(value)
    return new Intl.DateTimeFormat('ko-KR', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).format(date)
  }

  return {
    notices,
    noticeLoading,
    noticeMessage,
    noticeError,
    noticeEditingId,
    noticeForm,
    setNoticeForm,
    loadNotices,
    resetNoticeForm,
    saveNotice,
    deleteNotice,
    editNotice,
    displayNoticeDate,
  }
}
