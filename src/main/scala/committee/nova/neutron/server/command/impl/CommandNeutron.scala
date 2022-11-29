package committee.nova.neutron.server.command.impl

import committee.nova.dateutils.DateUtils
import committee.nova.neutron.implicits.{ICommandSenderImplicit, PlayerImplicit}
import committee.nova.neutron.server.command.init.CommandInit
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.Utilities.Str.timeFormatter
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.{ChatComponentText, ChatComponentTranslation, ChatStyle, EnumChatFormatting}
import net.minecraftforge.common.DimensionManager

import java.lang.management.ManagementFactory
import java.util

class CommandNeutron extends CommandBase {
  override def getCommandName: String = "neutron"

  override def getCommandUsage(sender: ICommandSender): String = if (!sender.isInstanceOf[EntityPlayerMP] || sender.asInstanceOf[EntityPlayerMP].isOp)
    Utilities.Str.convertStringArgsToString("/neutron help", "/neutron gc") else "/neutron help"

  override def processCommand(sender: ICommandSender, args: Array[String]): Unit = {
    if (args.length != 1) {
      sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
      return
    }
    if (args(0).equals("help")) {
      CommandInit.commands.filter(c => c.canCommandSenderUseCommand(sender)).foreach(c =>
        sender.addChatMessage(new ChatComponentText(s"${c.getCommandName} >> ${c.getCommandUsage(sender)}"))
      )
      return
    }
    if (!sender.isOp) {
      sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.noPerm")
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
      return
    }
    args(0) match {
      case "reload" =>
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.neutron.reload.inProgress")
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        try {
          ServerConfig.sync()
        } catch {
          case e: Exception =>
            e.printStackTrace()
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.neutron.reload.error")
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
            return
        }
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.neutron.reload.success")
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
      case e@("gc" | "tps") => {
        for (dim <- DimensionManager.getIDs) {
          val worldTickTime = Utilities.Math.mean(MinecraftServer.getServer.worldTickTimes.get(dim)) * 1.0E-6D
          val worldTps = 20.0 min (1000 / worldTickTime)
          sender.addChatMessage(new ChatComponentTranslation("commands.forge.tps.summary", s"DIM$dim", timeFormatter.format(worldTickTime), timeFormatter.format(worldTps))
            .setChatStyle(new ChatStyle().setColor(getColorFromTps(worldTps))))
        }
        val meanTickTime = Utilities.Math.mean(MinecraftServer.getServer.tickTimeArray) * 1.0E-6D
        val meanTps = 20.0 min (1000 / meanTickTime)
        sender.addChatMessage(new ChatComponentTranslation("commands.forge.tps.summary", "Overall", timeFormatter.format(meanTickTime), timeFormatter.format(meanTps))
          .setChatStyle(new ChatStyle().setColor(getColorFromTps(meanTps))))
        sender.addChatMessage(new ChatComponentServerTranslation("gc.neutron.upTime",
          Utilities.L10n.formatDate(DateUtils.formatDateDiff(ManagementFactory.getRuntimeMXBean.getStartTime)))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        sender.addChatMessage(new ChatComponentServerTranslation("gc.neutron.maxMemory", Runtime.getRuntime.maxMemory / 1048576)
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        sender.addChatMessage(new ChatComponentServerTranslation("gc.neutron.totalMemory", Runtime.getRuntime.totalMemory / 1048576)
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        sender.addChatMessage(new ChatComponentServerTranslation("gc.neutron.freeMemory", Runtime.getRuntime.freeMemory / 1048576)
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        for (world <- DimensionManager.getWorlds) sender.addChatMessage(new ChatComponentServerTranslation("gc.neutron.world",
          world.provider.dimensionId, world.theChunkProviderServer.loadedChunks.size,
          world.loadedEntityList.size, world.loadedTileEntityList.size)
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN))
        )
      }
      case _ => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
    }
  }

  override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = true

  override def addTabCompletionOptions(sender: ICommandSender, args: Array[String]): util.List[_] =
    CommandBase.getListOfStringsMatchingLastWord(args, (if (args.length != 1) Array() else if (sender.isOp) Array("reload", "gc", "tps", "help") else Array("help")
      ): _*)

  private def getColorFromTps(tps: Double): EnumChatFormatting = tps match {
    case a if a >= 19.0 => EnumChatFormatting.AQUA
    case b if b >= 16.0 => EnumChatFormatting.GREEN
    case c if c >= 12.0 => EnumChatFormatting.YELLOW
    case d if d >= 7.0 => EnumChatFormatting.RED
    case _ => EnumChatFormatting.DARK_RED
  }
}
