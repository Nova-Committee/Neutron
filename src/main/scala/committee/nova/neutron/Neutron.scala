package committee.nova.neutron

import committee.nova.neutron.server.command.init.CommandInit
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.event.handler.{FMLEventHandler, ForgeEventHandler}
import committee.nova.neutron.util.Utilities
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
    ServerConfig.init(e)
    val initialLang = Utilities.L10n.initializeL10n(ServerConfig.getLanguage)
    if (e.getSide == Side.CLIENT) LOGGER.warn(initialLang.get("msg.neutron.warn.clientInit"))
    else Array(
      "************************************************************************",
      " $$\\   $$\\                       $$\\                                    ",
      " $$$\\  $$ |                      $$ |                                   ",
      " $$$$\\ $$ | $$$$$$\\  $$\\   $$\\ $$$$$$\\    $$$$$$\\   $$$$$$\\  $$$$$$$\\   ",
      " $$ $$\\$$ |$$  __$$\\ $$ |  $$ |\\_$$  _|  $$  __$$\\ $$  __$$\\ $$  __$$\\  ",
      " $$ \\$$$$ |$$$$$$$$ |$$ |  $$ |  $$ |    $$ |  \\__|$$ /  $$ |$$ |  $$ | ",
      " $$ |\\$$$ |$$   ____|$$ |  $$ |  $$ |$$\\ $$ |      $$ |  $$ |$$ |  $$ | ",
      " $$ | \\$$ |\\$$$$$$$\\ \\$$$$$$  |  \\$$$$  |$$ |      \\$$$$$$  |$$ |  $$ | ",
      " \\__|  \\__| \\_______| \\______/    \\____/ \\__|       \\______/ \\__|  \\__| ",
      "************************************************************************",
      "Activating Neutron..."
    ).foreach(s => LOGGER.info(s))
  }

  @EventHandler def init(e: FMLInitializationEvent): Unit = {
    FMLEventHandler.init()
    ForgeEventHandler.init()
  }

  @EventHandler def postInit(e: FMLPostInitializationEvent): Unit = {}

  @EventHandler def onServerStarting(e: FMLServerStartingEvent): Unit = CommandInit.init(e)
}
