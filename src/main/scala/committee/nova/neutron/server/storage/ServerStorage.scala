package committee.nova.neutron.server.storage

import committee.nova.neutron.api.player.request.ITeleportRequest
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.util.Utilities
import net.minecraft.util.{ChatStyle, EnumChatFormatting}

import scala.collection.mutable

object ServerStorage {
  val teleportRequestSet: mutable.Set[ITeleportRequest] = new mutable.HashSet[ITeleportRequest]()

  def tick(): Unit = for (r <- teleportRequestSet) if (r.tick) {
    val msg = new ChatComponentServerTranslation("msg.neutron.cmd.tp.timeout").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA))
    if (!r.getIgnored) Utilities.getPlayerByUUID(r.getReceiver).foreach(c => c.addChatMessage(msg))
    Utilities.getPlayerByUUID(r.getSender).foreach(c => c.addChatMessage(msg))
    teleportRequestSet.remove(r)
  }

  def addRequest(request: ITeleportRequest): Boolean = {
    teleportRequestSet.foreach(r => if (r.getSender.equals(request.getSender) || (r.getReceiver.equals(request.getSender) && !r.getIgnored)) return false)
    teleportRequestSet.add(request)
  }
}
