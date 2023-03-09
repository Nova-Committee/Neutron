package committee.nova.neutron.api.ui.inventory

import committee.nova.neutron.api.player.request.ITeleportRequest
import net.minecraft.entity.player.EntityPlayerMP

trait ITpListFilter {
  def getId: Int

  def getCommand: String

  def getFilter: (EntityPlayerMP, ITeleportRequest) => Boolean
}

class TpListFilter(id: Int, command: String, filter: (EntityPlayerMP, ITeleportRequest) => Boolean) extends ITpListFilter {
  override def getId: Int = id

  override def getCommand: String = command

  override def getFilter: (EntityPlayerMP, ITeleportRequest) => Boolean = filter
}


