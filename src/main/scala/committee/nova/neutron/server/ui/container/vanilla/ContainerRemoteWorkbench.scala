package committee.nova.neutron.server.ui.container.vanilla

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ContainerWorkbench

class ContainerRemoteWorkbench(player: EntityPlayer) extends ContainerWorkbench(player.inventory, player.worldObj, player.posX.floor.toInt, player.posY.floor.toInt, player.posZ.floor.toInt) {
  override def canInteractWith(player: EntityPlayer): Boolean = true
}
