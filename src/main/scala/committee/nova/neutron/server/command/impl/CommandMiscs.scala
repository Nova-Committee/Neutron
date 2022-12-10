package committee.nova.neutron.server.command.impl

import committee.nova.neutron.implicits._
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.PermNodes
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.{ChatStyle, EnumChatFormatting}

object CommandMiscs {
  class Hat extends CommandBase {
    override def getCommandName: String = "hat"

    override def getCommandUsage(sender: ICommandSender): String = "/hat"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 0) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        return
      }
      val helmet = sender.getEquipmentInSlot(4)
      val inHand = sender.getEquipmentInSlot(0)
      sender.setCurrentItemOrArmor(4, inHand)
      sender.setCurrentItemOrArmor(0, helmet)
      sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.hat"))
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Miscs.HAT, _ => true)
      case _ => true
    }
  }
}
