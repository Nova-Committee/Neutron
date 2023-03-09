package committee.nova.neutron.server.event.impl

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.eventhandler.Event

case class TeleportFromEvent(player: EntityPlayerMP, oldDim: Int, oldX: Double, oldY: Double, oldZ: Double) extends Event