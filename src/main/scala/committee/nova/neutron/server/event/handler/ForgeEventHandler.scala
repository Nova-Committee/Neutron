package committee.nova.neutron.server.event.handler

import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.event.impl.TeleportFromEvent
import committee.nova.neutron.server.player.storage.FormerPos
import committee.nova.neutron.server.storage.ServerStorage
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent
import net.minecraft.util.Vec3
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerEvent

object ForgeEventHandler {
  def init(): Unit = MinecraftForge.EVENT_BUS.register(new ForgeEventHandler)
}

class ForgeEventHandler {
  @SubscribeEvent
  def onClone(event: PlayerEvent.Clone): Unit = {
    val oldPlayer = event.original
    val newPlayer = event.entityPlayer
    newPlayer.setTpaCoolDown(oldPlayer.getTpaCoolDown)
    newPlayer.setRtpAccumulation(oldPlayer.getRtpAccumulation)
    newPlayer.setHomes(oldPlayer.getHomes)
    newPlayer.setFormerPos(oldPlayer.getFormerPos)
  }

  @SubscribeEvent
  def onTeleport(event: TeleportFromEvent): Unit = {
    event.player.getFormerPos.addWithLimit(new FormerPos(event.oldDim, Vec3.createVectorHelper(event.oldX, event.oldY, event.oldZ)), ServerConfig.getMaxFormerPosStorage)
  }

  @SubscribeEvent
  def onLogin(event: PlayerLoggedInEvent): Unit = ServerStorage.uuid2Name.put(event.player.getUniqueID, event.player.getDisplayName)
}
