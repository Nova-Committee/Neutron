package committee.nova.neutron

import committee.nova.neutron.api.player.storage.{IHome, IPosWithDim}
import committee.nova.neutron.api.reference.INamed
import committee.nova.neutron.server.player.storage.NeutronEEP
import committee.nova.neutron.server.ui.container.ContainerInteractable
import committee.nova.neutron.util.collection.LimitedLinkedList
import cpw.mods.fml.common.event.FMLServerStartingEvent
import net.minecraft.command.ICommand
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.S2DPacketOpenWindow

import scala.collection.mutable
import scala.language.implicitConversions

package object implicits {
  implicit class PlayerImplicit(val player: EntityPlayer) {
    private def getNeutron: NeutronEEP = player.getExtendedProperties(NeutronEEP.id).asInstanceOf[NeutronEEP]

    def teleportTo(that: EntityPlayerMP): Boolean = {
      if (!player.isInstanceOf[EntityPlayerMP]) return false
      val mp = player.asInstanceOf[EntityPlayerMP]
      try {
        if (mp.dimension != that.dimension) mp.travelToDimension(that.dimension)
        mp.playerNetServerHandler.setPlayerLocation(that.posX, that.posY, that.posZ, that.rotationYaw, that.rotationPitch)
      } catch {
        case e: Exception =>
          e.printStackTrace()
          return false
      }
      true
    }

    def displayGUIInteractable(interactable: IInventory): Unit = {
      if (!player.isInstanceOf[EntityPlayerMP]) return
      val mp = player.asInstanceOf[EntityPlayerMP]
      if (mp.openContainer != mp.inventoryContainer) mp.closeScreen()
      mp.getNextWindowId()
      mp.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(mp.currentWindowId, 0, interactable.getInventoryName, interactable.getSizeInventory, interactable.hasCustomInventoryName))
      mp.openContainer = new ContainerInteractable(mp.inventory, interactable);
      mp.openContainer.windowId = mp.currentWindowId;
      mp.openContainer.addCraftingToCrafters(mp);
    }

    def getTpaCoolDown: Int = getNeutron.getTpaCoolDown

    def setTpaCoolDown(cd: Int): Unit = getNeutron.setTpaCoolDown(cd)

    def getRtpAccumulation: Int = getNeutron.getRtpAccumulation

    def setRtpAccumulation(acc: Int): Unit = getNeutron.setRtpAccumulation(acc)

    def getHomes: mutable.LinkedHashSet[IHome] = getNeutron.getHomes

    def setHomes(homes: mutable.LinkedHashSet[IHome]): Unit = getNeutron.setHomes(homes)

    def getFormerPos: LimitedLinkedList[IPosWithDim] = getNeutron.getFormerPos

    def setFormerPos(former: LimitedLinkedList[IPosWithDim]): Unit = getNeutron.setFormerPos(former)
  }

  implicit class ItemStackImplicit(val stack: ItemStack) {
    def getOrCreateTag: NBTTagCompound = {
      if (stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound
      stack.stackTagCompound
    }
  }

  implicit class NBTTagCompoundImplicit(val tag: NBTTagCompound) {
    def getOrCreateTag(name: String): NBTTagCompound = {
      if (!tag.hasKey(name)) tag.setTag(name, new NBTTagCompound)
      tag.getCompoundTag(name)
    }
  }

  implicit class FMLServerStartingEventImplicit(val event: FMLServerStartingEvent) {
    def registerServerCommands(cmds: ICommand*): Unit = cmds.foreach(c => event.registerServerCommand(c))
  }

  implicit def named2Str(named: INamed): String = named.getName
}
