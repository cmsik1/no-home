import { DangerZone } from './DangerZone.jsx'

export function ProfilePanel(props) {
  const {
    member,
    memberLoading,
    profileForm,
    setProfileForm,
    profileEditing,
    setProfileEditing,
    logoutMember,
    updateMember,
    deleteConfirm,
    setDeleteConfirm,
    deleteMember,
  } = props

  return (
    <div className="profile-layout">
      <dl className="detail-list">
        <div><dt>Email</dt><dd>{member.email}</dd></div>
        <div><dt>Name</dt><dd>{member.name}</dd></div>
        <div><dt>Phone</dt><dd>{member.phone || '-'}</dd></div>
      </dl>
      {!profileEditing ? (
        <div className="actions">
          <button className="primary-button" type="button" disabled={memberLoading} onClick={() => { setProfileForm({ name: member?.name || '', phone: member?.phone || '' }); setProfileEditing(true) }}>Edit</button>
          <button className="secondary-button" type="button" disabled={memberLoading} onClick={logoutMember}>Logout</button>
        </div>
      ) : (
        <form className="account-form" onSubmit={(event) => { event.preventDefault(); updateMember() }}>
          <label><span>Name</span><input value={profileForm.name} type="text" required onChange={(event) => setProfileForm({ ...profileForm, name: event.target.value.trim() })} /></label>
          <label><span>Phone</span><input value={profileForm.phone} type="tel" onChange={(event) => setProfileForm({ ...profileForm, phone: event.target.value.trim() })} /></label>
          <div className="actions"><button className="primary-button" type="submit" disabled={memberLoading}>Save</button><button className="secondary-button" type="button" disabled={memberLoading} onClick={() => setProfileEditing(false)}>Cancel</button></div>
        </form>
      )}
      <DangerZone
        deleteConfirm={deleteConfirm}
        setDeleteConfirm={setDeleteConfirm}
        memberLoading={memberLoading}
        deleteMember={deleteMember}
      />
    </div>
  )
}
