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
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.event.ClickEvent
import net.minecraft.util.text.{Style, TextFormatting}

import java.util.UUID
import scala.util.Try
import scala.util.control.Breaks.{break, breakable}

object CommandTeleport {
  class Tpa extends CommandSingleArgPlayer {
    override def getName: String = "tpa"

    override def getUsage(sender: ICommandSender): String = "/tpa [UserName]"

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 1) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        return
      }
      Utilities.Player.getPlayer(server, sender, args(0)) match {
        case Some(receiver) =>
          if (sender == receiver) {
            sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.self")
              .setStyle(new Style().setColor(TextFormatting.RED)))
            return
          }
          val cd = sender.getTpaCoolDown
          if (cd > 0) {
            sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.cd", String.valueOf(cd))
              .setStyle(new Style().setColor(TextFormatting.RED)))
            return
          }
          val request = new TeleportToRequest(server, sender.getUniqueID, receiver.getUniqueID, ServerConfig.getMaxTpExpirationTime)
          val sent = ServerStorage.addRequest(request)
          if (!sent) {
            sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.existed")
              .setStyle(new Style().setColor(TextFormatting.RED)))
            return
          }
          val id = request.getId.toString
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.requestSent", ServerConfig.getMaxTpExpirationTime).setStyle(new Style().setColor(TextFormatting.GREEN)))
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.action.cancel").setStyle(new Style().setColor(TextFormatting.GRAY).setClickEvent(
            new ClickEvent(ClickEvent.Action.RUN_COMMAND, s"/tpcancel $id"))))
          receiver.sendMessage(
            new ChatComponentServerTranslation("msg.neutron.cmd.tpa.request", sender.getName).setStyle(new Style().setColor(TextFormatting.YELLOW)))
          receiver.sendMessage(Utilities.L10n.mergeComponent(
            new ChatComponentServerTranslation("msg.neutron.cmd.action.accept").setStyle(new Style().setColor(TextFormatting.GREEN)
              .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, s"/tpaccept $id"))),
            Utilities.L10n.getSpace,
            new ChatComponentServerTranslation("msg.neutron.cmd.action.deny").setStyle(new Style().setColor(TextFormatting.RED)
              .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, s"/tpdeny $id"))),
            Utilities.L10n.getSpace,
            new ChatComponentServerTranslation("msg.neutron.cmd.action.ignore").setStyle(new Style().setColor(TextFormatting.GRAY)
              .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, s"/tpignore $id")))
          ))
        case None => sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.playerNotFound", args(0)).setStyle(new Style().setColor(TextFormatting.DARK_RED)))
      }
    }

    override def filterName(name: String, player: EntityPlayerMP): Boolean = CommandSingleArgPlayer.filterSelf.apply(name, player)

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Tp.TO, _ => true)
      case _ => true
    }
  }

  class TpaHere extends CommandSingleArgPlayer {
    override def getName: String = "tpahere"

    override def getUsage(sender: ICommandSender): String = "/tpahere [UserName]"

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 1) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        return
      }
      Utilities.Player.getPlayer(server, sender, args(0)) match {
        case Some(receiver) =>
          if (sender == receiver) {
            sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.self")
              .setStyle(new Style().setColor(TextFormatting.RED)))
            return
          }
          val cd = sender.getTpaCoolDown
          if (cd > 0) {
            sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.cd", String.valueOf(cd))
              .setStyle(new Style().setColor(TextFormatting.RED)))
            return
          }
          val request = new TeleportHereRequest(server, sender.getUniqueID, receiver.getUniqueID, ServerConfig.getMaxTpExpirationTime)
          val sent = ServerStorage.addRequest(request)
          if (!sent) {
            sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.existed")
              .setStyle(new Style().setColor(TextFormatting.RED)))
            return
          }
          sender.sendMessage(
            new ChatComponentServerTranslation("msg.neutron.cmd.tp.requestSent", ServerConfig.getMaxTpExpirationTime).setStyle(new Style().setColor(TextFormatting.GREEN)))
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.action.cancel").setStyle(new Style().setColor(TextFormatting.GRAY).setClickEvent(
            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpcancel"))))
          receiver.sendMessage(
            new ChatComponentServerTranslation("msg.neutron.cmd.tpahere.request", sender.getName).setStyle(new Style().setColor(TextFormatting.YELLOW)))
          receiver.sendMessage(Utilities.L10n.mergeComponent(
            new ChatComponentServerTranslation("msg.neutron.cmd.action.accept").setStyle(new Style().setColor(TextFormatting.GREEN)
              .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"))),
            Utilities.L10n.getSpace,
            new ChatComponentServerTranslation("msg.neutron.cmd.action.deny").setStyle(new Style().setColor(TextFormatting.RED)
              .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"))),
            Utilities.L10n.getSpace,
            new ChatComponentServerTranslation("msg.neutron.cmd.action.ignore").setStyle(new Style().setColor(TextFormatting.GRAY)
              .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpignore")))
          ))
        case None => sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.playerNotFound").setStyle(new Style().setColor(TextFormatting.DARK_RED)))
      }
    }

    override def filterName(name: String, player: EntityPlayerMP): Boolean = CommandSingleArgPlayer.filterSelf.apply(name, player)

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Tp.HERE, _ => true)
      case _ => true
    }
  }

  class TpCancel extends CommandBase {
    override def getName: String = "tpcancel"

    override def getUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString("/tpcancel", "/tpcancel [TPID]")

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
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
              if (success && !r.wasIgnored) Utilities.Player.getPlayerByUUID(server, receiver).foreach(c => c.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.cancelled", info)
                .setStyle(new Style().setColor(TextFormatting.DARK_RED))))
              break
            }
          }
          sender.sendMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.tp.cancelled" else "msg.neutron.cmd.reply.tp.invalid",
            if (success) info else "")
            .setStyle(new Style().setColor(if (success) TextFormatting.YELLOW else TextFormatting.DARK_RED)))
        case 1 =>
          var success = false
          var info = ""
          breakable {
            for (r <- ServerStorage.teleportRequestSet if r.getSender == sender.getUniqueID && r.getId == Try(UUID.fromString(args(0))).getOrElse(null)) {
              val receiver = r.getReceiver
              info = r.getInfo
              success = ServerStorage.teleportRequestSet.remove(r)
              if (success && !r.wasIgnored) Utilities.Player.getPlayerByUUID(server, receiver).foreach(c => c.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.cancelled", info)
                .setStyle(new Style().setColor(TextFormatting.DARK_RED))))
              break
            }
          }
          sender.sendMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.tp.cancelled" else "msg.neutron.cmd.reply.tp.invalid",
            if (success) info else "")
            .setStyle(new Style().setColor(if (success) TextFormatting.YELLOW else TextFormatting.DARK_RED)))
        case _ =>
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
            .setStyle(new Style().setColor(TextFormatting.YELLOW)))
      }
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Tp.CANCEL, _ => true)
      case _ => true
    }
  }

  class TpAccept extends CommandBase {
    override def getName: String = "tpaccept"

    override def getUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString("/tpaccept", "/tpaccept [TPID]")

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
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
              if (success) Utilities.Player.getPlayerByUUID(server, r.getSender).foreach(c => {
                c.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.accepted", info)
                  .setStyle(new Style().setColor(TextFormatting.GREEN)))
                c.setTpaCoolDown(ServerConfig.getTpCoolDown)
              })
              ServerStorage.teleportRequestSet.remove(r)
              break
            }
          }
          sender.sendMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.tp.accepted" else "msg.neutron.cmd.reply.tp.invalid",
            if (success) info else "")
            .setStyle(new Style().setColor(if (success) TextFormatting.GREEN else TextFormatting.DARK_RED)))
        case 1 =>
          var success = false
          var info = ""
          breakable {
            for (r <- ServerStorage.teleportRequestSet if r.getReceiver == sender.getUniqueID && r.getId == Try(UUID.fromString(args(0))).getOrElse(null)) {
              info = r.getInfo
              success = r.apply
              if (success) Utilities.Player.getPlayerByUUID(server, r.getSender).foreach(c => {
                c.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.accepted", info)
                  .setStyle(new Style().setColor(TextFormatting.GREEN)))
                c.setTpaCoolDown(ServerConfig.getTpCoolDown)
              })
              ServerStorage.teleportRequestSet.remove(r)
              break
            }
          }
          sender.sendMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.tp.accepted" else "msg.neutron.cmd.reply.tp.invalid",
            if (success) info else "")
            .setStyle(new Style().setColor(if (success) TextFormatting.GREEN else TextFormatting.DARK_RED)))
        case _ =>
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
            .setStyle(new Style().setColor(TextFormatting.YELLOW)))
      }

    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Tp.ACCEPT, _ => true)
      case _ => true
    }
  }

  class TpDeny extends CommandBase {
    override def getName: String = "tpdeny"

    override def getUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString("/tpdeny", "/tpdeny [TPID]")

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
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
              if (success) Utilities.Player.getPlayerByUUID(server, s).foreach(c => {
                c.setTpaCoolDown(ServerConfig.getTpCoolDown)
                c.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.denied", info)
                  .setStyle(new Style().setColor(TextFormatting.RED)))
              })
              break
            }
          }
          sender.sendMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.tp.denied" else "msg.neutron.cmd.reply.tp.invalid",
            if (success) info else "")
            .setStyle(new Style().setColor(if (success) TextFormatting.YELLOW else TextFormatting.DARK_RED)))
        case 1 =>
          var success = false
          var info = ""
          breakable {
            for (r <- ServerStorage.teleportRequestSet if r.getReceiver == sender.getUniqueID && r.getId == Try(UUID.fromString(args(0))).getOrElse(null)) {
              val s = r.getSender
              info = r.getInfo
              success = ServerStorage.teleportRequestSet.remove(r)
              if (success) Utilities.Player.getPlayerByUUID(server, s).foreach(c => {
                c.setTpaCoolDown(ServerConfig.getTpCoolDown)
                c.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.denied", info)
                  .setStyle(new Style().setColor(TextFormatting.RED)))
              })
              break
            }
          }
          sender.sendMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.tp.denied" else "msg.neutron.cmd.reply.tp.invalid",
            if (success) info else "")
            .setStyle(new Style().setColor(if (success) TextFormatting.YELLOW else TextFormatting.DARK_RED)))
        case _ => sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
      }
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Tp.DENY, _ => true)
      case _ => true
    }
  }

  class TpIgnore extends CommandBase {
    override def getName: String = "tpignore"

    override def getUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString("/tpignore", "/tpignore [TPID]")

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
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
              sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.ignored", info)
                .setStyle(new Style().setColor(TextFormatting.YELLOW)))
              break
            }
          }
          if (!ignored) sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.invalid")
            .setStyle(new Style().setColor(TextFormatting.DARK_RED)))
        case 1 =>
          var ignored = false
          var info = ""
          breakable {
            for (r <- ServerStorage.teleportRequestSet if (r.getReceiver == sender.getUniqueID && !r.wasIgnored
              && r.getId == Try(UUID.fromString(args(0))).getOrElse(null))) {
              info = r.getInfo
              r.setIgnored()
              ignored = true
              sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.ignored", info)
                .setStyle(new Style().setColor(TextFormatting.YELLOW)))
              break
            }
          }
          if (!ignored) sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.invalid")
            .setStyle(new Style().setColor(TextFormatting.DARK_RED)))
        case _ => sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
      }

    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Tp.IGNORE, _ => true)
      case _ => true
    }
  }
}
