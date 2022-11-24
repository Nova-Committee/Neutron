package committee.nova.neutron.server.ui.inventory.impl

import committee.nova.neutron.api.player.storage.IHome
import committee.nova.neutron.implicits._
import committee.nova.neutron.server.ui.inventory.base.InventoryInteraction
import committee.nova.neutron.server.ui.inventory.impl.InventoryHome.{checkPage, getHomeStack, getPageStack}
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.Tags
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

object InventoryHome {
  def getHomeStack(home: IHome): ItemStack = {
    val stack = new ItemStack(Items.bed)
    val tag = stack.getOrCreateTag
    val interaction = new NBTTagCompound
    interaction.setString(Tags.CMD, s"/home ${home.getName}")
    tag.setTag(Tags.INTERACTABLE, interaction)
    tag.getOrCreateTag("display").setString("Name", home.getName)
    stack
  }

  def getPageStack(current: Int, isNext: Boolean): ItemStack = {
    val stack = new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, if (isNext) 2 else 1)
    val tag = stack.getOrCreateTag
    val interaction = new NBTTagCompound
    interaction.setString(Tags.CMD, s"/homegui ${current + (if (isNext) 1 else -1)}")
    tag.setTag(Tags.INTERACTABLE, interaction)
    tag.getOrCreateTag("display").setString("Name", Utilities.L10n.getFromCurrentLang(s"button.neutron.${if (isNext) "next" else "previous"}"))
    stack
  }

  def checkPage(number: Int, page: Int): Int = {
    val real = number * 1.0 / 34
    if (real + 1 >= page) page else 1
  }
}

class InventoryHome(player: EntityPlayerMP, page: Int) extends InventoryInteraction(player, "ui.neutron.home", 36, page) {
  override def init(): Unit = {
    val homes = player.getHomes.toList
    val real = checkPage(homes.size, page)
    if (real > 1) setInventorySlotContents(27, getPageStack(real, isNext = false))
    val size = 35 min (homes.size - 34 * (real - 1))
    for (i <- 0 until 27.min(size)) setInventorySlotContents(i, getHomeStack(homes(34 * (real - 1) + i)))
    if (size < 27) return
    for (i <- 28 until 35.min(size)) setInventorySlotContents(i, getHomeStack(homes(34 * (real - 1) + i - 1)))
    if (homes.size > 34 * (real - 1) + size) setInventorySlotContents(35, getPageStack(real, isNext = true))
  }
}
