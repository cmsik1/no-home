export function DangerZone({ deleteConfirm, setDeleteConfirm, memberLoading, deleteMember }) {
  return (
    <div className="danger-zone">
      <strong>Delete Account</strong>
      <p>Deleting the account ends the current session and removes the member profile.</p>
      <label><span>Confirm Text</span><input value={deleteConfirm} type="text" placeholder="DELETE" onChange={(event) => setDeleteConfirm(event.target.value)} /></label>
      <button className="danger-button" type="button" disabled={memberLoading} onClick={deleteMember}>Delete Account</button>
    </div>
  )
}
