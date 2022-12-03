package committee.nova.neutron.server.ui.inventory.impl

import committee.nova.neutron.api.player.request.ITeleportRequest
import committee.nova.neutron.implicits._
import committee.nova.neutron.server.player.request.TeleportToRequest
import committee.nova.neutron.server.storage.ServerStorage
import committee.nova.neutron.server.ui.inventory.base.InventoryInteraction
import committee.nova.neutron.server.ui.inventory.base.InventoryInteraction.getRefreshStack
import committee.nova.neutron.server.ui.inventory.impl.InventoryTpRequest.{getAcceptStack, getCancelStack, getDenyStack, getIgnoreStack}
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.Tags
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

import java.util.UUID

object InventoryTpRequest {
  def getAcceptStack(request: ITeleportRequest): ItemStack = getActionStack(request, "accept", 5)

  def getDenyStack(request: ITeleportRequest): ItemStack = getActionStack(request, "deny", 14)

  def getIgnoreStack(request: ITeleportRequest): ItemStack = getActionStack(request, "ignore", 15)

  def getCancelStack(request: ITeleportRequest): ItemStack = getActionStack(request, "cancel", 15)

  private def getActionStack(request: ITeleportRequest, action: String, color: Int): ItemStack = {
    val stack = new ItemStack(Item.getItemFromBlock(Blocks.stained_glass_pane), 1, color)
    val tag = stack.getOrCreateTag
    val interaction = new NBTTagCompound
    interaction.setString(Tags.CMD, s"/tp$action ${request.getId}")
    tag.setTag(Tags.INTERACTABLE, interaction)
    stack.setTagDisplayName(Utilities.L10n.getFromCurrentLang(s"button.neutron.$action"))
  }
}

class InventoryTpRequest(player: EntityPlayerMP, tpid: UUID) extends InventoryInteraction(player, Utilities.L10n.getFromCurrentLang("ui.neutron.tprequest"), 9) {
  override def init(): Unit = {
    setInventorySlotContents(8, getRefreshStack("tprequest", tpid.toString))
    for (request <- ServerStorage.teleportRequestSet if (tpid == request.getId && request.isRelevantTo(player))) {
      val to = request.isInstanceOf[TeleportToRequest]
      setInventorySlotContents(if (to) 1 else 7, Utilities.Player.getPlayerSkull(Utilities.Player.getPlayerNameByUUID(request.getSender)))
      setInventorySlotContents(if (to) 7 else 1, Utilities.Player.getPlayerSkull(Utilities.Player.getPlayerNameByUUID(request.getReceiver)))
      val isSender = request.getSender.equals(player.getUniqueID)
      if (isSender) setInventorySlotContents(4, getCancelStack(request)) else {
        setInventorySlotContents(3, getAcceptStack(request))
        setInventorySlotContents(4, getDenyStack(request))
        setInventorySlotContents(5, getIgnoreStack(request))
      }
      return
    }
  }
}
