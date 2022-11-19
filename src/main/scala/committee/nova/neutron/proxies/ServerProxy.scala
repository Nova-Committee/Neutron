package committee.nova.neutron.proxies

import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.event.handler.FMLEventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent}

class ServerProxy extends CommonProxy {
  override def preInit(event: FMLPreInitializationEvent): Unit = {
    super.preInit(event)
    ServerConfig.init(event)
  }

  override def init(event: FMLInitializationEvent): Unit = {
    super.init(event)
    FMLEventHandler.init()
  }
}
