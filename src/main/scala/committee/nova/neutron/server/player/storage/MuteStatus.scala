package committee.nova.neutron.server.player.storage

import committee.nova.neutron.api.player.storage.IBooleanStatus
import committee.nova.neutron.util.Utilities

class MuteStatus extends IBooleanStatus {
  private var muted = false
  private var note = ""
  private var byConsole = false

  def this(muted: Boolean) = {
    this
    this.muted = muted
  }

  def this(muted: Boolean, note: String) = {
    this(muted)
    this.note = note
  }

  def this(muted: Boolean, note: String, byConsole: Boolean) = {
    this(muted, note)
    this.byConsole = byConsole
  }

  override def isApplied: Boolean = muted

  override def getNote: String = if (note.isEmpty) Utilities.L10n.getFromCurrentLang("phr.neutron.unknown") else note

  override def setApplied(muted: Boolean): Unit = this.muted = muted

  override def setNote(note: String): Unit = this.note = note

  def isExecutedByConsole: Boolean = byConsole

  def setExecutedByConsole(byConsole: Boolean): Unit = this.byConsole = byConsole
}
