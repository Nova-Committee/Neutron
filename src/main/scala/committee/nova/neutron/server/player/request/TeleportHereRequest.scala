package committee.nova.neutron.server.player.request

import committee.nova.neutron.api.player.request.ITeleportRequest
import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.util.Utilities

import java.util.UUID

class TeleportHereRequest(private val sender: UUID, private val receiver: UUID) extends ITeleportRequest {
  private var timeout = 1200
  private var ignored = false

  def this(sender: UUID, receiver: UUID, timeout: Int) = {
    this(sender, receiver)
    this.timeout = timeout
  }

  override def tick: Boolean = {
    timeout -= 1
    timeout <= 0
  }

  override def apply: Boolean = {
    val oS = Utilities.getPlayerByUUID(sender)
    val oR = Utilities.getPlayerByUUID(receiver)
    if (oS.isEmpty || oR.isEmpty) return false
    oR.get.teleportTo(oS.get)
  }

  override def setIgnored(): Unit = ignored = true

  override def getIgnored: Boolean = ignored

  override def getSender: UUID = sender

  override def getReceiver: UUID = receiver

  override def hashCode(): Int = sender.hashCode() + receiver.hashCode()
}
