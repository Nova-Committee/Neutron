package committee.nova.neutron.server.event.handler

import committee.nova.neutron.Neutron
import committee.nova.neutron.implicits._
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.event.impl.{InteractableItemClickEvent, TeleportFromEvent}
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.server.player.storage.FormerPos
import committee.nova.neutron.server.player.storage.capability.impl.NeutronCapability
import committee.nova.neutron.util.reference.Tags
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.{Style, TextFormatting}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.{AttachCapabilitiesEvent, ServerChatEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ForgeEventHandler {
  def init(): Unit = MinecraftForge.EVENT_BUS.register(new ForgeEventHandler)
}

class ForgeEventHandler {
  @SubscribeEvent
  def onAttachCap(event: AttachCapabilitiesEvent[Entity]): Unit = {
    event.getObject match {
      case _: EntityPlayer => event.addCapability(new ResourceLocation(Neutron.MODID, Neutron.MODID), new NeutronCapability.Provider)
      case _ =>
    }
  }

  @SubscribeEvent
  def onDeath(event: LivingDeathEvent): Unit = {
    if (!event.getEntityLiving.isInstanceOf[EntityPlayerMP]) return
    val player = event.getEntityLiving.asInstanceOf[EntityPlayerMP]
    MinecraftForge.EVENT_BUS.post(TeleportFromEvent(player, player.dimension, player.posX, player.posY, player.posZ))
  }

  @SubscribeEvent
  def onClone(event: PlayerEvent.Clone): Unit = {
    val oldPlayer = event.getOriginal
    val newPlayer = event.getEntityPlayer
    val cap = Neutron.neutronCapability
    val storage = cap.getStorage
    if (!(oldPlayer.hasCapability(cap, null) && newPlayer.hasCapability(cap, null))) return
    storage.readNBT(cap, newPlayer.getCapability(cap, null), null, storage.writeNBT(cap, oldPlayer.getCapability(cap, null), null))
    if (event.isWasDeath && ServerConfig.shouldKeepStatsAfterSuicide) {
      val suicide = newPlayer.getStatsBeforeSuicide
      if (!suicide.isValid) return
      newPlayer.setHealth(suicide.getHealth)
      newPlayer.getFoodStats.addStats(suicide.getFoodLevel, suicide.getSaturation)
      suicide.setValid(false)
    }
  }

  @SubscribeEvent
  def onTeleport(event: TeleportFromEvent): Unit = {
    event.player.getFormerPos.addWithLimit(new FormerPos(event.oldDim, new Vec3d(event.oldX, event.oldY, event.oldZ)), ServerConfig.getMaxFormerPosStorage)
  }

  @SubscribeEvent
  def onInteractableItemClick(event: InteractableItemClickEvent): Unit = {
    val tag = event.stack.getOrCreateTag.getCompoundTag(Tags.INTERACTABLE)
    event.player.server.getCommandManager.executeCommand(event.player, tag.getString(Tags.CMD))
  }

  @SubscribeEvent
  def onEntityJoinWorld(event: EntityJoinWorldEvent): Unit = {
    event.getEntity match {
      case i: EntityItem =>
        val stack = i.getItem
        if (stack.hasTagCompound && stack.getTagCompound.hasKey(Tags.INTERACTABLE)) event.setCanceled(true)
      case _ =>
    }
  }

  @SubscribeEvent
  def onServerChat(event: ServerChatEvent): Unit = {
    if (event.getMessage.startsWith("/")) return
    val player = event.getPlayer
    if (player.getMuteStatus.isApplied) {
      player.sendMessage(new ChatComponentServerTranslation("msg.neutron.chat.muted")
        .setStyle(new Style().setColor(TextFormatting.RED)))
      event.setCanceled(true)
    }
  }
}
