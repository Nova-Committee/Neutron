package committee.nova.neutron.server.commands.impl

import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.server.player.request.TeleportToRequest
import committee.nova.neutron.server.storage.ServerStorage
import committee.nova.neutron.util.Utilities
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.event.ClickEvent
import net.minecraft.util.{ChatComponentText, ChatStyle, EnumChatFormatting}

import scala.util.control.Breaks.{break, breakable}

object CommandTeleport {
  class Tpa extends CommandBase {
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
        case Some(receiver) => {
          if (sender == receiver) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.cantTpYourSelf"))
            return
          }
          val cd = receiver.getTpaCoolDown
          if (cd > 0) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.cdTime", String.valueOf(cd)))
            return
          }
          val request = new TeleportToRequest(sender.getUniqueID, receiver.getUniqueID, 1200)
          val sent = ServerStorage.addRequest(request)
          if (!sent) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tp.existed"))
            return
          }
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tpa.success", 1200).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN))
            .appendSibling(new ChatComponentServerTranslation("msg.neutron.cmd.action.cancel").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY).setChatClickEvent(
              new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpcancel")
            ))))
          receiver.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.tpa.request", sender.getDisplayName).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW))
            .appendSibling(new ChatComponentServerTranslation("msg.neutron.cmd.action.agree").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"))))
            .appendSibling(new ChatComponentServerTranslation("msg.neutron.cmd.action.deny").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"))))
            .appendSibling(new ChatComponentServerTranslation("msg.neutron.cmd.action.ignore").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)
              .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpignore")))))
        }
        case None => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.playerNotFound"))
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
        for (r <- ServerStorage.teleportRequestSet if (r.getSender == sender.getUniqueID)) {
          success = ServerStorage.teleportRequestSet.remove(r)
          break()
        }
      }
      sender.addChatMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.uCancelled" else "msg.neutron.cmd.reply.cancel.invalid"))
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
        for (r <- ServerStorage.teleportRequestSet if (r.getReceiver == sender.getUniqueID)) {
          success = r.apply
          ServerStorage.teleportRequestSet.remove(r)
          break()
        }
      }
      sender.addChatMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.uAccepted" else "msg.neutron.cmd.reply.accept.invalid"))
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
        for (r <- ServerStorage.teleportRequestSet if (r.getReceiver == sender.getUniqueID)) {
          success = ServerStorage.teleportRequestSet.remove(r)
          break()
        }
      }
      sender.addChatMessage(new ChatComponentServerTranslation(if (success) "msg.neutron.cmd.reply.uDenied" else "msg.neutron.cmd.reply.deny.invalid"))
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
      for (r <- ServerStorage.teleportRequestSet if (r.getReceiver == sender.getUniqueID)) {
        r.setIgnored()
        val rp = Utilities.getPlayerByUUID(r.getSender)
        if (rp.isEmpty) return
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.ignore", rp.get.getDisplayName))
      }
    }
  }
}
