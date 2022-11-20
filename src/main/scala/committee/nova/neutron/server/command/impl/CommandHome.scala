package committee.nova.neutron.server.command.impl

import committee.nova.neutron.api.player.storage.IHome
import committee.nova.neutron.implicits.PlayerImplicit
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.server.player.storage.{Home => SHome}
import committee.nova.neutron.util.Utilities
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.{ChatStyle, EnumChatFormatting}

import java.util
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.util.control.Breaks.{break, breakable}

object CommandHome {
  class Home extends CommandBase {
    override def getCommandName: String = "home"

    override def getCommandUsage(sender: ICommandSender): String = "/home [homeName]"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      val l = args.length
      val homes = sender.getHomes
      if (l == 0) {
        homes.size() match {
          case 0 =>
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.invalid")
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)))
            return
          case 1 =>
            val home = homes.asScala.head
            if (home.getDim != sender.dimension) sender.travelToDimension(home.getDim)
            val pos = home.getPos
            sender.playerNetServerHandler.setPlayerLocation(pos._1, pos._2, pos._3, sender.rotationYaw, sender.rotationPitch)
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.teleport.success", home.getName)
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
            return
          case y if y > 1 =>
            sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.vague", y,
              Utilities.String.convertCollectionToString(homes.asScala, (h: IHome) => h.getName))
              .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
            return
        }
      }
      if (l != 1) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getCommandUsage(sender))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)))
        return
      }
      val name = args(0)
      homes.asScala.foreach(home => if (name == home.getName) {
        if (home.getDim != sender.dimension) sender.travelToDimension(home.getDim)
        val pos = home.getPos
        sender.playerNetServerHandler.setPlayerLocation(pos._1, pos._2, pos._3, sender.rotationYaw, sender.rotationPitch)
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.teleport.success", home.getName)
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)))
        return
      }
      )
      sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.notFound", name)
        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
    }

    override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = true
  }

  class SetHome extends CommandBase {
    override def getCommandName: String = "sethome"

    override def getCommandUsage(sender: ICommandSender): String = "/sethome"

    override def processCommand(c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      val l = args.length
      val homes = sender.getHomes
      if (l == 0) {
        setHomeForPlayer(sender, SHome.getNextUnOccupiedHomeName(1, homes.asScala.map(h => h.getName).toArray), homes)
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

    private def setHomeForPlayer(sender: EntityPlayerMP, name: String, homes: util.HashSet[IHome]): Unit = {
      if (homes.size() >= ServerConfig.getMaxHomeNumber) {
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.set.exceeded"))
        return
      }
      val home = new SHome(name, sender.dimension, (sender.posX, sender.posY, sender.posZ))
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
        sender.addChatMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.vague", homes.size(),
          Utilities.String.convertCollectionToString(homes.asScala, (h: IHome) => h.getName))
          .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
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
  }
}
