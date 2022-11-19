package committee.nova.neutron.server.commands.init

import committee.nova.neutron.implicits.FMLServerStartingEventImplicit
import committee.nova.neutron.server.commands.impl.CommandTeleport._
import cpw.mods.fml.common.event.FMLServerStartingEvent

object CommandInit {
  def init(e: FMLServerStartingEvent): Unit = {
    e.registerServerCommands(new Tpa, new TpaHere, new TpCancel, new TpAccept, new TpDeny, new TpIgnore)
  }
}
