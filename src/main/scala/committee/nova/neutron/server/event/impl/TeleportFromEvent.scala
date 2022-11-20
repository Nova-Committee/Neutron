package committee.nova.neutron.server.event.impl

import cpw.mods.fml.common.eventhandler.Event
import net.minecraft.entity.player.EntityPlayerMP

case class TeleportFromEvent(player: EntityPlayerMP, oldDim: Int, oldX: Double, oldY: Double, oldZ: Double) extends Event