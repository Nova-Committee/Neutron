package committee.nova.neutron.server.command.impl

import com.google.common.collect.ImmutableList
import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import net.minecraft.command.{CommandBase, ICommandSender, WrongUsageException}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.{ChatStyle, EnumChatFormatting}

import java.util

class CommandNeutron extends CommandBase {
  override def getCommandName: String = "neutron"

  override def getCommandUsage(sender: ICommandSender): String = "/neutron reload"

  override def processCommand(sender: ICommandSender, args: Array[String]): Unit = {
    if (args.length != 1) {
      sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
      return
    }
    args(0) match {
      case "reload" =>
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reload.inProgress")
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        try {
          ServerConfig.sync()
        } catch {
          case e: Exception =>
            e.printStackTrace()
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reload.error")
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
            return
        }
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reload.success")
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
      case _ => throw new WrongUsageException("msg.neutron.cmd.wrongUsage")
    }
  }

  override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = {
    sender match {
      case p: EntityPlayerMP => p.isOp
      case _ => true
    }
  }

  override def addTabCompletionOptions(sender: ICommandSender, args: Array[String]): util.List[_] = {
    if (args.length != 1 || !sender.isInstanceOf[EntityPlayerMP]) ImmutableList.of() else ImmutableList.of("reload")
  }
}
