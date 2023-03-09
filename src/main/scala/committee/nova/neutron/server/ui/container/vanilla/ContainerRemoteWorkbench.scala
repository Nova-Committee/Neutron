package committee.nova.neutron.server.ui.container.vanilla

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ContainerWorkbench

class ContainerRemoteWorkbench(player: EntityPlayer) extends ContainerWorkbench(player.inventory, player.world, player.getPosition) {
  override def canInteractWith(player: EntityPlayer): Boolean = true
}
