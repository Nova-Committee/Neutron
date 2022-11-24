package committee.nova.neutron.server.ui.inventory.impl

import committee.nova.neutron.util.Utilities
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.InventoryBasic
import net.minecraft.network.play.server.S29PacketSoundEffect

class InventoryTrashcan(player: EntityPlayerMP) extends InventoryBasic(Utilities.L10n.getFromCurrentLang("ui.neutron.trashcan"), false, 36) {
  override def closeInventory(): Unit = {
    for (i <- 0 until this.getSizeInventory if getStackInSlotOnClosing(i) != null) {
      player.playerNetServerHandler.sendPacket(new S29PacketSoundEffect("random.fizz", player.posX, player.posY, player.posZ, 1.0F, 1.0F))
      return
    }
  }
}
