package committee.nova.neutron.server.commands.base

import com.google.common.collect.ImmutableList
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.server.MinecraftServer

import java.util

abstract class CommandSingleArgPlayer extends CommandBase {
  override def addTabCompletionOptions(sender: ICommandSender, args: Array[String]): util.List[_] = {
    if (args.length != 1) return ImmutableList.of()
    CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer.getAllUsernames.toSeq: _*)
  }
}
