package committee.nova.neutron.server.command.impl

import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import net.minecraft.command.{CommandBase, ICommandSender, WrongUsageException}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.{ChatStyle, EnumChatFormatting}

class CommandNeutron extends CommandBase {
  override def getCommandName: String = "neutron"

  override def getCommandUsage(sender: ICommandSender): String = "/neutron reload"

  override def processCommand(sender: ICommandSender, args: Array[String]): Unit = {
    if (args.length != 1) {

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
      case p: EntityPlayerMP => MinecraftServer.getServer.getConfigurationManager.func_152596_g(p.getGameProfile)
      case _ => true
    }
  }
}
