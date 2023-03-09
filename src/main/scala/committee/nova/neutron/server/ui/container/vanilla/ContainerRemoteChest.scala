package committee.nova.neutron.server.ui.container.vanilla

import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.inventory.{ContainerChest, IInventory}

class ContainerRemoteChest(player: EntityPlayerMP, inv1: IInventory, inv2: IInventory) extends ContainerChest(inv1, inv2, player) {
  override def canInteractWith(player: EntityPlayer): Boolean = true
}
