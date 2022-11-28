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
        case _ => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
      }
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean =
      !sender.isInstanceOf[EntityPlayerMP] || sender.asInstanceOf[EntityPlayerMP].isOp
  }

  class Mute extends CommandSingleArgPlayer {
    override def getCommandName: String = "mute"

    override def getCommandUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString("/mute [UserName] ([Reason])")

    override def processCommand(sender: ICommandSender, args: Array[String]): Unit = {
      if (args.length < 1 || args.length > 2) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        return
      }
      val target = Utilities.Player.getPlayer(sender, args(0))
      if (target.isEmpty) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.playerNotFound", args(0))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
        return
      }
      val targetPlayer = target.get
      val isConsole = !sender.isInstanceOf[EntityPlayerMP]
      if (targetPlayer.isOp && !isConsole) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.noPerm")
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
        return
      }
      val status = targetPlayer.getMuteStatus
      status.setApplied(true)
      status.setExecutedByConsole(isConsole)
      if (args.length == 2) status.setNote(args(1))
      targetPlayer.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.mute.acted", status.getNote)
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
      sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.mute.success", targetPlayer.getDisplayName, status.getNote)
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean =
      !sender.isInstanceOf[EntityPlayerMP] || sender.asInstanceOf[EntityPlayerMP].isOp
  }

  class Unmute extends CommandSingleArgPlayer {
    override def getCommandName: String = "unmute"

    override def getCommandUsage(sender: ICommandSender): String = "/unmute [UserName]"

    override def processCommand(sender: ICommandSender, args: Array[String]): Unit = {
      if (args.length != 1) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        return
      }
      val target = Utilities.Player.getPlayer(sender, args(0))
      if (target.isEmpty) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.playerNotFound", args(0))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
        return
      }
      val targetPlayer = target.get
      val status = targetPlayer.getMuteStatus
      if (status.isExecutedByConsole && sender.isInstanceOf[EntityPlayerMP]) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.noPerm")
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
        return
      }
      status.setApplied(false)
      status.setNote("")
      targetPlayer.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.unmute.acted")
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
      sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.unmute.success", targetPlayer.getDisplayName)
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
    }
  }
}
