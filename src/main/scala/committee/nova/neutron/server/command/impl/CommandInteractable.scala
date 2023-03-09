package committee.nova.neutron.server.command.impl

import com.google.common.collect.ImmutableList
import committee.nova.neutron.implicits._
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.server.storage.ServerStorage
import committee.nova.neutron.server.ui.inventory.impl.{InventoryBack, InventoryHome, InventoryTpList, InventoryTpRequest}
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.PermNodes
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{Style, TextFormatting}

import java.util
import java.util.UUID
import scala.util.Try

object CommandInteractable {
  class HomeGui extends CommandBase {
    override def getName: String = "homegui"

    override def getUsage(p_71518_1_ : ICommandSender): String = Utilities.Str.convertStringArgsToString("/homegui", "/homegui [Page]")

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      args.length match {
        case 0 =>
          sender.displayGUIInteractable(new InventoryHome(sender, 1))
        case 1 =>
          val f = Try(Integer.parseInt(args(0)))
          if (f.isSuccess) {
            sender.displayGUIInteractable(new InventoryHome(sender, f.get))
            return
          }
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(0))
            .setStyle(new Style().setColor(TextFormatting.RED)))
        case _ => sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
      }
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Interactable.HOME_GUI, _ => true)
      case _ => true
    }
  }

  class BackGui extends CommandBase {
    override def getName: String = "backgui"

    override def getUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString("/backgui", "/backgui [Page]")

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      args.length match {
        case 0 =>
          sender.displayGUIInteractable(new InventoryBack(sender, 1))
        case 1 =>
          val f = Try(Integer.parseInt(args(0)))
          if (f.isSuccess) {
            sender.displayGUIInteractable(new InventoryBack(sender, f.get))
            return
          }
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(0))
            .setStyle(new Style().setColor(TextFormatting.RED)))
        case _ => sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
      }
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Interactable.BACK_GUI, _ => true)
      case _ => true
    }
  }

  object TpList {
    class All extends TpList("tplist", (s, p) => new InventoryTpList.All(s, p))

    class To extends TpList("tptlist", (s, p) => new InventoryTpList.To(s, p))

    class Here extends TpList("tphlist", (s, p) => new InventoryTpList.Here(s, p))

    class Sent extends TpList("tpslist", (s, p) => new InventoryTpList.Sent(s, p))

    class Received extends TpList("tprlist", (s, p) => new InventoryTpList.Received(s, p))
  }

  class TpList(command: String, gui: (EntityPlayerMP, Int) => InventoryTpList) extends CommandBase {
    override def getName: String = command

    override def getUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString(s"/$command", s"/$command [Page]")

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      args.length match {
        case 0 =>
          sender.displayGUIInteractable(gui.apply(sender, 1))
        case 1 =>
          Try(Integer.parseInt(args(0))).foreach(l => {
            sender.displayGUIInteractable(gui.apply(sender, l))
            return
          })
          // TODO:
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(0))
            .setStyle(new Style().setColor(TextFormatting.RED)))
        case _ => sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
      }
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Interactable.TPLIST_GUI, _ => true)
      case _ => true
    }
  }

  class TpRequest extends CommandBase {
    override def getName: String = "tprequest"

    override def getUsage(sender: ICommandSender): String = "/tprequest [TPID]"

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 1) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        return
      }
      Try(UUID.fromString(args(0))).foreach(uuid => {
        if (!ServerStorage.teleportRequestSet.exists(r => uuid == r.getId)) {
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reply.tp.invalid")
            .setStyle(new Style().setColor(TextFormatting.DARK_RED)))
          return
        }
        sender.displayGUIInteractable(new InventoryTpRequest(server, sender, uuid))
      })
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Interactable.TPREQUEST_GUI, _ => true)
      case _ => true
    }

    override def getTabCompletions(server: MinecraftServer, sender: ICommandSender, args: Array[String], pos: BlockPos): util.List[String] = {
      sender match {
        case p: EntityPlayerMP => CommandBase.getListOfStringsMatchingLastWord(args, ServerStorage.getRelevantRequestsOn(p).map(r => r.getId.toString).toSeq: _*)
        case _ => ImmutableList.of()
      }
    }
  }
}
