package committee.nova.neutron.server.ui.inventory.base

import committee.nova.neutron.implicits._
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.Tags
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.inventory.InventoryBasic
import net.minecraft.item.{Item, ItemStack}

object InventoryInteraction {
  def getNonInteractablePageStack(isLast: Boolean): ItemStack = {
    val stack = new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 7)
    stack.getOrCreateTag.getOrCreateTag("display").setString("Name", Utilities.L10n.getFromCurrentLang(s"button.neutron.${if (isLast) "last" else "first"}"))
    stack
  }
}

abstract class InventoryInteraction(val player: EntityPlayerMP, val key: String, val size: Int, val page: Int)
  extends InventoryBasic("", true, size) {
  this.func_110133_a(Utilities.L10n.getFromCurrentLang(key) + " " + Utilities.L10n.getFromCurrentLang("ui.neutron.page", checkPage))
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

  def getTotal: Int

  def checkPage: Int = {
    val real = getTotal * 1.0 / 34
    if (real + 1 >= page) page else 1
  }
}
