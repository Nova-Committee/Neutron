package committee.nova.neutron.server.ui.inventory.impl

import committee.nova.neutron.api.player.storage.IHome
import committee.nova.neutron.api.ui.inventory.IPageable
import committee.nova.neutron.implicits._
import committee.nova.neutron.server.ui.inventory.base.InventoryInteraction
import committee.nova.neutron.server.ui.inventory.base.InventoryInteraction.getNonInteractablePageStack
import committee.nova.neutron.server.ui.inventory.impl.InventoryHome.getHomeStack
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.Tags
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumChatFormatting

object InventoryHome {
  def getHomeStack(home: IHome): ItemStack = {
    val stack = new ItemStack(Items.bed)
    val tag = stack.getOrCreateTag
    val interaction = new NBTTagCompound
    interaction.setString(Tags.CMD, s"/home ${home.getName}")
    tag.setTag(Tags.INTERACTABLE, interaction)
    stack.setTagDisplayName(home.getName).appendExtraTooltip(EnumChatFormatting.YELLOW + Utilities.L10n.getFromCurrentLang("phr.neutron.position",
      s"DIM${home.getDim} >> ${Utilities.Location.getLiteralFromVec3(home.getPos)}"))
  }
}

class InventoryHome(player: EntityPlayerMP, page: Int) extends InventoryInteraction(player, "ui.neutron.home", 36, page) with IPageable {
  override def init(): Unit = {
    val homes = player.getHomes.toList
    val real = checkPage
    setInventorySlotContents(27, if (real > 1) getPageStack(real, isNext = false) else getNonInteractablePageStack(false))
    val size = 35 min (homes.size - 34 * (real - 1))
    setInventorySlotContents(35, if (homes.size > 34 * (real - 1) + size) getPageStack(real, isNext = true) else getNonInteractablePageStack(true))
    for (i <- 0 until 27.min(size)) setInventorySlotContents(i, getHomeStack(homes(34 * (real - 1) + i)))
    if (size < 27) return
    for (i <- 28 until 35.min(size)) setInventorySlotContents(i, getHomeStack(homes(34 * (real - 1) + i - 1)))
  }

  override def getTotal: Int = player.getHomes.toList.size

  override def getPageableCommand: String = "homegui"
}
