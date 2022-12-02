package committee.nova.neutron.server.ui.inventory.base

import committee.nova.neutron.api.ui.inventory.IPageable
import committee.nova.neutron.implicits._
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.Tags
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.{Blocks, Items}
import net.minecraft.inventory.InventoryBasic
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

object InventoryInteraction {
  def getNonInteractablePageStack(isLast: Boolean): ItemStack = {
    val stack = new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 7)
    stack.setTagDisplayName(Utilities.L10n.getFromCurrentLang(s"button.neutron.${if (isLast) "last" else "first"}"))
    stack
  }

  def getPageStack(cmd: String, current: Int, isNext: Boolean): ItemStack = {
    val stack = new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, if (isNext) 5 else 14)
    val tag = stack.getOrCreateTag
    val interaction = new NBTTagCompound
    interaction.setString(Tags.CMD, s"/$cmd ${current + (if (isNext) 1 else -1)}")
    tag.setTag(Tags.INTERACTABLE, interaction)
    stack.setTagDisplayName(Utilities.L10n.getFromCurrentLang(s"button.neutron.${if (isNext) "next" else "previous"}"))
  }

  def getRefreshStack(current: String, arg: String): ItemStack = {
    val stack = new ItemStack(Items.water_bucket)
    val tag = stack.getOrCreateTag
    val interaction = new NBTTagCompound
    interaction.setString(Tags.CMD, s"/$current $arg")
    tag.setTag(Tags.INTERACTABLE, interaction)
    stack.setTagDisplayName(Utilities.L10n.getFromCurrentLang("button.neutron.refresh"))
  }
}

abstract class InventoryInteraction(val player: EntityPlayerMP, val key: String, val size: Int)
  extends InventoryBasic(Utilities.L10n.getFromCurrentLang(key), true, size) {
  this match {
    case pageable: IPageable => this.func_110133_a(Utilities.L10n.getFromCurrentLang(key) + " " + Utilities.L10n.getFromCurrentLang("ui.neutron.page", pageable.checkPage))
    case _ =>
  }
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
