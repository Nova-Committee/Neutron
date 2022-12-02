package committee.nova.neutron.server.command.impl

import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.server.ui.inventory.impl.{InventoryBack, InventoryHome, InventoryTpList}
import committee.nova.neutron.util.Utilities
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.{ChatStyle, EnumChatFormatting}

import scala.util.Try

object CommandInteractable {
  class HomeGui extends CommandBase {
    override def getCommandName: String = "homegui"

    override def getCommandUsage(p_71518_1_ : ICommandSender): String = Utilities.Str.convertStringArgsToString("/homegui", "/homegui [Page]")

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
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
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(0))
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
        case _ => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
      }
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = true
  }

  class BackGui extends CommandBase {
    override def getCommandName: String = "backgui"

    override def getCommandUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString("/backgui", "/backgui [Page]")

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
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
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(0))
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
        case _ => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
      }
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = true
  }

  object TpList {
    class All extends TpList("tplist", (s, p) => new InventoryTpList.All(s, p))

    class To extends TpList("tptlist", (s, p) => new InventoryTpList.To(s, p))

    class Here extends TpList("tphlist", (s, p) => new InventoryTpList.Here(s, p))

    class Sent extends TpList("tpslist", (s, p) => new InventoryTpList.Sent(s, p))

    class Received extends TpList("tprlist", (s, p) => new InventoryTpList.Received(s, p))
  }

  class TpList(command: String, gui: (EntityPlayerMP, Int) => InventoryTpList) extends CommandBase {
    override def getCommandName: String = command

    override def getCommandUsage(sender: ICommandSender): String = Utilities.Str.convertStringArgsToString(s"/$command", s"/$command [Page]")

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      args.length match {
        case 0 =>
          sender.displayGUIInteractable(gui.apply(sender, 1))
        case 1 =>
          val f = Try(Integer.parseInt(args(0)))
          if (f.isSuccess) {
            sender.displayGUIInteractable(gui.apply(sender, f.get))
            return
          }
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(0))
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
        case _ => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
      }
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = true
  }
}
