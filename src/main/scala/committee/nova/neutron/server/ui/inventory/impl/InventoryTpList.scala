package committee.nova.neutron.server.ui.inventory.impl

import committee.nova.neutron.api.player.request.ITeleportRequest
import committee.nova.neutron.api.ui.inventory.{IPageable, ISwitchable, ITpListFilter, TpListFilter}
import committee.nova.neutron.implicits._
import committee.nova.neutron.server.player.request.{TeleportHereRequest, TeleportToRequest}
import committee.nova.neutron.server.storage.ServerStorage
import committee.nova.neutron.server.ui.inventory.base.InventoryInteraction
import committee.nova.neutron.server.ui.inventory.base.InventoryInteraction.{getNonInteractablePageStack, getRefreshStack}
import committee.nova.neutron.server.ui.inventory.impl.InventoryTpList.Filter._
import committee.nova.neutron.server.ui.inventory.impl.InventoryTpList.{getRequestStack, getSwitchStack}
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.Tags
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumChatFormatting

object InventoryTpList {
  def getItemFromRequest(request: ITeleportRequest): Item = {
    request match {
      case _: TeleportToRequest => Item.getItemFromBlock(Blocks.stained_glass)
      case _: TeleportHereRequest => Item.getItemFromBlock(Blocks.stained_hardened_clay)
      case _ => Item.getItemFromBlock(Blocks.glass)
    }
  }

  def wasIgnored(request: ITeleportRequest, player: EntityPlayerMP): Boolean = request.wasIgnored && request.getReceiver.equals(player.getUniqueID)

  def getColorFromRequest(request: ITeleportRequest, player: EntityPlayerMP): Int = {
    if (wasIgnored(request, player)) return 7
    request match {
      case _: TeleportToRequest => 5
      case _: TeleportHereRequest => 3
      case _ => 7
    }
  }

  def getRequestStack(request: ITeleportRequest, player: EntityPlayerMP): ItemStack = {
    val stack = new ItemStack(getItemFromRequest(request), 1, getColorFromRequest(request, player))
    val tag = stack.getOrCreateTag
    val interaction = new NBTTagCompound
    interaction.setString(Tags.CMD, s"/tprequest ${request.getId}")
    tag.setTag(Tags.INTERACTABLE, interaction)
    stack.setTagDisplayName(request.getInfo)
      .appendExtraTooltip(if (wasIgnored(request, player)) EnumChatFormatting.GRAY + Utilities.L10n.getFromCurrentLang("phr.neutron.ignored")
      else EnumChatFormatting.YELLOW + Utilities.L10n.getFromCurrentLang("phr.neutron.pending"))
  }

  def getSwitchStack(current: Int): ItemStack = {
    val stack = new ItemStack(Items.compass)
    val tag = stack.getOrCreateTag
    val interaction = new NBTTagCompound
    val next = getNextFilterId(current)
    interaction.setString(Tags.CMD, s"/${getListCmd(next)} 1")
    tag.setTag(Tags.INTERACTABLE, interaction)
    stack.setTagDisplayName(Utilities.L10n.getFromCurrentLang("button.neutron.switch",
      Utilities.L10n.getFromCurrentLang(s"ui.neutron.tplist.$current")))
      .appendExtraTooltip(EnumChatFormatting.YELLOW + Utilities.L10n.getFromCurrentLang("button.neutron.switchTo",
        Utilities.L10n.getFromCurrentLang(s"ui.neutron.tplist.$next")))
  }


  def getNextFilterId(current: Int): Int = (current + 1) % 5

  def getListCmd(ordinal: Int): String = {
    Filter.getFilters.foreach(f => if (ordinal == f.getId) return f.getCommand)
    "tplist"
  }

  object Filter {
    case object ALL extends TpListFilter(0, "tplist", (p, r) => r.isRelevantTo(p))

    case object RECEIVED extends TpListFilter(1, "tprlist", (p, r) => r.getReceiver.equals(p.getUniqueID))

    case object SENT extends TpListFilter(2, "tpslist", (p, r) => r.getSender.equals(p.getUniqueID))

    case object TO extends TpListFilter(3, "tptlist", (p, r) => r.isRelevantTo(p) && r.isInstanceOf[TeleportToRequest])

    case object HERE extends TpListFilter(4, "tphlist", (p, r) => r.isRelevantTo(p) && r.isInstanceOf[TeleportHereRequest])

    def getFilters: Array[ITpListFilter] = Array(ALL, TO, HERE, SENT, RECEIVED)
  }

  class All(player: EntityPlayerMP, page: Int) extends InventoryTpList(player, page, ALL)

  class Received(player: EntityPlayerMP, page: Int) extends InventoryTpList(player, page, RECEIVED)

  class Sent(player: EntityPlayerMP, page: Int) extends InventoryTpList(player, page, SENT)

  class To(player: EntityPlayerMP, page: Int) extends InventoryTpList(player, page, TO)

  class Here(player: EntityPlayerMP, page: Int) extends InventoryTpList(player, page, HERE)
}

class InventoryTpList(player: EntityPlayerMP, page: Int, filter: ITpListFilter) extends InventoryInteraction(player, Utilities.L10n.getFromCurrentLang(s"ui.neutron.tplist.${filter.getId}"), 36)
  with ISwitchable with IPageable {
  override def init(): Unit = {
    val requests = getRequests
    val real = checkPage
    setInventorySlotContents(28, getSwitchStack(getId))
    setInventorySlotContents(34, getRefreshStack(getPageableCommand, page.toString))
    setInventorySlotContents(27, if (real > 1) getPageStack(real, isNext = false) else getNonInteractablePageStack(false))
    val size = 33 min (requests.size - 32 * (real - 1))
    setInventorySlotContents(35, if (requests.size > 32 * (real - 1) + size) getPageStack(real, isNext = true) else getNonInteractablePageStack(true))
    for (i <- 0 until 27.min(size)) setInventorySlotContents(i, getRequestStack(requests(34 * (real - 1) + i), player))
    if (size < 27) return
    for (i <- 29 until 34.min(size)) setInventorySlotContents(i, getRequestStack(requests(34 * (real - 1) + i - 1), player))
  }

  def getRequests: List[ITeleportRequest] = ServerStorage.teleportRequestSet.filter(filter.getFilter.curried.apply(player)).toList

  override def getTotal: Int = getRequests.size

  override def getId: Int = filter.getId

  override def getPageableCommand: String = filter.getCommand

  override def getPage: Int = page

  override def getPageCapacity: Int = 32
}
