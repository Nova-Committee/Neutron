package committee.nova.neutron.server.event.handler

import committee.nova.neutron.implicits._
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.event.impl.{InteractableItemClickEvent, TeleportFromEvent}
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.server.player.storage.{FormerPos, NeutronEEP}
import committee.nova.neutron.server.storage.ServerStorage
import committee.nova.neutron.util.reference.Tags
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.util.{ChatStyle, EnumChatFormatting, Vec3}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.{ItemTooltipEvent, PlayerEvent}

object ForgeEventHandler {
  def init(): Unit = MinecraftForge.EVENT_BUS.register(new ForgeEventHandler)
}

class ForgeEventHandler {
  @SubscribeEvent
  def onConstruct(event: EntityConstructing): Unit = {
    if (!event.entity.isInstanceOf[EntityPlayer]) return
    event.entity.registerExtendedProperties(NeutronEEP.id, new NeutronEEP)
  }

  @SubscribeEvent
  def onDeath(event: LivingDeathEvent): Unit = {
    if (!event.entityLiving.isInstanceOf[EntityPlayerMP]) return
    val player = event.entityLiving.asInstanceOf[EntityPlayerMP]
    MinecraftForge.EVENT_BUS.post(TeleportFromEvent(player, player.dimension, player.posX, player.posY, player.posZ))
  }

  @SubscribeEvent
  def onClone(event: PlayerEvent.Clone): Unit = {
    val oldPlayer = event.original
    val newPlayer = event.entityPlayer
    val tag = new NBTTagCompound
    oldPlayer.getExtendedProperties(NeutronEEP.id).saveNBTData(tag)
    newPlayer.getExtendedProperties(NeutronEEP.id).loadNBTData(tag)
  }

  @SubscribeEvent
  def onTeleport(event: TeleportFromEvent): Unit = {
    event.player.getFormerPos.addWithLimit(new FormerPos(event.oldDim, Vec3.createVectorHelper(event.oldX, event.oldY, event.oldZ)), ServerConfig.getMaxFormerPosStorage)
  }

  @SubscribeEvent
  def onLogin(event: PlayerLoggedInEvent): Unit = ServerStorage.uuid2Name.put(event.player.getUniqueID, event.player.getDisplayName)

  @SubscribeEvent
  def onInteractableItemClick(event: InteractableItemClickEvent): Unit = {
    val tag = event.stack.getOrCreateTag.getCompoundTag(Tags.INTERACTABLE)
    MinecraftServer.getServer.getCommandManager.executeCommand(event.player, tag.getString(Tags.CMD))
  }

  @SubscribeEvent
  def onItemTooltip(event: ItemTooltipEvent): Unit = {
    val stack = event.itemStack
    if (!stack.hasTagCompound || !stack.getTagCompound.hasKey(Tags.TOOLTIPABLE)) return
    val tooltipList = stack.getTagCompound.getTagList(Tags.TOOLTIPABLE, 8)
    for (i <- 0 until tooltipList.tagCount) event.toolTip.add(tooltipList.getStringTagAt(i))
  }

  @SubscribeEvent
  def onEntityJoinWorld(event: EntityJoinWorldEvent): Unit = {
    event.entity match {
      case i: EntityItem => {
        val stack = i.getEntityItem
        if (stack.hasTagCompound && stack.getTagCompound.hasKey(Tags.INTERACTABLE)) event.setCanceled(true)
      }
      case _ =>
    }
  }

  @SubscribeEvent
  def onServerChat(event: ServerChatEvent): Unit = {
    if (event.message.startsWith("/")) return
    val player = event.player
    if (player.getMuteStatus.isApplied) {
      player.addChatMessage(new ChatComponentServerTranslation("msg.neutron.chat.muted")
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
      event.setCanceled(true)
    }
  }
}
