package committee.nova.neutron.server.ui.container.vanilla

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.{ContainerChest, IInventory}

class ContainerRemoteChest(inv1: IInventory, inv2: IInventory) extends ContainerChest(inv1, inv2) {
  override def canInteractWith(player: EntityPlayer): Boolean = true
}
