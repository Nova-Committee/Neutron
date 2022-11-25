package committee.nova.neutron.server.ui.inventory.impl

import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.util.Utilities
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.InventoryBasic
import net.minecraft.util.{ChatStyle, EnumChatFormatting}

class InventoryTrashcan(player: EntityPlayerMP) extends InventoryBasic(Utilities.L10n.getFromCurrentLang("ui.neutron.trashcan"), false, 36) {
  override def closeInventory(): Unit = {
    var cleaned = 0
    for (i <- 0 until this.getSizeInventory) {
      val stack = getStackInSlotOnClosing(i)
      if (stack != null) cleaned += stack.stackSize
    }
    if (cleaned == 0) return
    player.playNotifySound("random.fizz")
    player.addChatMessage(new ChatComponentServerTranslation("msg.neutron.ui.trashcan", cleaned)
      .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
  }
}
