export function buildSearchPageProps(controller) {
  const { search, map, memberAccount, interests, seoulDistricts, dealModeOptions, mapStatusLabel } = controller
  return {
    filters: search.filters,
    updateFilter: search.updateFilter,
    updateFilters: search.updateFilters,
    handleDealModeChange: search.handleDealModeChange,
    searchFirstPage: (event) => {
      event.preventDefault()
      search.searchHouses(1)
    },
    resetSearch: search.resetSearch,
    seoulDistricts,
    dealModeOptions,
    sortOptions: search.activeSortOptions,
    resultDisplayMode: search.resultDisplayMode,
    setResultDisplayMode: search.setResultDisplayMode,
    visibleCountLabel: search.visibleCountLabel,
    activeFilterSummary: search.activeFilterSummary,
    regionError: search.regionError,
    dealMonthError: search.dealMonthError,
    legalDongs: search.legalDongs,
    legalDongLoading: search.legalDongLoading,
    legalDongError: search.legalDongError,
    selectedLawdCd: search.selectedLawdCd,
    loadLegalDongs: search.loadLegalDongs,
    priceFilterVisible: search.priceFilterVisible,
    priceRange: search.priceRange,
    priceRangeAvailable: search.priceRangeAvailable,
    monthlyRentRangeAvailable: search.monthlyRentRangeAvailable,
    activeMinPriceKey: search.activeMinPriceKey,
    activeMaxPriceKey: search.activeMaxPriceKey,
    priceRangeLoading: search.priceRangeLoading,
    priceRangeError: search.priceRangeError,
    loadPriceRange: search.loadPriceRange,
    member: memberAccount.member,
    interestRegions: interests.interestRegions,
    canSaveInterestRegion: interests.canSaveInterestRegion,
    interestRegionLoading: interests.interestRegionLoading,
    interestRegionMessage: interests.interestRegionMessage,
    interestRegionError: interests.interestRegionError,
    saveInterestRegion: interests.saveInterestRegion,
    applyInterestRegion: interests.applyInterestRegion,
    deleteInterestRegion: interests.deleteInterestRegion,
    searchPanelCollapsed: search.searchPanelCollapsed,
    setSearchPanelCollapsed: search.setSearchPanelCollapsed,
    hasSearched: search.hasSearched,
    loading: search.loading,
    error: search.error,
    items: search.items,
    selectedItem: search.selectedItem,
    selectItem: search.setSelectedItem,
    pageSummary: search.pageSummary,
    resultMetaLabel: search.resultMetaLabel,
    canGoPreviousPage: search.canGoPreviousPage,
    canGoNextPage: search.canGoNextPage,
    goPreviousPage: () => search.canGoPreviousPage && search.searchHouses(search.searchPage - 1),
    goNextPage: () => search.canGoNextPage && search.searchHouses(search.searchPage + 1),
    mapCanvasRef: map.mapCanvasRef,
    mapReady: map.mapReady,
    mapError: map.mapError,
    mapStatusLabel,
    selectedMapItem: search.selectedItem,
    backToList: () => search.setSelectedItem(null),
  }
}

export function buildAccountPageProps(controller) {
  const { setActivePage, memberAccount, loginMember, logoutMember } = controller
  return {
    member: memberAccount.member,
    accountMode: memberAccount.accountMode,
    setAccountMode: memberAccount.setAccountMode,
    memberLoading: memberAccount.memberLoading,
    memberMessage: memberAccount.memberMessage,
    memberError: memberAccount.memberError,
    loginForm: memberAccount.loginForm,
    setLoginForm: memberAccount.setLoginForm,
    signupForm: memberAccount.signupForm,
    setSignupForm: memberAccount.setSignupForm,
    passwordResetForm: memberAccount.passwordResetForm,
    setPasswordResetForm: memberAccount.setPasswordResetForm,
    profileForm: memberAccount.profileForm,
    setProfileForm: memberAccount.setProfileForm,
    profileEditing: memberAccount.profileEditing,
    setProfileEditing: memberAccount.setProfileEditing,
    deleteConfirm: memberAccount.deleteConfirm,
    setDeleteConfirm: memberAccount.setDeleteConfirm,
    onClose: () => setActivePage('search'),
    loginMember,
    signupMember: memberAccount.signupMember,
    resetPassword: memberAccount.resetPassword,
    logoutMember,
    updateMember: memberAccount.updateMember,
    deleteMember: memberAccount.deleteMember,
  }
}

export function buildMemberSearchPageProps(controller) {
  const { setActivePage, memberAccount, isNoticeAdmin } = controller
  return {
    memberLoading: memberAccount.memberLoading,
    memberMessage: memberAccount.memberMessage,
    memberError: memberAccount.memberError,
    keyword: memberAccount.memberSearchKeyword,
    setKeyword: memberAccount.setMemberSearchKeyword,
    results: memberAccount.memberSearchResults,
    onSearch: () => memberAccount.searchMembers(isNoticeAdmin),
    onClear: () => {
      memberAccount.setMemberSearchKeyword('')
      memberAccount.setMemberSearchResults([])
    },
    onBack: () => setActivePage('search'),
  }
}

export function buildNoticePageProps(controller) {
  const { notices, isNoticeAdmin } = controller
  return {
    notices: notices.notices,
    noticeLoading: notices.noticeLoading,
    noticeMessage: notices.noticeMessage,
    noticeError: notices.noticeError,
    isNoticeAdmin,
    noticeEditingId: notices.noticeEditingId,
    noticeForm: notices.noticeForm,
    setNoticeForm: notices.setNoticeForm,
    saveNotice: notices.saveNotice,
    resetNoticeForm: notices.resetNoticeForm,
    editNotice: notices.editNotice,
    deleteNotice: notices.deleteNotice,
    displayNoticeDate: notices.displayNoticeDate,
  }
}
