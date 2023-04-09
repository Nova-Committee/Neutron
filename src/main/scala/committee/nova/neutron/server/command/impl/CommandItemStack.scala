package committee.nova.neutron.server.command.impl

import com.google.common.collect.ImmutableList
import committee.nova.neutron.implicits._
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.PermNodes
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{Style, TextFormatting}

import java.util
import scala.collection.JavaConversions._
import scala.collection.mutable

object CommandItemStack {
  class Repair extends CommandBase {
    override def getName: String = "repair"

    override def getUsage(sender: ICommandSender): String = if (!sender.isInstanceOf[EntityPlayerMP]) Utilities.Str.convertStringArgsToString(
      "/repair [UserName]", "/repair all [UserName]", "/repair hand [UserName]")
    else Utilities.Str.convertStringArgsToString("/repair", "/repair hand", "/repair all",
      "/repair [UserName]", "/repair all [UserName]", "/repair hand [UserName]")

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      c match {
        case p: EntityPlayerMP =>
          args.length match {
            case 0 => repairItem(p, p, p.getHeldItem(EnumHand.MAIN_HAND))
            case 1 => args(0) match {
              case "hand" => repairItem(p, p, p.getHeldItem(EnumHand.MAIN_HAND))
              case "all" => if (p.hasPermOrElse(PermNodes.ItemStack.REPAIR_ALL, p.isOp)) repairItems(p, p) else repairItem(p, p, p.getHeldItem(EnumHand.MAIN_HAND))
              case y =>
                if (!p.hasPermOrElse(PermNodes.ItemStack.REPAIR_OTHER, p.isOp)) {
                  p.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.noPerm")
                    .setStyle(new Style().setColor(TextFormatting.RED)))
                  return
                }
                val target = Utilities.Player.getPlayer(server, p, y)
                target.foreach(t => repairItem(p, t, t.getHeldItem(EnumHand.MAIN_HAND)))
                if (target.isDefined) return
                p.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", y)
                  .setStyle(new Style().setColor(TextFormatting.RED)))
            }
            case 2 =>
              if (!p.hasPermOrElse(PermNodes.ItemStack.REPAIR_OTHER, p.isOp)) {
                p.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.noPerm")
                  .setStyle(new Style().setColor(TextFormatting.RED)))
                return
              }
              val target = Utilities.Player.getPlayer(server, p, args(1))
              if (target.isEmpty) {
                p.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.playerNotFound", args(1))
                  .setStyle(new Style().setColor(TextFormatting.DARK_RED)))
                return
              }
              target.foreach(t => {
                args(0) match {
                  case "hand" => repairItem(p, t, t.getHeldItem(EnumHand.MAIN_HAND))
                  case "all" => repairItems(p, t)
                  case y => p.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", y)
                    .setStyle(new Style().setColor(TextFormatting.RED)))
                }
              })
            case _ => p.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(p))
              .setStyle(new Style().setColor(TextFormatting.YELLOW)))
          }
        case z =>
          args.length match {
            case 1 =>
              val target = Utilities.Player.getPlayer(server, z, args(0))
              target.foreach(t => repairItem(z, t, t.getHeldItem(EnumHand.MAIN_HAND)))
              if (target.isDefined) return
              z.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(0))
                .setStyle(new Style().setColor(TextFormatting.RED)))
            case 2 =>
              val target = Utilities.Player.getPlayer(server, z, args(1))
              if (target.isEmpty) {
                z.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(1))
                  .setStyle(new Style().setColor(TextFormatting.RED)))
                return
              }
              target.foreach(t => {
                args(0) match {
                  case "hand" => repairItem(z, t, t.getHeldItem(EnumHand.MAIN_HAND))
                  case "all" => repairItems(z, t)
                  case y => z.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", y)
                    .setStyle(new Style().setColor(TextFormatting.RED)))
                }
              })
            case _ => z.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(z))
              .setStyle(new Style().setColor(TextFormatting.YELLOW)))
          }
      }
    }

    private def repairItem(sender: ICommandSender, target: EntityPlayerMP, stack: ItemStack): Unit = {
      if (stack.isEmpty) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.repair.empty")
          .setStyle(new Style().setColor(TextFormatting.RED)))
        return
      }
      if (!stack.isItemStackDamageable) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.repair.notDamageable", stack.getDisplayName)
          .setStyle(new Style().setColor(TextFormatting.RED)))
        return
      }
      stack.setItemDamage(0)
      if (target != sender) {
        target.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.repair.successBy", sender.getName, stack.getDisplayName)
          .setStyle(new Style().setColor(TextFormatting.GREEN)))
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.repair.successOther",
          target.getName, stack.getDisplayName).setStyle(new Style().setColor(TextFormatting.GREEN)))
      } else sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.repair.successSelf", stack.getDisplayName)
        .setStyle(new Style().setColor(TextFormatting.GREEN)))
    }

    private def repairItems(sender: ICommandSender, target: EntityPlayerMP): Unit = {
      target.inventory.armorInventory.filter(p => !p.isEmpty).foreach(s => repairItem(sender, target, s))
      target.inventory.mainInventory.filter(p => !p.isEmpty).foreach(s => repairItem(sender, target, s))
    }

    override def getTabCompletions(server: MinecraftServer, c: ICommandSender, args: Array[String], pos: BlockPos): util.List[String] = {
      if (!c.isInstanceOf[EntityPlayerMP]) return ImmutableList.of()
      val sender = c.asInstanceOf[EntityPlayerMP]
      val a = Array("hand", if (sender.hasPermOrElse(PermNodes.ItemStack.REPAIR_ALL, sender.isOp)) "all" else "")
      args.length match {
        case 1 =>
          val z = new mutable.MutableList[String].++(a).++(server.getOnlinePlayerNames)
          CommandBase.getListOfStringsMatchingLastWord(args, z: _*)
        case 2 =>
          if (a.contains(args(0))) CommandBase.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames.toSeq: _*)
          else ImmutableList.of()
      }
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Array(PermNodes.ItemStack.REPAIR_ONE, PermNodes.ItemStack.REPAIR_OTHER, PermNodes.ItemStack.REPAIR_ALL)
        .exists(n => p.hasPermOrElse(n.getName, p.isOp))
      case _ => true
    }
  }
}
