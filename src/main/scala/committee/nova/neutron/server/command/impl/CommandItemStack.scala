package committee.nova.neutron.server.command.impl

import com.google.common.collect.ImmutableList
import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.util.Utilities
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.util.{ChatStyle, EnumChatFormatting}

import java.util
import scala.collection.mutable

object CommandItemStack {
  class Repair extends CommandBase {
    override def getCommandName: String = "repair"

    override def getCommandUsage(sender: ICommandSender): String = if (!sender.isInstanceOf[EntityPlayerMP]) Utilities.Str.convertStringArgsToString(
      "/repair [UserName]", "/repair all [UserName]", "/repair hand [UserName]")
    else Utilities.Str.convertStringArgsToString("/repair", "/repair hand", "/repair all",
      "/repair [UserName]", "/repair all [UserName]", "/repair hand [UserName]")

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      c match {
        case p: EntityPlayerMP => {
          args.length match {
            case 0 => repairItem(p, p, p.getHeldItem)
            case 1 => args(0) match {
              case "hand" => repairItem(p, p, p.getHeldItem)
              case "all" => repairItems(p, p)
              case y => {
                val target = Utilities.Player.getPlayer(p, y)
                target.foreach(t => repairItem(p, t, t.getHeldItem))
                if (target.isDefined) return
                p.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", y)
                  .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
              }
            }
            case 2 => {
              val target = Utilities.Player.getPlayer(p, args(1))
              if (target.isEmpty) {
                p.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.playerNotFound", args(1))
                  .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
                return
              }
              target.foreach(t => {
                args(0) match {
                  case "hand" => repairItem(p, t, t.getHeldItem)
                  case "all" => repairItems(p, t)
                  case y => p.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", y)
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
                }
              })
            }
            case _ => p.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(p))
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
          }
        }
        case z => {
          args.length match {
            case 1 => {
              val target = Utilities.Player.getPlayer(z, args(0))
              target.foreach(t => repairItem(z, t, t.getHeldItem))
              if (target.isDefined) return
              z.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(0))
                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            }
            case 2 => {
              val target = Utilities.Player.getPlayer(z, args(1))
              if (target.isEmpty) {
                z.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(1))
                  .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
                return
              }
              target.foreach(t => {
                args(0) match {
                  case "hand" => repairItem(z, t, t.getHeldItem)
                  case "all" => repairItems(z, t)
                  case y => z.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", y)
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
                }
              })
            }
            case _ => z.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(z))
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
          }
        }
      }
    }

    private def repairItem(sender: ICommandSender, target: EntityPlayerMP, stack: ItemStack): Unit = {
      if (stack == null) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.repair.empty")
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
        return
      }
      if (!stack.isItemStackDamageable) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.repair.notDamageable", stack.getDisplayName)
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
        return
      }
      stack.setItemDamage(0)
      if (target != sender) {
        target.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.repair.successBy", sender.getCommandSenderName, stack.getDisplayName)
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.repair.successOther",
          target.getDisplayName, stack.getDisplayName).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
      } else sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.repair.successSelf", stack.getDisplayName)
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
    }

    private def repairItems(sender: ICommandSender, target: EntityPlayerMP): Unit = {
      target.inventory.armorInventory.filter(p => p != null).foreach(s => repairItem(sender, target, s))
      target.inventory.mainInventory.filter(p => p != null).foreach(s => repairItem(sender, target, s))
    }

    override def addTabCompletionOptions(c: ICommandSender, args: Array[String]): util.List[_] = {
      val a = Array("hand", "all")
      args.length match {
        case 1 => {
          val c = new mutable.MutableList[String].++(a).++(MinecraftServer.getServer.getAllUsernames)
          CommandBase.getListOfStringsMatchingLastWord(args, c: _*)
        }
        case 2 => {
          if (a.contains(args(0))) CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer.getAllUsernames.toSeq: _*)
          else ImmutableList.of()
        }
      }
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean =
      !sender.isInstanceOf[EntityPlayerMP] || sender.asInstanceOf[EntityPlayerMP].isOp
  }
}
