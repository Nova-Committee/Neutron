package committee.nova.neutron.server.command.impl

import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import net.minecraft.command.{CommandBase, ICommandSender, WrongUsageException}

class CommandNeutron extends CommandBase {
  override def getCommandName: String = "neutron"

  override def getCommandUsage(sender: ICommandSender): String = "/neutron reload"

  override def processCommand(sender: ICommandSender, args: Array[String]): Unit = {
    if (args.length != 1) {

    }
    args(0) match {
      case "reload" => {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.reloading"))
        ServerConfig.sync()
      }
      case _ => throw new WrongUsageException("msg.neutron.cmd.wrongUsage")
    }
  }
}
