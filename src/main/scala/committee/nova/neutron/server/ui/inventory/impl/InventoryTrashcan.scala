package committee.nova.neutron.server.ui.inventory.impl

import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.util.Utilities
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.InventoryBasic
import net.minecraft.util.text.{Style, TextFormatting}

class InventoryTrashcan extends InventoryBasic(Utilities.L10n.getFromCurrentLang("ui.neutron.trashcan"), false, 36) {
  override def closeInventory(player: EntityPlayer): Unit = {
    var cleaned = 0
    for (i <- 0 until this.getSizeInventory) {
      val stack = getStackInSlot(i)
      if (!stack.isEmpty) cleaned += stack.getCount
    }
    if (cleaned == 0) return
    player.playNotifySound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE)
    player.sendMessage(new ChatComponentServerTranslation("msg.neutron.ui.trashcan", cleaned)
      .setStyle(new Style().setColor(TextFormatting.YELLOW)))
  }
}
