package committee.nova.neutron.server.command.base

import com.google.common.collect.ImmutableList
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos

import java.util

object CommandSingleArgPlayer {
  val filterSelf: (String, EntityPlayerMP) => Boolean = (s, p) => s == p.getName
}

abstract class CommandSingleArgPlayer extends CommandBase {
  override def getTabCompletions(server: MinecraftServer, sender: ICommandSender, args: Array[String], pos: BlockPos): util.List[String] = {
    if (args.length != 1 || !sender.isInstanceOf[EntityPlayerMP]) return ImmutableList.of()
    val player = sender.asInstanceOf[EntityPlayerMP]
    val buffer = server.getOnlinePlayerNames.toBuffer.++(getExtraCompletion(player, args))
    for (name <- buffer if filterName(name, player)) try {
      buffer.-=(name)
    } catch {
      case _: Exception =>
    }
    CommandBase.getListOfStringsMatchingLastWord(args, buffer: _*)
  }

  def filterName(name: String, player: EntityPlayerMP): Boolean

  def getExtraCompletion(sender: ICommandSender, args: Array[String]): Array[String] = Array()
}
