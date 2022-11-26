package committee.nova.neutron.server.command.impl

import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.command.base.CommandSingleArgPlayer
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.util.Utilities
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.{ChatStyle, EnumChatFormatting}

object CommandPlayer {
  class Heal extends CommandBase {
    override def getCommandName: String = "heal"

    override def getCommandUsage(p_71518_1_ : ICommandSender): String = Utilities.Str.convertStringArgsToString("/heal", "/heal [UserName]")

    override def processCommand(sender: ICommandSender, args: Array[String]): Unit = {
      args.length match {
        case 0 =>
          sender match {
            case p: EntityPlayerMP => p.healAndFeed()
            case _ =>
          }
        case 1 =>
          Utilities.Player.getPlayer(sender, args(0)).foreach(p => {
            p.healAndFeed()
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.heal.success", p.getDisplayName)
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
            return
          })
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.playerNotFound", args(0))
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
      }
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean =
      !sender.isInstanceOf[EntityPlayerMP] || sender.asInstanceOf[EntityPlayerMP].isOp
  }

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
        case Some(target) => {
          if (sender != target) sender.displayGUIChest(target.inventory)
          else sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.invsee.self")
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        }
        case None => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.playerNotFound")
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
      }
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => p.isOp
      case _ => false
    }
  }
}
