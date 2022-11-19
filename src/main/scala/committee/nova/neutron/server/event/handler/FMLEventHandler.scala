package committee.nova.neutron.server.event.handler

import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.storage.ServerStorage
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent.{Phase, PlayerTickEvent, ServerTickEvent}

object FMLEventHandler {
  def init(): Unit = FMLCommonHandler.instance().bus().register(new FMLEventHandler)
}

class FMLEventHandler {
  @SubscribeEvent
  def onServerTick(e: ServerTickEvent): Unit = if (e.phase == Phase.END) ServerStorage.tick()

  @SubscribeEvent
  def onPlayerTick(e: PlayerTickEvent): Unit = {
    if (e.phase == Phase.END) return
    val player = e.player
    // RTP Accumulation
    if (player.getRtpAccumulation > 0) {
      val max = ServerConfig.getMaxRtpChances * ServerConfig.getRtpChancesRecoveryTime
      if (player.getRtpAccumulation > max) player.setRtpAccumulation(max)
      player.setRtpAccumulation(player.getRtpAccumulation - 1)
    }
    // Teleportation CD
    if (player.getTpaCoolDown > 0) {
      val max = ServerConfig.getTpCoolDown
      if (player.getTpaCoolDown > max) player.setTpaCoolDown(max)
      player.setTpaCoolDown(player.getTpaCoolDown - 1)
    }
  }
}
