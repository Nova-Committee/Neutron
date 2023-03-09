package committee.nova.neutron.api.player.storage

trait IBooleanStatus {
  def isApplied: Boolean

  def setApplied(applied: Boolean): Unit

  def getNote: String

  def setNote(note: String): Unit
}
