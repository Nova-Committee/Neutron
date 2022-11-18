package committee.nova.neutron.proxies

import committee.nova.neutron.server.config.ServerConfig
import cpw.mods.fml.common.event.FMLPreInitializationEvent

class ServerProxy extends CommonProxy {
  override def preInit(event: FMLPreInitializationEvent): Unit = {
    super.preInit(event)
    ServerConfig.init(event)
  }
}
