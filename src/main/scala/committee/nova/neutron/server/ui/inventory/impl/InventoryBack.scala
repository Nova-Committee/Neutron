package committee.nova.neutron.server.ui.inventory.impl

import committee.nova.neutron.api.player.storage.IPosWithDim
import committee.nova.neutron.implicits._
import committee.nova.neutron.server.ui.inventory.base.InventoryInteraction
import committee.nova.neutron.server.ui.inventory.base.InventoryInteraction.getNonInteractablePageStack
import committee.nova.neutron.server.ui.inventory.impl.InventoryBack.{getFormerPosStack, getPageStack}
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.Tags
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

object InventoryBack {
  def getFormerPosStack(ordinal: Int, pos: IPosWithDim): ItemStack = {
    val stack = new ItemStack(Items.ender_pearl)
    val tag = stack.getOrCreateTag
    val interaction = new NBTTagCompound
    val literal = ordinal + 1
    interaction.setString(Tags.CMD, s"/back $literal")
    tag.setTag(Tags.INTERACTABLE, interaction)
    tag.getOrCreateTag("display").setString("Name", Utilities.L10n.getFromCurrentLang("phr.neutron.formerPos", literal.toString))
    stack.appendExtraToolTip(Utilities.L10n.getFromCurrentLang("phr.neutron.position", s"DIM${pos.getDim}:${Utilities.Location.getLiteralFromVec3(pos.getPos)}"))
  }

  def getPageStack(current: Int, isNext: Boolean): ItemStack = {
    val stack = new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, if (isNext) 5 else 14)
    val tag = stack.getOrCreateTag
    val interaction = new NBTTagCompound
    interaction.setString(Tags.CMD, s"/backgui ${current + (if (isNext) 1 else -1)}")
    tag.setTag(Tags.INTERACTABLE, interaction)
    tag.getOrCreateTag("display").setString("Name", Utilities.L10n.getFromCurrentLang(s"button.neutron.${if (isNext) "next" else "previous"}"))
    stack
  }
}

class InventoryBack(player: EntityPlayerMP, page: Int) extends InventoryInteraction(player, "ui.neutron.back", 36, page) {
  override def init(): Unit = {
    val former = player.getFormerPos
    val real = checkPage
    setInventorySlotContents(27, if (real > 1) getPageStack(real, isNext = false) else getNonInteractablePageStack(false))
    val size = 35 min (former.size - 34 * (real - 1))
    setInventorySlotContents(35, if (former.size > 34 * (real - 1) + size) getPageStack(real, isNext = true) else getNonInteractablePageStack(true))
    for (i <- 0 until 27.min(size)) setInventorySlotContents(i, getFormerPosStack(34 * (real - 1) + i, former.get(34 * (real - 1) + i)))
    if (size < 27) return
    for (i <- 28 until 35.min(size)) setInventorySlotContents(i, getFormerPosStack(34 * (real - 1) + i - 1, former.get(34 * (real - 1) + i - 1)))
  }

  override def getTotal: Int = player.getFormerPos.size()
}
