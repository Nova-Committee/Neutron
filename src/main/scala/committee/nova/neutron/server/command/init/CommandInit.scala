package committee.nova.neutron.server.command.init

import committee.nova.neutron.implicits.FMLServerStartingEventImplicit
import committee.nova.neutron.server.command.impl.CommandHome.{DelHome, Home, SetHome}
import committee.nova.neutron.server.command.impl.CommandNeutron
import committee.nova.neutron.server.command.impl.CommandTeleport._
import cpw.mods.fml.common.event.FMLServerStartingEvent

object CommandInit {
  def init(e: FMLServerStartingEvent): Unit = {
    e.registerServerCommands(
      new Tpa, new TpaHere, new TpCancel, new TpAccept, new TpDeny, new TpIgnore, new RTP,
      new CommandNeutron,
      new Home, new SetHome, new DelHome
    )
  }
}
