package committee.nova.neutron.server.command.impl

import com.google.common.collect.ImmutableList
import committee.nova.dateutils.DateUtils
import committee.nova.neutron.implicits._
import committee.nova.neutron.server.command.init.CommandInit
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.Utilities.Str.timeFormatter
import committee.nova.neutron.util.reference.PermNodes
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{Style, TextComponentString, TextComponentTranslation, TextFormatting}
import net.minecraftforge.common.DimensionManager

import java.lang.management.ManagementFactory
import java.util

class CommandNeutron extends CommandBase {
  override def getName: String = "neutron"

  override def getUsage(sender: ICommandSender): String = if (!sender.isInstanceOf[EntityPlayerMP] || sender.asInstanceOf[EntityPlayerMP].isOp)
    Utilities.Str.convertStringArgsToString("/neutron help", "/neutron gc") else "/neutron help"

  override def execute(server: MinecraftServer, sender: ICommandSender, args: Array[String]): Unit = {
    if (args.length != 1) {
      sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
        .setStyle(new Style().setColor(TextFormatting.YELLOW)))
      return
    }
    if (args(0).equals("help")) {
      CommandInit.commands.filter(c => c.checkPermission(server, sender)).foreach(c =>
        sender.sendMessage(new TextComponentString(s"${c.getName} >> ${c.getUsage(sender)}"))
      )
      return
    }
    sender match {
      case p: EntityPlayerMP => if (!Utilities.Perm.hasPermOrElse(p, PermNodes.Neutron.MANAGE, _ => p.isOp)) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.noPerm")
          .setStyle(new Style().setColor(TextFormatting.RED)))
        return
      }
      case _ =>
    }
    args(0) match {
      case "reload" =>
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.neutron.reload.inProgress")
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        try {
          ServerConfig.sync()
        } catch {
          case e: Exception =>
            e.printStackTrace()
            sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.neutron.reload.error")
              .setStyle(new Style().setColor(TextFormatting.DARK_RED)))
            return
        }
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.neutron.reload.success")
          .setStyle(new Style().setColor(TextFormatting.GREEN)))
      case _@("gc" | "tps") =>
        for (dim <- DimensionManager.getIDs) {
          val worldTickTime = Utilities.Math.mean(server.worldTickTimes.get(dim)) * 1.0E-6D
          val worldTps = 20.0 min (1000 / worldTickTime)
          sender.sendMessage(new TextComponentTranslation("commands.forge.tps.summary", s"DIM$dim", timeFormatter.format(worldTickTime), timeFormatter.format(worldTps))
            .setStyle(new Style().setColor(getColorFromTps(worldTps))))
        }
        val meanTickTime = Utilities.Math.mean(server.tickTimeArray) * 1.0E-6D
        val meanTps = 20.0 min (1000 / meanTickTime)
        sender.sendMessage(new TextComponentTranslation("commands.forge.tps.summary", "Overall", timeFormatter.format(meanTickTime), timeFormatter.format(meanTps))
          .setStyle(new Style().setColor(getColorFromTps(meanTps))))
        sender.sendMessage(new ChatComponentServerTranslation("gc.neutron.upTime",
          Utilities.L10n.formatDate(DateUtils.formatDateDiff(ManagementFactory.getRuntimeMXBean.getStartTime)))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        sender.sendMessage(new ChatComponentServerTranslation("gc.neutron.maxMemory", Runtime.getRuntime.maxMemory / 1048576)
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        sender.sendMessage(new ChatComponentServerTranslation("gc.neutron.totalMemory", Runtime.getRuntime.totalMemory / 1048576)
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        sender.sendMessage(new ChatComponentServerTranslation("gc.neutron.freeMemory", Runtime.getRuntime.freeMemory / 1048576)
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        for (world <- DimensionManager.getWorlds) sender.sendMessage(new ChatComponentServerTranslation("gc.neutron.world",
          world.provider.getDimension, world.getChunkProvider.loadedChunks.size,
          world.loadedEntityList.size, world.loadedTileEntityList.size)
          .setStyle(new Style().setColor(TextFormatting.GREEN))
        )
      case _ => sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
        .setStyle(new Style().setColor(TextFormatting.YELLOW)))
    }
  }

  override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = true

  override def getTabCompletions(server: MinecraftServer, sender: ICommandSender, args: Array[String], pos: BlockPos): util.List[String] = {
    sender match {
      case p: EntityPlayerMP => CommandBase.getListOfStringsMatchingLastWord(args, (if (args.length != 1) Array() else if (Utilities.Perm.hasPermOrElse(p, PermNodes.Neutron.MANAGE, _ => p.isOp)) Array("reload", "gc", "tps", "help") else Array("help")
        ): _*)
      case _ => ImmutableList.of()
    }
  }

  private def getColorFromTps(tps: Double): TextFormatting = tps match {
    case a if a >= 19.0 => TextFormatting.AQUA
    case b if b >= 16.0 => TextFormatting.GREEN
    case c if c >= 12.0 => TextFormatting.YELLOW
    case d if d >= 7.0 => TextFormatting.RED
    case _ => TextFormatting.DARK_RED
  }
}
