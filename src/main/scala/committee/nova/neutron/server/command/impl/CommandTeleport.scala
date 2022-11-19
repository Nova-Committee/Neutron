package committee.nova.neutron.server.command.impl

import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.command.base.CommandSingleArgPlayer
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.server.player.request.{TeleportHereRequest, TeleportToRequest}
import committee.nova.neutron.server.storage.ServerStorage
import committee.nova.neutron.util.Utilities
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.event.ClickEvent
import net.minecraft.util.{ChatComponentText, ChatStyle, EnumChatFormatting}

import scala.util.control.Breaks.{break, breakable}

object CommandTeleport {
  class Tpa extends CommandSingleArgPlayer {
    override def getCommandName: String = "tpa"

    override def getCommandUsage(sender: ICommandSender): String = "/tpa [UserName]"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 1) {
        sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)))
        return
      }
      Utilities.getPlayer(sender, args(0)) match {
        case Some(receiver) =>
          if (sender == receiver) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.cantTpYourSelf")
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
          }
          val cd = receiver.getTpaCoolDown
          if (cd > 0) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.cdTime", String.valueOf(cd))
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
          }
          val request = new TeleportToRequest(sender.getUniqueID, receiver.getUniqueID, ServerConfig.getMaxTPExpirationTime)
          val sent = ServerStorage.addRequest(request)
          if (!sent) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.existed")
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
          }
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.requestSent", ServerConfig.getMaxTPExpirationTime).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
          sender.addChatMessage(Utilities.getEmpty
            .appendSibling(new ChatComponentServerTranslation("msg.neutron.cmd.action.cancel").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY).setChatClickEvent(
              new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpcancel")
            ))))
          receiver.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tpa.request", sender.getDisplayName).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
          receiver.addChatMessage(Utilities.getEmpty
            .appendSibling(new ChatComponentServerTranslation("msg.neutron.cmd.action.accept").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"))))
            .appendSibling(Utilities.getSpace)
            .appendSibling(new ChatComponentServerTranslation("msg.neutron.cmd.action.deny").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"))))
            .appendSibling(Utilities.getSpace)
            .appendSibling(new ChatComponentServerTranslation("msg.neutron.cmd.action.ignore").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpignore")))))
        case None => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.playerNotFound").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
      }
    }
  }

  class TpaHere extends CommandSingleArgPlayer {
    override def getCommandName: String = "tpahere"

    override def getCommandUsage(sender: ICommandSender): String = "/tpahere [UserName]"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 1) {
        sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)))
        return
      }
      Utilities.getPlayer(sender, args(0)) match {
        case Some(receiver) =>
          if (sender == receiver) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.cantTpYourSelf")
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
          }
          val cd = receiver.getTpaCoolDown
          if (cd > 0) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.cdTime", String.valueOf(cd))
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
          }
          val request = new TeleportHereRequest(sender.getUniqueID, receiver.getUniqueID, ServerConfig.getMaxTPExpirationTime)
          val sent = ServerStorage.addRequest(request)
          if (!sent) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.existed")
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
          }
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.requestSent", ServerConfig.getMaxTPExpirationTime).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
          sender.addChatMessage(Utilities.getEmpty
            .appendSibling(new ChatComponentServerTranslation("msg.neutron.cmd.action.cancel").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY).setChatClickEvent(
              new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpcancel")
            ))))
          receiver.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tpahere.request", sender.getDisplayName).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
          receiver.addChatMessage(Utilities.getEmpty
            .appendSibling(new ChatComponentServerTranslation("msg.neutron.cmd.action.accept").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"))))
            .appendSibling(Utilities.getSpace)
            .appendSibling(new ChatComponentServerTranslation("msg.neutron.cmd.action.deny").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"))))
            .appendSibling(Utilities.getSpace)
            .appendSibling(new ChatComponentServerTranslation("msg.neutron.cmd.action.ignore").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpignore")))))
        case None => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.playerNotFound").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
      }
    }
  }

  class TpCancel extends CommandBase {
    override def getCommandName: String = "tpcancel"

    override def getCommandUsage(sender: ICommandSender): String = "/tpcancel"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 0) {
        sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)))
        return
      }
      var success = false
      breakable {
        for (r <- ServerStorage.teleportRequestSet if r.getSender == sender.getUniqueID) {
          val receiver = r.getReceiver
          success = ServerStorage.teleportRequestSet.remove(r)
          if (success) Utilities.getPlayerByUUID(receiver).foreach(c => c.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.cancelled")
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED))))
          break()
        }
      }
      sender.addChatMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.cancelled" else "msg.neutron.cmd.reply.invalid")
        .setChatStyle(new ChatStyle().setColor(if (success) EnumChatFormatting.YELLOW else EnumChatFormatting.DARK_RED)))
    }
  }

  class TpAccept extends CommandBase {
    override def getCommandName: String = "tpaccept"

    override def getCommandUsage(sender: ICommandSender): String = "/tpaccept"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 0) {
        sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)))
        return
      }
      var success = false
      breakable {
        for (r <- ServerStorage.teleportRequestSet if r.getReceiver == sender.getUniqueID) {
          success = r.apply
          if (success) Utilities.getPlayerByUUID(r.getSender).foreach(c => c.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.accepted")
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN))))
          // TODO:
          ServerStorage.teleportRequestSet.remove(r)
          break()
        }
      }
      sender.addChatMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.accepted" else "msg.neutron.cmd.reply.invalid")
        .setChatStyle(new ChatStyle().setColor(if (success) EnumChatFormatting.GREEN else EnumChatFormatting.DARK_RED)))
    }
  }

  class TpDeny extends CommandBase {
    override def getCommandName: String = "tpdeny"

    override def getCommandUsage(sender: ICommandSender): String = "/tpdeny"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 0) {
        sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)))
        return
      }
      var success = false
      breakable {
        for (r <- ServerStorage.teleportRequestSet if r.getReceiver == sender.getUniqueID) {
          val s = r.getSender
          success = ServerStorage.teleportRequestSet.remove(r)
          if (success) Utilities.getPlayerByUUID(s).foreach(c => c.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.denied")
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))))
          break()
        }
      }
      sender.addChatMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.denied" else "msg.neutron.cmd.reply.invalid")
        .setChatStyle(new ChatStyle().setColor(if (success) EnumChatFormatting.YELLOW else EnumChatFormatting.DARK_RED)))
    }
  }

  class TpIgnore extends CommandBase {
    override def getCommandName: String = "tpignore"

    override def getCommandUsage(sender: ICommandSender): String = "/tpignore"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 0) {
        sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)))
        return
      }
      var ignored = false
      breakable {
        for (r <- ServerStorage.teleportRequestSet if r.getReceiver == sender.getUniqueID && !r.getIgnored) {
          r.setIgnored()
          ignored = true
          val rp = Utilities.getPlayerByUUID(r.getSender)
          if (rp.isEmpty) return
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.ignored", rp.get.getDisplayName)
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
          break()
        }
      }
      if (!ignored) sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.invalid")
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
    }
  }
}
