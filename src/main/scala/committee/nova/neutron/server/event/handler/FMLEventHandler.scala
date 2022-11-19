package committee.nova.neutron.server.event.handler

import committee.nova.neutron.server.storage.ServerStorage
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.gameevent.TickEvent.{Phase, ServerTickEvent}

object FMLEventHandler {
  def init(): Unit = FMLCommonHandler.instance().bus().register(new FMLEventHandler)
}

class FMLEventHandler {
  @EventHandler
  def onServerTick(e: ServerTickEvent): Unit = if (e.phase == Phase.START) ServerStorage.tick()
}
