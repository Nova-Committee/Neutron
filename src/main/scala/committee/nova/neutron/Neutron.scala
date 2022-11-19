package committee.nova.neutron

import committee.nova.neutron.proxies.CommonProxy
import committee.nova.neutron.server.commands.init.CommandInit
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent, FMLServerStartingEvent}
import cpw.mods.fml.common.{Mod, SidedProxy}
import org.apache.logging.log4j.Logger

@Mod(modid = Neutron.MODID, useMetadata = true, modLanguage = "scala", acceptableRemoteVersions = "*")
object Neutron {
  var LOGGER: Logger = _
  final val MODID = "neutron"
  final val packagePrefix = "committee.nova." + MODID + ".proxies."

  //noinspection VarCouldBeVal
  @SidedProxy(serverSide = packagePrefix + "ServerProxy", clientSide = packagePrefix + "ClientProxy")
  var proxy: CommonProxy = _

  @EventHandler def preInit(e: FMLPreInitializationEvent): Unit = {
    LOGGER = e.getModLog
    proxy.preInit(e)
  }

  @EventHandler def init(e: FMLInitializationEvent): Unit = proxy.init(e)

  @EventHandler def postInit(e: FMLPostInitializationEvent): Unit = proxy.postInit(e)

  @EventHandler def onServerStarting(e: FMLServerStartingEvent): Unit = CommandInit.init(e)
}
