package committee.nova.neutron.server.event.handler

import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.event.impl.TeleportFromEvent
import committee.nova.neutron.server.player.storage.FormerPos
import cpw.mods.fml.common.eventhandler.SubscribeEvent
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
    val player = event.player
    player.getFormerPos.addWithLimit(new FormerPos(event.oldDim, Vec3.createVectorHelper(event.oldX, event.oldY, event.oldZ)), ServerConfig.getMaxFormerPosStorage)
  }
}
