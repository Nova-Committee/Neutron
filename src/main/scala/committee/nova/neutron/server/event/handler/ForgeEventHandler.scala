package committee.nova.neutron.server.event.handler

import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.event.impl.TeleportFromEvent
import committee.nova.neutron.server.player.storage.{FormerPos, NeutronEEP}
import committee.nova.neutron.server.storage.ServerStorage
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.Vec3
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.PlayerEvent

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
    MinecraftForge.EVENT_BUS.post(TeleportFromEvent(player, player.dimension, player.posX, player.posY, player.posZ));
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
}
