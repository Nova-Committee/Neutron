package committee.nova.neutron.server.event.handler

import committee.nova.neutron.implicits.PlayerImplicit
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
  }
}
