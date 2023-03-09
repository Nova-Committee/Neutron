package committee.nova.neutron.server.command.impl

import committee.nova.neutron.implicits._
import committee.nova.neutron.server.command.base.CommandSingleArgPlayer
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.server.ui.inventory.impl.InventoryTrashcan
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.PermNodes
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.{Style, TextFormatting}

object CommandGui {
  class InvSee extends CommandSingleArgPlayer {
    override def getName: String = "invsee"

    override def getUsage(sender: ICommandSender): String = "/invsee [UserName]"

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 1) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        return
      }
      Utilities.Player.getPlayer(server, sender, args(0)) match {
        case Some(target) =>
          if (sender != target) sender.displayGUIRemoteChest(target.inventory)
          else sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.invsee.self")
            .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        case None => sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.playerNotFound")
          .setStyle(new Style().setColor(TextFormatting.DARK_RED)))
      }
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Gui.INVSEE, p => p.isOp)
      case _ => false
    }

    override def filterName(name: String, player: EntityPlayerMP): Boolean = CommandSingleArgPlayer.filterSelf.apply(name, player)
  }

  class Craft extends CommandBase {
    override def getName: String = "craft"

    override def getUsage(sender: ICommandSender): String = "craft"

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 0) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        return
      }
      sender.displayGUIRemoteWorkbench()
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Gui.CRAFT_GUI, _ => true)
      case _ => true
    }
  }

  class EnderChest extends CommandBase {
    override def getName: String = "enderchest"

    override def getUsage(sender: ICommandSender): String = "/enderchest"

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 0) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        return
      }
      sender.displayGUIChest(sender.getInventoryEnderChest)
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Gui.ENDERCHEST_GUI, _ => true)
      case _ => true
    }
  }

  class Anvil extends CommandBase {
    override def getName: String = "anvil"

    override def getUsage(sender: ICommandSender): String = "/anvil"

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 0) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        return
      }
      sender.displayGUIRemoteAnvil()
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Gui.ANVIL_GUI, _ => true)
      case _ => true
    }
  }

  class Trashcan extends CommandBase {
    override def getName: String = "trashcan"

    override def getUsage(sender: ICommandSender): String = "/trashcan"

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      sender.displayGUIChest(new InventoryTrashcan)
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Gui.TRASHCAN, _ => true)
      case _ => true
    }
  }
}
