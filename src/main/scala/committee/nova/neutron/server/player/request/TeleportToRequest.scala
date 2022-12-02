package committee.nova.neutron.server.player.request

import committee.nova.neutron.api.player.request.ITeleportRequest
import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.event.impl.TeleportFromEvent
import committee.nova.neutron.util.Utilities
import net.minecraftforge.common.MinecraftForge

import java.util.UUID

class TeleportToRequest(private val sender: UUID, private val receiver: UUID) extends ITeleportRequest {
  private val id = UUID.randomUUID()
  private var timeout = ServerConfig.getMaxTpExpirationTime
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
    val oS = Utilities.Player.getPlayerByUUID(sender)
    val oR = Utilities.Player.getPlayerByUUID(receiver)
    if (oS.isEmpty || oR.isEmpty) return false
    val s = oS.get
    MinecraftForge.EVENT_BUS.post(TeleportFromEvent(s, s.dimension, s.posX, s.posY, s.posZ))
    s.teleportTo(oR.get)
  }

  override def setIgnored(): Unit = ignored = true

  override def wasIgnored: Boolean = ignored

  override def getSender: UUID = sender

  override def getReceiver: UUID = receiver

  override def getInfo: String = s"${Utilities.Player.getPlayerNameByUUID(getSender)} >> ${Utilities.Player.getPlayerNameByUUID(getReceiver)}"

  override def getId: UUID = id
}
