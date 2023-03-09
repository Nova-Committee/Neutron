package committee.nova.neutron.api.ui.inventory

import committee.nova.neutron.implicits._
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.Tags
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

trait IPageable {
  def getPage: Int

  def getTotal: Int

  def getPageCapacity: Int

  def checkPage: Int = {
    val real = getTotal * 1.0 / getPageCapacity
    if (real + 1 >= getPage) getPage else 1
  }

  def getPageableCommand: String

  def getPageStack(current: Int, isNext: Boolean): ItemStack = {
    val stack = new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, if (isNext) 5 else 14)
    val tag = stack.getOrCreateTag
    val interaction = new NBTTagCompound
    interaction.setString(Tags.CMD, s"/$getPageableCommand ${current + (if (isNext) 1 else -1)}")
    tag.setTag(Tags.INTERACTABLE, interaction)
    stack.setTagDisplayName(Utilities.L10n.getFromCurrentLang(s"button.neutron.${if (isNext) "next" else "previous"}"))
  }
}
