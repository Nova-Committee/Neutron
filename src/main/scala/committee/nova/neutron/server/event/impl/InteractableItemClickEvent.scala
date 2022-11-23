package committee.nova.neutron.server.event.impl

import cpw.mods.fml.common.eventhandler.Event
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack

case class InteractableItemClickEvent(player: EntityPlayerMP, stack: ItemStack) extends Event
