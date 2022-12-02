package committee.nova.neutron.api.player.request

import java.util.UUID

trait ITeleportRequest {
  def getId: UUID

  def tick: Boolean

  def apply: Boolean

  def setIgnored(): Unit

  def wasIgnored: Boolean

  def getSender: UUID

  def getReceiver: UUID

  def getInfo: String
}
