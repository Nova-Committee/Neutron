package committee.nova.neutron.server.command.impl

import committee.nova.neutron.implicits._
import committee.nova.neutron.server.command.base.CommandSingleArgPlayer
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.server.player.request.{TeleportHereRequest, TeleportToRequest}
import committee.nova.neutron.server.storage.ServerStorage
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.PermNodes
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.event.ClickEvent
import net.minecraft.util.{ChatStyle, EnumChatFormatting}

import java.util.UUID
import scala.util.Try
import scala.util.control.Breaks.{break, breakable}

object CommandTeleport {
  class Tpa extends CommandSingleArgPlayer {
    override def getCommandName: String = "tpa"

    override def getCommandUsage(sender: ICommandSender): String = "/tpa [UserName]"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 1) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        return
      }
      Utilities.Player.getPlayer(sender, args(0)) match {
        case Some(receiver) =>
          if (sender == receiver) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.self")
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
          }
          val cd = sender.getTpaCoolDown
          if (cd > 0) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.cd", String.valueOf(cd))
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
          }
          val request = new TeleportToRequest(sender.getUniqueID, receiver.getUniqueID, ServerConfig.getMaxTpExpirationTime)
          val sent = ServerStorage.addRequest(request)
          if (!sent) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.existed")
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
          }
          val id = request.getId.toString
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.requestSent", ServerConfig.getMaxTpExpirationTime).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.action.cancel").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY).setChatClickEvent(
            new ClickEvent(ClickEvent.Action.RUN_COMMAND, s"/tpcancel $id"))))
          receiver.addChatMessage(
            new ChatComponentServerTranslation("msg.neutron.cmd.tpa.request", sender.getCommandSenderName).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
          receiver.addChatMessage(Utilities.L10n.mergeComponent(
            new ChatComponentServerTranslation("msg.neutron.cmd.action.accept").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, s"/tpaccept $id"))),
            Utilities.L10n.getSpace,
            new ChatComponentServerTranslation("msg.neutron.cmd.action.deny").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, s"/tpdeny $id"))),
            Utilities.L10n.getSpace,
            new ChatComponentServerTranslation("msg.neutron.cmd.action.ignore").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, s"/tpignore $id")))
          ))
        case None => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.playerNotFound", args(0)).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
      }
    }

    override def filterName(name: String, player: EntityPlayerMP): Boolean = CommandSingleArgPlayer.filterSelf.apply(name, player)

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Tp.TO, _ => true)
      case _ => true
    }
  }

  class TpaHere extends CommandSingleArgPlayer {
    override def getCommandName: String = "tpahere"

    override def getCommandUsage(sender: ICommandSender): String = "/tpahere [UserName]"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 1) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        return
      }
      Utilities.Player.getPlayer(sender, args(0)) match {
        case Some(receiver) =>
          if (sender == receiver) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.self")
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
          }
          val cd = sender.getTpaCoolDown
          if (cd > 0) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.cd", String.valueOf(cd))
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
          }
          val request = new TeleportHereRequest(sender.getUniqueID, receiver.getUniqueID, ServerConfig.getMaxTpExpirationTime)
          val sent = ServerStorage.addRequest(request)
          if (!sent) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.existed")
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
          }
          sender.addChatMessage(
            new ChatComponentServerTranslation("msg.neutron.cmd.tp.requestSent", ServerConfig.getMaxTpExpirationTime).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.action.cancel").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY).setChatClickEvent(
            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpcancel"))))
          receiver.addChatMessage(
            new ChatComponentServerTranslation("msg.neutron.cmd.tpahere.request", sender.getCommandSenderName).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
          receiver.addChatMessage(Utilities.L10n.mergeComponent(
            new ChatComponentServerTranslation("msg.neutron.cmd.action.accept").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"))),
            Utilities.L10n.getSpace,
            new ChatComponentServerTranslation("msg.neutron.cmd.action.deny").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"))),
            Utilities.L10n.getSpace,
            new ChatComponentServerTranslation("msg.neutron.cmd.action.ignore").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpignore")))
          ))
        case None => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.playerNotFound").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
      }
    }

    override def filterName(name: String, player: EntityPlayerMP): Boolean = CommandSingleArgPlayer.filterSelf.apply(name, player)

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Tp.HERE, _ => true)
      case _ => true
    }
  }

  class TpCancel extends CommandBase {
    override def getCommandName: String = "tpcancel"

    override def getCommandUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString("/tpcancel", "/tpcancel [TPID]")

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      args.length match {
        case 0 =>
          var info = ""
          var success = false
          breakable {
            for (r <- ServerStorage.teleportRequestSet.toList.reverse if r.getSender == sender.getUniqueID) {
              val receiver = r.getReceiver
              info = r.getInfo
              success = ServerStorage.teleportRequestSet.remove(r)
              if (success && !r.wasIgnored) Utilities.Player.getPlayerByUUID(receiver).foreach(c => c.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.cancelled", info)
                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED))))
              break
            }
          }
          sender.addChatMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.tp.cancelled" else "msg.neutron.cmd.reply.tp.invalid",
            if (success) info else "")
            .setChatStyle(new ChatStyle().setColor(if (success) EnumChatFormatting.YELLOW else EnumChatFormatting.DARK_RED)))
        case 1 =>
          var success = false
          var info = ""
          breakable {
            for (r <- ServerStorage.teleportRequestSet if r.getSender == sender.getUniqueID && r.getId == Try(UUID.fromString(args(0))).getOrElse(null)) {
              val receiver = r.getReceiver
              info = r.getInfo
              success = ServerStorage.teleportRequestSet.remove(r)
              if (success && !r.wasIgnored) Utilities.Player.getPlayerByUUID(receiver).foreach(c => c.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.cancelled", info)
                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED))))
              break
            }
          }
          sender.addChatMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.tp.cancelled" else "msg.neutron.cmd.reply.tp.invalid",
            if (success) info else "")
            .setChatStyle(new ChatStyle().setColor(if (success) EnumChatFormatting.YELLOW else EnumChatFormatting.DARK_RED)))
        case _ =>
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
      }
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Tp.CANCEL, _ => true)
      case _ => true
    }
  }

  class TpAccept extends CommandBase {
    override def getCommandName: String = "tpaccept"

    override def getCommandUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString("/tpaccept", "/tpaccept [TPID]")

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]

      args.length match {
        case 0 =>
          var success = false
          var info = ""
          breakable {
            for (r <- ServerStorage.teleportRequestSet.toList.reverse if r.getReceiver == sender.getUniqueID) {
              info = r.getInfo
              success = r.apply
              if (success) Utilities.Player.getPlayerByUUID(r.getSender).foreach(c => {
                c.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.accepted", info)
                  .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
                c.setTpaCoolDown(ServerConfig.getTpCoolDown)
              })
              ServerStorage.teleportRequestSet.remove(r)
              break
            }
          }
          sender.addChatMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.tp.accepted" else "msg.neutron.cmd.reply.tp.invalid",
            if (success) info else "")
            .setChatStyle(new ChatStyle().setColor(if (success) EnumChatFormatting.GREEN else EnumChatFormatting.DARK_RED)))
        case 1 =>
          var success = false
          var info = ""
          breakable {
            for (r <- ServerStorage.teleportRequestSet if r.getReceiver == sender.getUniqueID && r.getId == Try(UUID.fromString(args(0))).getOrElse(null)) {
              info = r.getInfo
              success = r.apply
              if (success) Utilities.Player.getPlayerByUUID(r.getSender).foreach(c => {
                c.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.accepted", info)
                  .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
                c.setTpaCoolDown(ServerConfig.getTpCoolDown)
              })
              ServerStorage.teleportRequestSet.remove(r)
              break
            }
          }
          sender.addChatMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.tp.accepted" else "msg.neutron.cmd.reply.tp.invalid",
            if (success) info else "")
            .setChatStyle(new ChatStyle().setColor(if (success) EnumChatFormatting.GREEN else EnumChatFormatting.DARK_RED)))
        case _ =>
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
      }

    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Tp.ACCEPT, _ => true)
      case _ => true
    }
  }

  class TpDeny extends CommandBase {
    override def getCommandName: String = "tpdeny"

    override def getCommandUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString("/tpdeny", "/tpdeny [TPID]")

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      args.length match {
        case 0 =>
          var success = false
          var info = ""
          breakable {
            for (r <- ServerStorage.teleportRequestSet.toList.reverse if r.getReceiver == sender.getUniqueID) {
              val s = r.getSender
              info = r.getInfo
              success = ServerStorage.teleportRequestSet.remove(r)
              if (success) Utilities.Player.getPlayerByUUID(s).foreach(c => {
                c.setTpaCoolDown(ServerConfig.getTpCoolDown)
                c.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.denied", info)
                  .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
              })
              break
            }
          }
          sender.addChatMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.tp.denied" else "msg.neutron.cmd.reply.tp.invalid",
            if (success) info else "")
            .setChatStyle(new ChatStyle().setColor(if (success) EnumChatFormatting.YELLOW else EnumChatFormatting.DARK_RED)))
        case 1 =>
          var success = false
          var info = ""
          breakable {
            for (r <- ServerStorage.teleportRequestSet if r.getReceiver == sender.getUniqueID && r.getId == Try(UUID.fromString(args(0))).getOrElse(null)) {
              val s = r.getSender
              info = r.getInfo
              success = ServerStorage.teleportRequestSet.remove(r)
              if (success) Utilities.Player.getPlayerByUUID(s).foreach(c => {
                c.setTpaCoolDown(ServerConfig.getTpCoolDown)
                c.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.denied", info)
                  .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
              })
              break
            }
          }
          sender.addChatMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.tp.denied" else "msg.neutron.cmd.reply.tp.invalid",
            if (success) info else "")
            .setChatStyle(new ChatStyle().setColor(if (success) EnumChatFormatting.YELLOW else EnumChatFormatting.DARK_RED)))
        case _ => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
      }
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Tp.DENY, _ => true)
      case _ => true
    }
  }

  class TpIgnore extends CommandBase {
    override def getCommandName: String = "tpignore"

    override def getCommandUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString("/tpignore", "/tpignore [TPID]")

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      args.length match {
        case 0 =>
          var ignored = false
          var info = ""
          breakable {
            for (r <- ServerStorage.teleportRequestSet.toList.reverse if r.getReceiver == sender.getUniqueID && !r.wasIgnored) {
              info = r.getInfo
              r.setIgnored()
              ignored = true
              sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.ignored", info)
                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
              break
            }
          }
          if (!ignored) sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.invalid")
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
        case 1 =>
          var ignored = false
          var info = ""
          breakable {
            for (r <- ServerStorage.teleportRequestSet if (r.getReceiver == sender.getUniqueID && !r.wasIgnored
              && r.getId == Try(UUID.fromString(args(0))).getOrElse(null))) {
              info = r.getInfo
              r.setIgnored()
              ignored = true
              sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.ignored", info)
                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
              break
            }
          }
          if (!ignored) sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.invalid")
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
        case _ => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
      }

    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Tp.IGNORE, _ => true)
      case _ => true
    }
  }
}
