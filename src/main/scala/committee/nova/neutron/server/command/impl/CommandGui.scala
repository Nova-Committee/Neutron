package committee.nova.neutron.server.command.impl

import committee.nova.neutron.implicits._
import committee.nova.neutron.server.command.base.CommandSingleArgPlayer
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.server.ui.inventory.impl.InventoryTrashcan
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.PermNodes
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.{ChatStyle, EnumChatFormatting}

object CommandGui {
  class InvSee extends CommandSingleArgPlayer {
    override def getCommandName: String = "invsee"

    override def getCommandUsage(sender: ICommandSender): String = "/invsee [UserName]"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 1) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        return
      }
      Utilities.Player.getPlayer(sender, args(0)) match {
        case Some(target) =>
          if (sender != target) sender.displayGUIRemoteChest(target.inventory)
          else sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.invsee.self")
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        case None => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.playerNotFound")
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
      }
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Gui.INVSEE, p => p.isOp)
      case _ => false
    }

    override def filterName(name: String, player: EntityPlayerMP): Boolean = CommandSingleArgPlayer.filterSelf.apply(name, player)
  }

  class Craft extends CommandBase {
    override def getCommandName: String = "craft"

    override def getCommandUsage(sender: ICommandSender): String = "craft"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 0) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        return
      }
      sender.displayGUIRemoteWorkbench()
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Gui.CRAFT_GUI, _ => true)
      case _ => true
    }
  }

  class EnderChest extends CommandBase {
    override def getCommandName: String = "enderchest"

    override def getCommandUsage(sender: ICommandSender): String = "/enderchest"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 0) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        return
      }
      sender.displayGUIChest(sender.getInventoryEnderChest)
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Gui.ENDERCHEST_GUI, _ => true)
      case _ => true
    }
  }

  class Anvil extends CommandBase {
    override def getCommandName: String = "anvil"

    override def getCommandUsage(sender: ICommandSender): String = "/anvil"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 0) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        return
      }
      sender.displayGUIRemoteAnvil()
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Gui.ANVIL_GUI, _ => true)
      case _ => true
    }
  }

  class Trashcan extends CommandBase {
    override def getCommandName: String = "trashcan"

    override def getCommandUsage(sender: ICommandSender): String = "/trashcan"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]

      sender.displayGUIChest(new InventoryTrashcan(sender))
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Gui.TRASHCAN, _ => true)
      case _ => true
    }
  }
}
