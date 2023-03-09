package committee.nova.neutron.server.storage

import committee.nova.neutron.api.player.request.ITeleportRequest
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.util.Utilities
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.text.{Style, TextFormatting}
import net.minecraftforge.fml.server.FMLServerHandler

import java.util.UUID
import scala.collection.mutable

object ServerStorage {
  val teleportRequestSet: mutable.LinkedHashSet[ITeleportRequest] = new mutable.LinkedHashSet[ITeleportRequest]()
  val uuid2Name: mutable.HashMap[UUID, String] = new mutable.HashMap[UUID, String]()

  def tick(): Unit = {
    val server = FMLServerHandler.instance().getServer
    for (r <- teleportRequestSet) if (r.tick) {
      val msg = new ChatComponentServerTranslation("msg.neutron.cmd.tp.timeout", r.getInfo).setStyle(new Style().setColor(TextFormatting.DARK_AQUA))
      if (!r.wasIgnored) Utilities.Player.getPlayerByUUID(server, r.getReceiver).foreach(c => c.sendMessage(msg))
      Utilities.Player.getPlayerByUUID(server, r.getSender).foreach(c => c.sendMessage(msg))
      teleportRequestSet.remove(r)
    }
  }

  def addRequest(request: ITeleportRequest): Boolean = {
    teleportRequestSet.foreach(r => if (r.getSender.equals(request.getSender) || (r.getReceiver.equals(request.getSender) && !r.wasIgnored)) return false)
    teleportRequestSet.add(request)
  }

  def getRelevantRequestsOn(player: EntityPlayerMP): mutable.LinkedHashSet[ITeleportRequest] = teleportRequestSet.filter(r => r.isRelevantTo(player))
}
