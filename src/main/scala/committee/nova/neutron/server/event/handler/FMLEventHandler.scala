package committee.nova.neutron.server.event.handler

import committee.nova.neutron.implicits._
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.storage.ServerStorage
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.{Phase, PlayerTickEvent, ServerTickEvent}
import net.minecraftforge.fml.relauncher.Side

object FMLEventHandler {
  def init(): Unit = FMLCommonHandler.instance().bus().register(new FMLEventHandler)
}

class FMLEventHandler {
  @SubscribeEvent
  def onServerTick(e: ServerTickEvent): Unit = if (e.phase == Phase.END) ServerStorage.tick()

  @SubscribeEvent
  def onPlayerTick(e: PlayerTickEvent): Unit = {
    if (e.side == Side.CLIENT) return
    val player = e.player
    if (e.phase == Phase.START) return
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

  @SubscribeEvent
  def onLogin(event: PlayerLoggedInEvent): Unit = ServerStorage.uuid2Name.put(event.player.getUniqueID, event.player.getName)
}
