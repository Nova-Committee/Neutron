package committee.nova.neutron.api.player.request

import net.minecraft.entity.player.EntityPlayerMP

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

  def isRelevantTo(player: EntityPlayerMP): Boolean = Array(getSender, getReceiver).contains(player.getUniqueID)
}
