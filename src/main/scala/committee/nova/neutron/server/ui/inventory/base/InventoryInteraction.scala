package committee.nova.neutron.server.ui.inventory.base

import committee.nova.neutron.implicits._
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.Tags
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.InventoryBasic
import net.minecraft.item.ItemStack


abstract class InventoryInteraction(val player: EntityPlayerMP, val key: String, val size: Int, val page: Int)
  extends InventoryBasic(Utilities.L10n.getFromCurrentLang(key) + " " + Utilities.L10n.getFromCurrentLang("ui.neutron.page", page), false, size) {
  this.init()

  override def closeInventory(): Unit = {
    for (i <- 0 until getSizeInventory) {
      val stack = getStackInSlot(i)
      if (stack != null && !stack.getOrCreateTag.hasKey(Tags.INTERACTABLE)) {
        val world = player.worldObj
        val item = new EntityItem(world, player.posX, player.posY, player.posZ, getStackInSlot(i))
        world.spawnEntityInWorld(item)
      }
    }
  }

  override def isItemValidForSlot(index: Int, stack: ItemStack): Boolean = false

  def init(): Unit
}
