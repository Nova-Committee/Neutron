package committee.nova.neutron.server.ui.container.vanilla

import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.inventory.ContainerRepair

class ContainerRemoteAnvil(player: EntityPlayerMP) extends ContainerRepair(player.inventory, player.world,
  player.getPosition, player) {
  override def canInteractWith(player: EntityPlayer): Boolean = true
}
