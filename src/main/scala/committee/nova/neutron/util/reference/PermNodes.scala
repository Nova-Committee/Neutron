package committee.nova.neutron.util.reference

import committee.nova.neutron.api.reference.Named

import scala.collection.mutable

object PermNodes {
  object Gui {
    case object INVSEE extends NeutronPerm("gui.invsee")

    case object CRAFT_GUI extends NeutronPerm("gui.craft")

    case object ENDERCHEST_GUI extends NeutronPerm("gui.enderchest")

    case object ANVIL_GUI extends NeutronPerm("gui.anvil")

    case object TRASHCAN extends NeutronPerm("gui.trashcan")
  }

  object Interactable {
    case object HOME_GUI extends NeutronPerm("interact.home")

    case object BACK_GUI extends NeutronPerm("interact.back")

    case object TPLIST_GUI extends NeutronPerm("interact.tplist")

    case object TPREQUEST_GUI extends NeutronPerm("interact.tprequest")
  }

  object Player {
    case object HEAL extends NeutronPerm("player.heal")

    case object SUICIDE extends NeutronPerm("player.suicide")

    case object MUTE extends NeutronPerm("player.mute")

    case object UNMUTE extends NeutronPerm("player.unmute")

    case object FLYSPEED extends NeutronPerm("player.speed.fly")

    case object WALKSPEED extends NeutronPerm("player.speed.walk")
  }

  object ItemStack {
    case object REPAIR_ONE extends NeutronPerm("item.repair.one")

    case object REPAIR_OTHER extends NeutronPerm("item.repair.other")

    case object REPAIR_ALL extends NeutronPerm("item.repair.all")
  }

  object Location {
    case object HOME extends NeutronPerm("loc.home.tp")

    case object SET_HOME extends NeutronPerm("loc.home.set")

    case object DEL_HOME extends NeutronPerm("loc.home.del")

    case object BACK extends NeutronPerm("loc.back")

    case object RTP extends NeutronPerm("loc.rtp")
  }

  object Tp {
    case object TO extends NeutronPerm("tp.ask.to")

    case object HERE extends NeutronPerm("tp.ask.here")

    case object CANCEL extends NeutronPerm("tp.cancel")

    case object ACCEPT extends NeutronPerm("tp.accept")

    case object DENY extends NeutronPerm("tp.deny")

    case object IGNORE extends NeutronPerm("tp.ignore")
  }

  object Miscs {
    case object HAT extends NeutronPerm("miscs.hat")
  }

  object Neutron {
    case object MANAGE extends NeutronPerm("manage")
  }

  sealed class NeutronPerm(name: String) extends Named(s"neutron.$name")

  final val COMMON_PERMS: Array[NeutronPerm] = mutable.Buffer(
    Gui.TRASHCAN, Gui.CRAFT_GUI,
    Interactable.BACK_GUI, Interactable.HOME_GUI, Interactable.TPLIST_GUI, Interactable.TPREQUEST_GUI,
    Player.SUICIDE,
    Location.HOME, Location.SET_HOME, Location.DEL_HOME, Location.BACK, Location.RTP,
    Tp.TO, Tp.HERE, Tp.CANCEL, Tp.ACCEPT, Tp.DENY, Tp.IGNORE,
    Miscs.HAT
  ).toArray

  final val MANAGER_PERMS: Array[NeutronPerm] = mutable.Buffer[NeutronPerm](
    Gui.ENDERCHEST_GUI, Gui.ANVIL_GUI, Gui.INVSEE,
    Player.HEAL, Player.MUTE, Player.UNMUTE, Player.FLYSPEED, Player.WALKSPEED,
    ItemStack.REPAIR_ONE, ItemStack.REPAIR_ALL, ItemStack.REPAIR_OTHER
  ).++=(COMMON_PERMS).toArray

  final val ADMIN_PERMS: Array[NeutronPerm] = mutable.Buffer[NeutronPerm](
    Neutron.MANAGE
  ).++=(MANAGER_PERMS).toArray
}
