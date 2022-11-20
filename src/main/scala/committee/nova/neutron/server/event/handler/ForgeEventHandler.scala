package committee.nova.neutron.server.event.handler

import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.event.impl.TeleportFromEvent
import cpw.mods.fml.common.eventhandler.SubscribeEvent
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
    newPlayer.setFormerX(oldPlayer.getFormerX)
    newPlayer.setFormerY(oldPlayer.getFormerY)
    newPlayer.setFormerZ(oldPlayer.getFormerZ)
    newPlayer.setFormerDim(oldPlayer.getFormerDim)
  }

  @SubscribeEvent
  def onTeleport(event: TeleportFromEvent): Unit = {
    val player = event.player
    player.setFormerX(event.oldX)
    player.setFormerY(event.oldY)
    player.setFormerZ(event.oldZ)
    player.setFormerDim(event.oldDim)
  }
}
