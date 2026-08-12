import { useEffect, useRef, useState } from 'react'
import { seoulDistricts } from '../houseSearchParams'
import { DEAL_MODE_OPTIONS, NOTICE_ADMIN_EMAILS } from '../config/appConfig'
import { useAgentCommands } from './useAgentCommands'
import { useHouseSearch } from './useHouseSearch'
import { useInterestRegions } from './useInterestRegions'
import { useKakaoHouseMap } from './useKakaoHouseMap'
import { useMemberAccount } from './useMemberAccount'
import { useNotices } from './useNotices'
import { fieldText } from '../utils/houseDisplay'

/**
 * 검색·지도·회원·공지·관심지역·AI hook을 하나의 화면 컨트롤러로 조립한다.
 * 하위 hook의 상태를 페이지용 파생 값과 이동 동작으로 묶어 AppRoutes에 제공한다.
 */
export function useAppController() {
  const [activePage, setActivePage] = useState('search')
  const mapControlsRef = useRef({
    clearMapMarkers: () => {},
    refreshMapMarkers: () => {},
  })

  // 검색 완료 시 지도 갱신이 필요하지만 지도 hook도 검색 결과를 입력으로 받으므로,
  // ref 기반 중계 함수를 두어 두 hook 사이의 초기화 순환 의존을 끊는다.
  const search = useHouseSearch({
    clearMapMarkers: (...args) => mapControlsRef.current.clearMapMarkers(...args),
    refreshMapMarkers: (...args) => mapControlsRef.current.refreshMapMarkers(...args),
  })

  const map = useKakaoHouseMap({
    activePage,
    hasSearched: search.hasSearched,
    items: search.items,
    selectedItem: search.selectedItem,
    onSelectItem: search.setSelectedItem,
  })
  mapControlsRef.current = {
    clearMapMarkers: map.clearMapMarkers,
    refreshMapMarkers: map.refreshMapMarkers,
  }

  const memberAccount = useMemberAccount({ setActivePage })
  const isNoticeAdmin = Boolean(memberAccount.member?.email && NOTICE_ADMIN_EMAILS.includes(String(memberAccount.member.email).trim().toLowerCase()))
  const notices = useNotices({ isNoticeAdmin })
  const interests = useInterestRegions({
    member: memberAccount.member,
    selectedLawdCd: search.selectedLawdCd,
    filters: search.filters,
    updateFilters: search.updateFilters,
    loadLegalDongs: search.loadLegalDongs,
    cancelSearch: search.cancelSearch,
  })
  const agent = useAgentCommands({
    filters: search.filters,
    setFilters: search.setFilters,
    loadLegalDongs: search.loadLegalDongs,
    searchHouses: search.searchHouses,
    resetSearch: search.resetSearch,
    setSearchPanelCollapsed: search.setSearchPanelCollapsed,
    hasSearched: search.hasSearched,
    resultDisplayMode: search.resultDisplayMode,
    searchPage: search.searchPage,
    totalPages: search.totalPages,
    items: search.items,
    selectItem: search.setSelectedItem,
    focusMapItem: map.focusMapItem,
  })

  useEffect(() => {
    memberAccount.loadCurrentMember()
  }, [memberAccount.loadCurrentMember])

  const accountSummary = memberAccount.member
    ? `${fieldText(memberAccount.member.name, memberAccount.member.email)} · ${memberAccount.member.email}`
    : '로그인한 회원 정보가 없습니다'
  const markerCountLabel = search.hasSearched ? `${map.markerDisplayCount.toLocaleString()} / ${search.items.length.toLocaleString()}개 표시` : '검색 전'
  const mapStatusLabel = map.mapError || (map.mapLoading ? '지도를 불러오는 중' : search.hasSearched ? `${markerCountLabel} · ${map.mapStatus}` : map.mapStatus)

  const openNoticePage = async () => {
    setActivePage('notice')
    await notices.loadNotices({ silent: true })
  }

  const openAccountPanel = (mode = memberAccount.member ? 'profile' : 'login') => {
    memberAccount.setAccountMode(mode)
    setActivePage('account')
    memberAccount.setProfileEditing(false)
    memberAccount.setMemberMessage('')
    memberAccount.setMemberError('')
    if (mode === 'profile') memberAccount.loadCurrentMember({ silent: false })
  }

  const openMemberSearchPage = () => {
    if (!isNoticeAdmin) {
      memberAccount.setMemberError('관리자만 회원 검색을 사용할 수 있습니다.')
      return
    }
    setActivePage('member-search')
    memberAccount.setMemberError('')
    memberAccount.setMemberMessage('')
  }

  const loginMember = async () => {
    await memberAccount.loginMember()
    if (activePage === 'notice') await notices.loadNotices({ silent: true })
  }

  const logoutMember = async () => {
    await memberAccount.logoutMember()
    notices.resetNoticeForm()
  }

  return {
    activePage,
    setActivePage,
    search,
    map,
    memberAccount,
    notices,
    interests,
    agent,
    isNoticeAdmin,
    accountSummary,
    mapStatusLabel,
    seoulDistricts,
    dealModeOptions: DEAL_MODE_OPTIONS,
    openNoticePage,
    openAccountPanel,
    openMemberSearchPage,
    loginMember,
    logoutMember,
  }
}
