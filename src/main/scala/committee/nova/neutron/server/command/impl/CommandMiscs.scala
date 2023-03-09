package committee.nova.neutron.server.command.impl

import committee.nova.neutron.implicits._
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.PermNodes
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.{Style, TextFormatting}

object CommandMiscs {
  class Hat extends CommandBase {
    override def getName: String = "hat"

    override def getUsage(sender: ICommandSender): String = "/hat"

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length != 0) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        return
      }
      val helmet = sender.getItemStackFromSlot(EntityEquipmentSlot.HEAD)
      val inHand = sender.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND)
      sender.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, inHand)
      sender.setItemStackToSlot(EntityEquipmentSlot.HEAD, helmet)
      sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.hat"))
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Miscs.HAT, _ => true)
      case _ => true
    }
  }
}
