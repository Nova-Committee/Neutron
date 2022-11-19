package committee.nova.neutron.server.event.handler

import committee.nova.neutron.server.storage.ServerStorage
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent.{Phase, ServerTickEvent}

object FMLEventHandler {
  def init(): Unit = FMLCommonHandler.instance().bus().register(new FMLEventHandler)
}

class FMLEventHandler {
  @SubscribeEvent
  def onServerTick(e: ServerTickEvent): Unit = if (e.phase == Phase.END) ServerStorage.tick()
}
