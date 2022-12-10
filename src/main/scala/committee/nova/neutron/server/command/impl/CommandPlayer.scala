package committee.nova.neutron.server.command.impl

import committee.nova.neutron.implicits._
import committee.nova.neutron.server.command.base.CommandSingleArgPlayer
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.PermNodes
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.{ChatStyle, EnumChatFormatting}

import java.lang.{Float => JFloat}
import scala.util.Try

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
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.heal.success", p.getCommandSenderName)
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
            return
          })
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.playerNotFound", args(0))
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
        case _ => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
      }
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Player.HEAL, _ => p.isOp)
      case _ => true
    }
  }

  class Suicide extends CommandBase {
    override def getCommandName: String = "suicide"

    override def getCommandUsage(sender: ICommandSender): String = "suicide"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (ServerConfig.shouldKeepStatsAfterSuicide) {
        val suicide = sender.getStatsBeforeSuicide
        suicide.setValid(true)
        suicide.setHealth(sender.getHealth)
        suicide.setFoodLevel(sender.getFoodStats.getFoodLevel)
        suicide.setSaturation(sender.getFoodStats.getSaturationLevel)
      }
      sender.attackEntityFrom(Utilities.Player.suicide, Float.MaxValue)
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Player.SUICIDE, _ => true)
      case _ => true
    }
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
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.playerNotFound", args(0))
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
      sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.mute.success", targetPlayer.getCommandSenderName, status.getNote)
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
    }

    override def filterName(name: String, player: EntityPlayerMP): Boolean = false

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Player.MUTE, _ => p.isOp)
      case _ => true
    }
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
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.playerNotFound", args(0))
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
      sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.unmute.success", targetPlayer.getCommandSenderName)
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
    }

    override def filterName(name: String, player: EntityPlayerMP): Boolean = false

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Player.UNMUTE, _ => p.isOp)
      case _ => true
    }
  }

  class FlySpeed extends CommandSingleArgPlayer {
    override def getCommandName: String = "flyspeed"

    override def getCommandUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString(
      "/flyspeed [UserName] [Multiplier]",
      if (sender.isInstanceOf[EntityPlayerMP]) "/flyspeed [Multiplier]" else "",
      if (sender.isInstanceOf[EntityPlayerMP]) "/flyspeed" else ""
    )

    override def processCommand(sender: ICommandSender, args: Array[String]): Unit = {
      args.length match {
        case 0 => sender match {
          case player: EntityPlayerMP => player.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.flySpeed.current", player.capabilities.flySpeed / 0.05F)
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
          case _ =>
        }
        case 1 =>
          sender match {
            case player: EntityPlayerMP =>
              val multiplier = Try(JFloat.parseFloat(args(0)) * 0.05F)
              if (multiplier.isFailure) {
                player.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(0))
                  .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
                return
              }
              player.setFlySpeed(multiplier.get)
            case _ =>
          }
        case 2 =>
          val target = Utilities.Player.getPlayer(sender, args(0))
          if (target.isEmpty) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.playerNotFound", args(0))
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
            return
          }
          val multiplier = Try(JFloat.parseFloat(args(1)) * 0.05F)
          if (multiplier.isFailure) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(0))
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
          }
          target.get.setFlySpeed(multiplier.get)
      }
    }

    override def filterName(name: String, player: EntityPlayerMP): Boolean = false

    override def getExtraCompletion(sender: ICommandSender, args: Array[String]): Array[String] = if (args.length == 1 || args.length == 2)
      Array("0.5", "1.0", "1.5", "2.0") else Array()

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Player.FLYSPEED, _ => p.isOp)
      case _ => true
    }
  }

  class WalkSpeed extends CommandSingleArgPlayer {
    override def getCommandName: String = "walkspeed"

    override def getCommandUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString(
      "/walkspeed [UserName] [Multiplier]",
      if (sender.isInstanceOf[EntityPlayerMP]) "/walkspeed [Multiplier]" else "",
      if (sender.isInstanceOf[EntityPlayerMP]) "/walkspeed" else ""
    )

    override def processCommand(sender: ICommandSender, args: Array[String]): Unit = {
      args.length match {
        case 0 => sender match {
          case player: EntityPlayerMP => player.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.walkSpeed.current", player.capabilities.walkSpeed / 0.1F)
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
          case _ =>
        }
        case 1 =>
          sender match {
            case player: EntityPlayerMP =>
              val multiplier = Try(JFloat.parseFloat(args(0)) * 0.1F)
              if (multiplier.isFailure) {
                player.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(0))
                  .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
                return
              }
              player.setWalkSpeed(multiplier.get)
            case _ =>
          }
        case 2 =>
          val target = Utilities.Player.getPlayer(sender, args(0))
          if (target.isEmpty) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.playerNotFound", args(0))
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
            return
          }
          val multiplier = Try(JFloat.parseFloat(args(1)) * 0.1F)
          if (multiplier.isFailure) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(0))
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
          }
          target.get.setWalkSpeed(multiplier.get)
      }
    }

    override def filterName(name: String, player: EntityPlayerMP): Boolean = false

    override def getExtraCompletion(sender: ICommandSender, args: Array[String]): Array[String] = if (args.length == 1 || args.length == 2)
      Array("0.5", "1.0", "1.5", "2.0") else Array()

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Player.WALKSPEED, _ => p.isOp)
      case _ => true
    }
  }
}
