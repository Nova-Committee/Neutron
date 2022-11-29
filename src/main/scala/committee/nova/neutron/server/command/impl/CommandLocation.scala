package committee.nova.neutron.server.command.impl

import com.google.common.collect.ImmutableList
import committee.nova.neutron.api.player.storage.IHome
import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.event.impl.TeleportFromEvent
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.server.player.storage.{Home => SHome}
import committee.nova.neutron.util.Utilities
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.{ChatStyle, EnumChatFormatting, Vec3}
import net.minecraftforge.common.MinecraftForge

import java.util
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.Try
import scala.util.control.Breaks.{break, breakable}

object CommandLocation {
  class Home extends CommandBase {
    override def getCommandName: String = "home"

    override def getCommandUsage(sender: ICommandSender): String = "/home [HomeName]"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      val l = args.length
      val homes = sender.getHomes
      if (l == 0) homes.size() match {
        case 0 =>
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.invalid")
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)))
          return
        case 1 =>
          val home = homes.head
          MinecraftForge.EVENT_BUS.post(TeleportFromEvent(sender, sender.dimension, sender.posX, sender.posY, sender.posZ))
          val pos = home.getPos
          sender.teleport(home.getDim, pos.xCoord, pos.yCoord, pos.zCoord, sender.rotationYaw, sender.rotationPitch)
          sender.closeScreen()
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.teleport.success", home.getName)
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
          return
        case y if y > 1 => {
          //sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.vague", y).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
          //Utilities.L10n.getComponentArrayFromIterator(homes.iterator, (h: IHome, i: Int) => s"${i + 1}. ${h.getName} > DIM${h.getDim}:${Utilities.Location.getLiteralFromVec3(h.getPos)}")
          //  .foreach(i => sender.addChatMessage(i.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))))
          MinecraftServer.getServer.getCommandManager.executeCommand(sender, "/homegui")
          return
        }
      }
      if (l != 1) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        return
      }
      val name = args(0)
      homes.foreach(home => if (name == home.getName) {
        MinecraftForge.EVENT_BUS.post(TeleportFromEvent(sender, sender.dimension, sender.posX, sender.posY, sender.posZ))
        val pos = home.getPos
        sender.teleport(home.getDim, pos.xCoord, pos.yCoord, pos.zCoord, sender.rotationYaw, sender.rotationPitch)
        sender.closeScreen()
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.teleport.success", home.getName)
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
        return
      }
      )
      sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.notFound", name)
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
    }

    override def addTabCompletionOptions(c: ICommandSender, args: Array[String]): util.List[_] = {
      if (args.length != 1) return ImmutableList.of()
      c match {
        case p: EntityPlayerMP => CommandBase.getListOfStringsMatchingLastWord(args, p.getHomes.collect({
          case h: IHome => h.getName
          case _ => ""
        }).toSeq: _*)
      }
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = true
  }

  class SetHome extends CommandBase {
    override def getCommandName: String = "sethome"

    override def getCommandUsage(sender: ICommandSender): String = "/sethome ([HomeName])"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      val l = args.length
      val homes = sender.getHomes
      if (l == 0) {
        setHomeForPlayer(sender, SHome.getNextUnOccupiedHomeName(1, homes.map(h => h.getName).toArray), homes)
        return
      }
      if (l != 1) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        return
      }
      setHomeForPlayer(sender, args(0), homes)
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = true

    private def setHomeForPlayer(sender: EntityPlayerMP, name: String, homes: mutable.LinkedHashSet[IHome]): Unit = {
      if (homes.size() >= ServerConfig.getMaxHomeNumber) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.set.exceeded"))
        return
      }
      val home = new SHome(name, sender.dimension, Vec3.createVectorHelper(sender.posX, sender.posY, sender.posZ))
      val added = homes.add(home)
      sender.addChatMessage(new ChatComponentServerTranslation(if (added) "msg.neutron.cmd.home.set.success" else "msg.neutron.cmd.home.set.failure", home.getName)
        .setChatStyle(new ChatStyle().setColor(if (added) EnumChatFormatting.GREEN else EnumChatFormatting.RED)))
    }
  }

  class DelHome extends CommandBase {
    override def getCommandName: String = "delhome"

    override def getCommandUsage(sender: ICommandSender): String = "/delhome"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      val l = args.length
      if (l > 1) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        return
      }
      val homes = sender.getHomes
      val size = homes.size()
      if (size == 0) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.invalid")
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)))
        return
      }
      if (l == 0) {
        if (size == 1) {
          val homeName = homes.head.getName
          homes.clear()
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.del.success", homeName)
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
          return
        }
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.vague", homes.size())
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
        Utilities.L10n.getComponentArrayFromIterator(homes.iterator, (h: IHome, i) => s"${i + 1}. ${h.getName} > DIM${h.getDim}:${Utilities.Location.getLiteralFromVec3(h.getPos)}")
          .foreach(i => sender.addChatMessage(i.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))))
        return
      }
      val name = args(0)
      var removed = false
      breakable {
        for (home <- homes if name == home.getName) {
          removed = homes.remove(home)
          break()
        }
      }
      sender.addChatMessage(new ChatComponentServerTranslation(if (removed) "msg.neutron.cmd.home.del.success" else "msg.neutron.cmd.home.notFound", name)
        .setChatStyle(new ChatStyle().setColor(if (removed) EnumChatFormatting.YELLOW else EnumChatFormatting.RED)))
    }

    override def addTabCompletionOptions(c: ICommandSender, args: Array[String]): util.List[_] = {
      if (args.length != 1) return ImmutableList.of()
      c match {
        case p: EntityPlayerMP => CommandBase.getListOfStringsMatchingLastWord(args, p.getHomes.collect({
          case h: IHome => h.getName
          case _ => ""
        }).toSeq: _*)
      }
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = true
  }

  class Back extends CommandBase {
    override def getCommandName: String = "back"

    override def getCommandUsage(sender: ICommandSender): String = "/back"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      val l = args.length
      if (l > 1) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        return
      }
      val former = sender.getFormerPos
      if (l == 0) former.size() match {
        case 0 =>
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.back.invalid")
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)))
          return
        case 1 =>
          val pos = former.get(0)
          sender.teleport(pos.getDim, pos.getX, pos.getY, pos.getZ, sender.rotationYaw, sender.rotationPitch)
          sender.closeScreen()
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.back.success", Utilities.Location.getLiteralFromVec3(pos.getPos))
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
          return
        case y if y > 1 =>
          MinecraftServer.getServer.getCommandManager.executeCommand(sender, "/backgui")
          //sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.back.vague", y)
          //  .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
          //Utilities.L10n.getComponentArrayFromIterator(former.iterator, (f: IPosWithDim, i) =>
          //  s"${i + 1}. DIM${f.getDim}:${Utilities.Location.getLiteralFromVec3(f.getPos)}")
          //  .foreach(i => sender.addChatMessage(i.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))))
          return
      }
      Try(Integer.parseInt(args(0))).toOption match {
        case Some(i) =>
          val index = i - 1
          if (index < 0 || index >= former.size()) {
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.back.invalidNumber")
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
            return
          }
          val pos = former.get(index)
          sender.teleport(pos.getDim, pos.getX, pos.getY, pos.getZ, sender.rotationYaw, sender.rotationPitch)
          sender.closeScreen()
          sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.back.success", Utilities.Location.getLiteralFromVec3(pos.getPos))
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
        case None => sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(0))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)))
      }
    }

    override def addTabCompletionOptions(c: ICommandSender, args: Array[String]): util.List[_] = {
      if (args.length != 1) return ImmutableList.of()
      c match {
        case p: EntityPlayerMP => CommandBase.getListOfStringsMatchingLastWord(args, Range.inclusive(1, p.getHomes.size).map(i => String.valueOf(i)): _*)
        case _ => ImmutableList.of()
      }
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = true
  }
}
