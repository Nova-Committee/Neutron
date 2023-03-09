package committee.nova.neutron.server.event.impl

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.Event

case class InteractableItemClickEvent(player: EntityPlayerMP, stack: ItemStack) extends Event
