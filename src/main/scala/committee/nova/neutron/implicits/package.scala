package committee.nova.neutron

import committee.nova.neutron.api.player.storage.{IHome, IPosWithDim}
import committee.nova.neutron.api.reference.INamed
import committee.nova.neutron.server.player.storage.NeutronEEP
import committee.nova.neutron.server.ui.container.ContainerInteractable
import committee.nova.neutron.util.collection.LimitedLinkedList
import committee.nova.neutron.util.reference.Tags
import cpw.mods.fml.common.event.FMLServerStartingEvent
import net.minecraft.command.ICommand
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString}
import net.minecraft.network.play.server.{S29PacketSoundEffect, S2DPacketOpenWindow}

import scala.collection.mutable
import scala.language.implicitConversions

package object implicits {
  implicit class PlayerImplicit(val player: EntityPlayer) {
    private def getNeutron: NeutronEEP = player.getExtendedProperties(NeutronEEP.id).asInstanceOf[NeutronEEP]

    def playNotifySound(sound: String): Unit = {
      player match {
        case mp: EntityPlayerMP => mp.playerNetServerHandler.sendPacket(new S29PacketSoundEffect(sound, mp.posX, mp.posY, mp.posZ, 1.0F, 1.0F))
        case _ =>
      }
    }

    def teleport(dim: Int, x: Double, y: Double, z: Double, yaw: Float, pitch: Float): Unit = {
      player match {
        case mp: EntityPlayerMP => {
          if (dim != mp.dimension) mp.travelToDimension(dim)
          mp.playerNetServerHandler.setPlayerLocation(x, y, z, yaw, pitch)
          mp.playNotifySound("mob.endermen.portal")
        }
      }
    }

    def teleportTo(that: EntityPlayerMP): Boolean = {
      if (!player.isInstanceOf[EntityPlayerMP]) return false
      val mp = player.asInstanceOf[EntityPlayerMP]
      try {
        teleport(that.dimension, that.posX, that.posY, that.posZ, that.rotationYaw, that.rotationPitch)
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

    def appendExtraToolTip(tooltips: String*): ItemStack = {
      val tag = stack.getOrCreateTag
      val tooltipList = new NBTTagList
      tooltips.foreach(t => tooltipList.appendTag(new NBTTagString(t)))
      tag.setTag(Tags.TOOLTIPABLE, tooltipList)
      stack
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
