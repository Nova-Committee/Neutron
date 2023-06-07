package committee.nova.neutron

import com.mojang.authlib.GameProfile
import committee.nova.neutron.Neutron.neutronCapability
import committee.nova.neutron.api.player.storage.{IHome, IPosWithDim}
import committee.nova.neutron.api.reference.INamed
import committee.nova.neutron.server.command.init.CommandInit
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.server.player.storage.capability.api.INeutronCapability
import committee.nova.neutron.server.player.storage.{MuteStatus, StatsBeforeSuicide}
import committee.nova.neutron.server.ui.container.ContainerInteractable
import committee.nova.neutron.server.ui.container.vanilla.ContainerRemoteChest
import committee.nova.neutron.server.world.teleporter.NeutronTeleporter
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.collection.LimitedLinkedList
import committee.nova.neutron.util.reference.Tags
import net.minecraft.block.{BlockAnvil, BlockWorkbench}
import net.minecraft.command.{ICommand, ICommandSender}
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.init.{MobEffects, SoundEvents}
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString, NBTUtil}
import net.minecraft.network.play.server.{SPacketOpenWindow, SPacketSoundEffect}
import net.minecraft.potion.PotionEffect
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{Style, TextFormatting}
import net.minecraft.util.{SoundCategory, SoundEvent}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerContainerEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent

import scala.collection.mutable
import scala.language.implicitConversions

package object implicits {
  implicit class PlayerImplicit(val player: EntityPlayer) {
    private def getNeutron: INeutronCapability = player.getCapability(neutronCapability, null)

    def playNotifySound(sound: SoundEvent): Unit = {
      player match {
        case mp: EntityPlayerMP => mp.connection.sendPacket(new SPacketSoundEffect(sound, SoundCategory.PLAYERS, mp.posX, mp.posY, mp.posZ, 1.0F, 1.0F))
        case _ =>
      }
    }

    def teleport(dim: Int, x: Double, y: Double, z: Double, yaw: Float, pitch: Float): Unit = {
      player match {
        case mp: EntityPlayerMP =>
          if (dim != mp.dimension) mp.changeDimension(dim, NeutronTeleporter(new BlockPos(x, y, z)))
          mp.connection.setPlayerLocation(x, y, z, yaw, pitch)
          mp.playNotifySound(SoundEvents.ENTITY_ENDERMEN_TELEPORT)
      }
    }

    def teleportTo(that: EntityPlayerMP): Boolean = {
      if (!player.isInstanceOf[EntityPlayerMP]) return false
      try {
        teleport(that.dimension, that.posX, that.posY, that.posZ, that.rotationYaw, that.rotationPitch)
      } catch {
        case e: Exception =>
          e.printStackTrace()
          return false
      }
      true
    }

    def healAndFeed(): Unit = {
      player.extinguish()
      player.setAir(300)
      player.clearActivePotions()
      player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 60, 0))
      player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 60, 0))
      player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 60, 0))
      player.setHealth(player.getMaxHealth)
      player.getFoodStats.addStats(20, 20.0F)
      player.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.heal.acted")
        .setStyle(new Style().setColor(TextFormatting.GREEN)))
    }

    def displayGUIInteractable(interactable: IInventory): Unit = {
      if (!player.isInstanceOf[EntityPlayerMP]) return
      val mp = player.asInstanceOf[EntityPlayerMP]
      if (mp.openContainer != mp.inventoryContainer) mp.closeScreen()
      mp.getNextWindowId()
      mp.connection.sendPacket(new SPacketOpenWindow(mp.currentWindowId, "minecraft:container", interactable.getDisplayName, interactable.getSizeInventory))
      mp.openContainer = new ContainerInteractable(mp, mp.inventory, interactable)
      mp.openContainer.windowId = mp.currentWindowId
      mp.openContainer.addListener(mp)
      MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(mp, mp.openContainer))
    }

    def displayGUIRemoteChest(inv: IInventory): Unit = {
      if (!player.isInstanceOf[EntityPlayerMP]) return
      val mp = player.asInstanceOf[EntityPlayerMP]
      if (mp.openContainer != mp.inventoryContainer) mp.closeScreen()
      mp.getNextWindowId()
      mp.connection.sendPacket(new SPacketOpenWindow(mp.currentWindowId, "minecraft:container", inv.getDisplayName))
      mp.openContainer = new ContainerRemoteChest(mp, mp.inventory, inv)
      mp.openContainer.windowId = mp.currentWindowId
      mp.openContainer.addListener(mp)
      MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(mp, mp.openContainer))
    }

    def displayGUIRemoteWorkbench(): Unit = {
      if (!player.isInstanceOf[EntityPlayerMP]) return
      val mp = player.asInstanceOf[EntityPlayerMP]
      mp.displayGui(new BlockWorkbench.InterfaceCraftingTable(mp.world, mp.getPosition))
    }

    def displayGUIRemoteAnvil(): Unit = {
      if (!player.isInstanceOf[EntityPlayerMP]) return
      val mp = player.asInstanceOf[EntityPlayerMP]
      mp.displayGui(new BlockAnvil.Anvil(mp.world, mp.getPosition))
    }

    def hasPermOrElse(perm: String, defaultValue: Boolean): Boolean = player match {
      case mp: EntityPlayerMP => Utilities.Perm.hasPermOrElse(mp, perm, _ => defaultValue)
      case _ => false
    }

    def isOp: Boolean = player.getServer.getPlayerList.getOppedPlayers.getEntry(player.getGameProfile) != null

    def setWalkSpeed(walkSpeed: Float): Unit = {
      if (!player.isInstanceOf[EntityPlayerMP]) return
      val mp = player.asInstanceOf[EntityPlayerMP]
      mp.capabilities.walkSpeed = walkSpeed min 1.0F
      mp.sendPlayerAbilities()
      mp.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.walkSpeed.acted", String.valueOf(mp.capabilities.walkSpeed / 0.1F))
        .setStyle(new Style().setColor(TextFormatting.YELLOW)))
    }

    def setFlySpeed(flySpeed: Float): Unit = {
      if (!player.isInstanceOf[EntityPlayerMP]) return
      val mp = player.asInstanceOf[EntityPlayerMP]
      mp.capabilities.flySpeed = flySpeed min 0.25F
      mp.sendPlayerAbilities()
      mp.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.flySpeed.acted", String.valueOf(mp.capabilities.flySpeed / 0.05F))
        .setStyle(new Style().setColor(TextFormatting.YELLOW)))
    }

    def getTpaCoolDown: Int = getNeutron.getTpaCoolDown

    def setTpaCoolDown(cd: Int): Unit = getNeutron.setTpaCoolDown(cd)

    def getRtpAccumulation: Int = getNeutron.getRtpAccumulation

    def setRtpAccumulation(acc: Int): Unit = getNeutron.setRtpAccumulation(acc)

    def getHomes: mutable.LinkedHashSet[IHome] = getNeutron.getHomes

    def setHomes(homes: mutable.LinkedHashSet[IHome]): Unit = getNeutron.setHomes(homes)

    def getFormerPos: LimitedLinkedList[IPosWithDim] = getNeutron.getFormerPos

    def setFormerPos(former: LimitedLinkedList[IPosWithDim]): Unit = getNeutron.setFormerPos(former)

    def getMuteStatus: MuteStatus = getNeutron.getMuteStatus

    def getStatsBeforeSuicide: StatsBeforeSuicide = getNeutron.getStatsBeforeSuicide
  }

  implicit class ItemStackImplicit(val stack: ItemStack) {
    def getOrCreateTag: NBTTagCompound = {
      if (stack.getTagCompound == null) stack.setTagCompound(new NBTTagCompound)
      stack.getTagCompound
    }

    def setTagDisplayName(name: String): ItemStack = {
      getOrCreateTag.getOrCreateTag(Tags.VANILLA_DISPLAY).setString(Tags.VANILLA_NAME, TextFormatting.RESET + name)
      stack
    }

    def appendExtraTooltip(tooltips: String*): ItemStack = {
      val tag = stack.getOrCreateTag.getOrCreateTag(Tags.VANILLA_DISPLAY)
      val tooltipList = new NBTTagList
      tooltips.foreach(t => tooltipList.appendTag(new NBTTagString(TextFormatting.RESET + t)))
      tag.setTag(Tags.VANILLA_LORE, tooltipList)
      stack
    }
  }

  implicit class ICommandSenderImplicit(val sender: ICommandSender) {
    def isOp: Boolean = sender match {
      case p: EntityPlayerMP => p.isOp
      case _ => true
    }
  }

  implicit class NBTTagCompoundImplicit(val tag: NBTTagCompound) {
    def getOrCreateTag(name: String): NBTTagCompound = {
      if (!tag.hasKey(name)) tag.setTag(name, new NBTTagCompound)
      tag.getCompoundTag(name)
    }

    def writeGameProfileIn(profile: GameProfile): NBTTagCompound = {
      NBTUtil.writeGameProfile(tag, profile)
      tag
    }
  }

  implicit class FMLServerStartingEventImplicit(val event: FMLServerStartingEvent) {
    def registerServerCommands(cmds: ICommand*): Unit = cmds.foreach(c => {
      event.registerServerCommand(c)
      CommandInit.commands.add(c)
    })
  }

  implicit def named2Str(named: INamed): String = named.getName
}
