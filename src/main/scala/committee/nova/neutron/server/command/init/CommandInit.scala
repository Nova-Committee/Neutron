package committee.nova.neutron.server.command.init

import committee.nova.neutron.implicits.FMLServerStartingEventImplicit
import committee.nova.neutron.server.command.impl.CommandGui._
import committee.nova.neutron.server.command.impl.CommandInteractable._
import committee.nova.neutron.server.command.impl.CommandItemStack._
import committee.nova.neutron.server.command.impl.CommandLocation._
import committee.nova.neutron.server.command.impl.CommandMiscs._
import committee.nova.neutron.server.command.impl.CommandNeutron
import committee.nova.neutron.server.command.impl.CommandPlayer._
import committee.nova.neutron.server.command.impl.CommandTeleport._
import cpw.mods.fml.common.event.FMLServerStartingEvent
import net.minecraft.command.ICommand

import scala.collection.mutable

object CommandInit {
  val commands: mutable.LinkedHashSet[ICommand] = new mutable.LinkedHashSet[ICommand]()

  def init(e: FMLServerStartingEvent): Unit = {
    e.registerServerCommands(
      new Tpa, new TpaHere, new TpCancel, new TpAccept, new TpDeny, new TpIgnore, new Rtp,
      new CommandNeutron,
      new Home, new SetHome, new DelHome, new Back,
      new Hat, new Trashcan,
      new HomeGui, new BackGui,
      new Heal, new Suicide, new Mute, new Unmute, new FlySpeed, new WalkSpeed,
      new InvSee, new Craft, new EnderChest, new Anvil,
      new Repair
    )
  }
}
