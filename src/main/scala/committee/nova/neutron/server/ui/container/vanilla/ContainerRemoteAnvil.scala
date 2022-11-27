package committee.nova.neutron.server.ui.container.vanilla

import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.inventory.ContainerRepair

class ContainerRemoteAnvil(player: EntityPlayerMP) extends ContainerRepair(player.inventory, player.worldObj,
  player.posX.floor.toInt, player.posY.floor.toInt, player.posZ.floor.toInt, player) {
  override def canInteractWith(player: EntityPlayer): Boolean = true
}
