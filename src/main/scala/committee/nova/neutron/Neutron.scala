package committee.nova.neutron

import committee.nova.neutron.server.commands.init.CommandInit
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.event.handler.FMLEventHandler
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event._
import cpw.mods.fml.relauncher.Side
import org.apache.logging.log4j.Logger

@Mod(modid = Neutron.MODID, useMetadata = true, modLanguage = "scala", acceptableRemoteVersions = "*")
object Neutron {
  var LOGGER: Logger = _
  final val MODID = "neutron"
  final val isServerSide = (e: FMLStateEvent) => e.getSide == Side.SERVER

  @EventHandler def preInit(e: FMLPreInitializationEvent): Unit = {
    LOGGER = e.getModLog
    if (isServerSide(e)) ServerConfig.init(e)
  }

  @EventHandler def init(e: FMLInitializationEvent): Unit = {
    FMLEventHandler.init()
  }

  @EventHandler def postInit(e: FMLPostInitializationEvent): Unit = {}

  @EventHandler def onServerStarting(e: FMLServerStartingEvent): Unit = CommandInit.init(e)
}
