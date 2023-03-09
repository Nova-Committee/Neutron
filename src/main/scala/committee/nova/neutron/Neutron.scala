package committee.nova.neutron

import committee.nova.neutron.server.command.init.CommandInit
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.event.handler.{FMLEventHandler, ForgeEventHandler}
import committee.nova.neutron.server.player.storage.capability.api.INeutronCapability
import committee.nova.neutron.server.player.storage.capability.impl.NeutronCapability
import committee.nova.neutron.util.Utilities
import net.minecraftforge.common.capabilities.{Capability, CapabilityInject, CapabilityManager}
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event._
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.Logger

import java.util.concurrent.Callable

@Mod(modid = Neutron.MODID, useMetadata = true, modLanguage = "scala", acceptableRemoteVersions = "*")
object Neutron {
  var LOGGER: Logger = _
  final val MODID = "neutron"
  final val isServerSide = (e: FMLStateEvent) => e.getSide == Side.SERVER

  var neutronCapability: Capability[INeutronCapability] = _

  @CapabilityInject(classOf[INeutronCapability])
  def setCap(cap: Capability[INeutronCapability]): Unit = neutronCapability = cap

  @EventHandler def preInit(e: FMLPreInitializationEvent): Unit = {
    Utilities.Perm.initPermCompat()
    LOGGER = e.getModLog
    ServerConfig.init(e)
    val initialLang = Utilities.L10n.initializeL10n(ServerConfig.getLanguage)
    CapabilityManager.INSTANCE.register(classOf[INeutronCapability], new NeutronCapability.Storage, new Callable[INeutronCapability] {
      override def call(): INeutronCapability = new NeutronCapability.Impl
    })
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
    Utilities.Perm.loadPermCompat()
  }

  @EventHandler def postInit(e: FMLPostInitializationEvent): Unit = {}

  @EventHandler def onServerStarting(e: FMLServerStartingEvent): Unit = CommandInit.init(e)
}
