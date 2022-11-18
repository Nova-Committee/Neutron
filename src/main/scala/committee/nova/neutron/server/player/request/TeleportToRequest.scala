package committee.nova.neutron.server.player.request

import committee.nova.neutron.api.player.INeutronPlayer
import committee.nova.neutron.api.player.request.ITeleportRequest
import committee.nova.neutron.util.Utilities

import java.util.UUID

class TeleportToRequest(private val sender: UUID, private val receiver: UUID) extends ITeleportRequest {
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
    oS.get.asInstanceOf[INeutronPlayer].teleportTo(oR.get)
    //oS.get.teleportTo(oR.get)
  }

  override def setIgnored(): Unit = ignored = true

  override def getIgnored: Boolean = ignored

  override def getSender: UUID = sender

  override def getReceiver: UUID = receiver

  override def equals(obj: Any): Boolean = {
    obj match {
      case t: TeleportToRequest => this.sender == t.sender && this.receiver == t.receiver
      case h: TeleportHereRequest => this.sender == h.getReceiver && this.receiver == h.getSender && this.ignored == h.getIgnored
      case _ => false
    }
  }

  override def hashCode(): Int = sender.hashCode() + receiver.hashCode()
}
