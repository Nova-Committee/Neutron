package committee.nova.neutron.api.player.request

import java.util.UUID

trait ITeleportRequest {
  def tick: Boolean

  def apply: Boolean

  def setIgnored(): Unit

  def getIgnored: Boolean

  def getSender: UUID

  def getReceiver: UUID

  def getInfo: String
}
