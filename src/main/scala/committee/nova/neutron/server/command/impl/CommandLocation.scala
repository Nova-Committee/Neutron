package committee.nova.neutron.server.command.impl

import com.google.common.collect.ImmutableList
import committee.nova.neutron.api.player.storage.{IHome, IPosWithDim}
import committee.nova.neutron.implicits._
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.event.impl.TeleportFromEvent
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.server.player.storage.{Home => SHome}
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.PermNodes
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.{MobEffects, SoundEvents}
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.potion.PotionEffect
import net.minecraft.server.MinecraftServer
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.{BlockPos, Vec3d}
import net.minecraft.util.text.{Style, TextFormatting}
import net.minecraft.world.WorldServer
import net.minecraftforge.common.MinecraftForge

import java.util
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.Try
import scala.util.control.Breaks.{break, breakable}

object CommandLocation {
  class Home extends CommandBase {
    override def getName: String = "home"

    override def getUsage(sender: ICommandSender): String = "/home [HomeName]"

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      val l = args.length
      val homes = sender.getHomes
      if (l == 0) homes.size() match {
        case 0 =>
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.invalid")
            .setStyle(new Style().setColor(TextFormatting.GRAY)))
          return
        case 1 =>
          val home = homes.head
          MinecraftForge.EVENT_BUS.post(TeleportFromEvent(sender, sender.dimension, sender.posX, sender.posY, sender.posZ))
          val pos = home.getPos
          sender.teleport(home.getDim, pos.x, pos.y, pos.z, sender.rotationYaw, sender.rotationPitch)
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.teleport.success", home.getName)
            .setStyle(new Style().setColor(TextFormatting.GREEN)))
          return
        case y if y > 1 =>
          if (Utilities.Perm.hasPermOrElse(sender, PermNodes.Interactable.HOME_GUI, _ => true)) {
            server.getCommandManager.executeCommand(sender, "/homegui")
            return
          }
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.vague", y).setStyle(new Style().setColor(TextFormatting.RED)))
          Utilities.L10n.getComponentArrayFromIterator(homes.iterator, (h: IHome, i: Int) => s"${i + 1}. ${h.getName} > DIM${h.getDim}:${Utilities.Location.getLiteralFromVec3(h.getPos)}")
            .foreach(i => sender.sendMessage(i.setStyle(new Style().setColor(TextFormatting.RED))))
          return
      }
      if (l != 1) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        return
      }
      val name = args(0)
      homes.foreach(home => if (name == home.getName) {
        MinecraftForge.EVENT_BUS.post(TeleportFromEvent(sender, sender.dimension, sender.posX, sender.posY, sender.posZ))
        val pos = home.getPos
        sender.teleport(home.getDim, pos.x, pos.y, pos.z, sender.rotationYaw, sender.rotationPitch)
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.teleport.success", home.getName)
          .setStyle(new Style().setColor(TextFormatting.GREEN)))
        return
      }
      )
      sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.notFound", name)
        .setStyle(new Style().setColor(TextFormatting.RED)))
    }

    override def getTabCompletions(server: MinecraftServer, c: ICommandSender, args: Array[String], pos: BlockPos): util.List[String] = {
      if (args.length != 1) return ImmutableList.of()
      c match {
        case p: EntityPlayerMP => CommandBase.getListOfStringsMatchingLastWord(args, p.getHomes.collect({
          case h: IHome => h.getName
          case _ => ""
        }).toSeq: _*)
      }
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Location.HOME, _ => true)
      case _ => true
    }
  }

  class SetHome extends CommandBase {
    override def getName: String = "sethome"

    override def getUsage(sender: ICommandSender): String = "/sethome ([HomeName])"

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      val l = args.length
      val homes = sender.getHomes
      if (l == 0) {
        setHomeForPlayer(sender, SHome.getNextUnOccupiedHomeName(1, homes.map(h => h.getName).toArray), homes)
        return
      }
      if (l != 1) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        return
      }
      setHomeForPlayer(sender, args(0), homes)
    }

    private def setHomeForPlayer(sender: EntityPlayerMP, name: String, homes: mutable.LinkedHashSet[IHome]): Unit = {
      if (homes.size() >= ServerConfig.getMaxHomeNumber) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.set.exceeded"))
        return
      }
      val home = new SHome(name, sender.dimension, new Vec3d(sender.posX, sender.posY, sender.posZ))
      val added = homes.add(home)
      sender.sendMessage(new ChatComponentServerTranslation(if (added) "msg.neutron.cmd.home.set.success" else "msg.neutron.cmd.home.set.failure", home.getName)
        .setStyle(new Style().setColor(if (added) TextFormatting.GREEN else TextFormatting.RED)))
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Location.SET_HOME, _ => true)
      case _ => true
    }
  }

  class DelHome extends CommandBase {
    override def getName: String = "delhome"

    override def getUsage(sender: ICommandSender): String = "/delhome"

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      val l = args.length
      if (l > 1) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        return
      }
      val homes = sender.getHomes
      val size = homes.size()
      if (size == 0) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.invalid")
          .setStyle(new Style().setColor(TextFormatting.GRAY)))
        return
      }
      if (l == 0) {
        if (size == 1) {
          val homeName = homes.head.getName
          homes.clear()
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.del.success", homeName)
            .setStyle(new Style().setColor(TextFormatting.YELLOW)))
          return
        }
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.home.vague", homes.size())
          .setStyle(new Style().setColor(TextFormatting.RED)))
        Utilities.L10n.getComponentArrayFromIterator(homes.iterator, (h: IHome, i) => s"${i + 1}. ${h.getName} > DIM${h.getDim}:${Utilities.Location.getLiteralFromVec3(h.getPos)}")
          .foreach(i => sender.sendMessage(i.setStyle(new Style().setColor(TextFormatting.RED))))
        return
      }
      val name = args(0)
      var removed = false
      breakable {
        for (home <- homes if name == home.getName) {
          removed = homes.remove(home)
          break
        }
      }
      sender.sendMessage(new ChatComponentServerTranslation(if (removed) "msg.neutron.cmd.home.del.success" else "msg.neutron.cmd.home.notFound", name)
        .setStyle(new Style().setColor(if (removed) TextFormatting.YELLOW else TextFormatting.RED)))
    }

    override def getTabCompletions(server: MinecraftServer, c: ICommandSender, args: Array[String], pos: BlockPos): util.List[String] = {
      if (args.length != 1) return ImmutableList.of()
      c match {
        case p: EntityPlayerMP => CommandBase.getListOfStringsMatchingLastWord(args, p.getHomes.collect({
          case h: IHome => h.getName
          case _ => ""
        }).toSeq: _*)
      }
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Location.DEL_HOME, _ => true)
      case _ => true
    }
  }

  class Back extends CommandBase {
    override def getName: String = "back"

    override def getUsage(sender: ICommandSender): String = "/back"

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      val l = args.length
      if (l > 1) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        return
      }
      val former = sender.getFormerPos
      if (l == 0) former.size() match {
        case 0 =>
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.back.invalid")
            .setStyle(new Style().setColor(TextFormatting.GRAY)))
          return
        case 1 =>
          val pos = former.get(0)
          sender.teleport(pos.getDim, pos.getX, pos.getY, pos.getZ, sender.rotationYaw, sender.rotationPitch)
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.back.success", Utilities.Location.getLiteralFromVec3(pos.getPos))
            .setStyle(new Style().setColor(TextFormatting.GREEN)))
          return
        case y if y > 1 =>
          if (Utilities.Perm.hasPermOrElse(sender, PermNodes.Interactable.BACK_GUI, _ => true)) {
            server.getCommandManager.executeCommand(sender, "/backgui")
            return
          }
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.back.vague", y)
            .setStyle(new Style().setColor(TextFormatting.RED)))
          Utilities.L10n.getComponentArrayFromIterator(former.iterator, (f: IPosWithDim, i) =>
            s"${i + 1}. DIM${f.getDim}:${Utilities.Location.getLiteralFromVec3(f.getPos)}")
            .foreach(i => sender.sendMessage(i.setStyle(new Style().setColor(TextFormatting.RED))))
          return
      }
      Try(Integer.parseInt(args(0))).toOption match {
        case Some(i) =>
          val index = i - 1
          if (index < 0 || index >= former.size()) {
            sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.back.invalidNumber")
              .setStyle(new Style().setColor(TextFormatting.DARK_RED)))
            return
          }
          val pos = former.get(index)
          sender.teleport(pos.getDim, pos.getX, pos.getY, pos.getZ, sender.rotationYaw, sender.rotationPitch)
          sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.back.success", Utilities.Location.getLiteralFromVec3(pos.getPos))
            .setStyle(new Style().setColor(TextFormatting.GREEN)))
        case None => sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.err.illegalArg", args(0))
          .setStyle(new Style().setColor(TextFormatting.DARK_RED)))
      }
    }

    override def getTabCompletions(server: MinecraftServer, c: ICommandSender, args: Array[String], pos: BlockPos): util.List[String] = {
      if (args.length != 1) return ImmutableList.of()
      c match {
        case p: EntityPlayerMP => CommandBase.getListOfStringsMatchingLastWord(args, Range.inclusive(1, p.getHomes.size).map(i => String.valueOf(i)): _*)
        case _ => ImmutableList.of()
      }
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Location.BACK, _ => true)
      case _ => true
    }
  }


  class Rtp extends CommandBase {
    override def getName: String = "rtp"

    override def getUsage(sender: ICommandSender): String = "/rtp"

    override def execute(server: MinecraftServer, c: ICommandSender, args: Array[String]): Unit = {
      if (!c.isInstanceOf[EntityPlayerMP]) return
      val sender = c.asInstanceOf[EntityPlayerMP]
      if (args.length > 0) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.usage", getUsage(sender))
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        return
      }
      val tries = sender.getRtpAccumulation * 1.0 / ServerConfig.getRtpChancesRecoveryTime + 1
      if (tries > ServerConfig.getMaxRtpChances) {
        sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.rtp.ranOut", sender.getRtpAccumulation % ServerConfig.getRtpChancesRecoveryTime)
          .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        return
      }
      val target = Utilities.Teleportation.getSafePosToTeleport(sender.world.asInstanceOf[WorldServer], sender, 0)
      target.foreach(p => {
        sender.dismountRidingEntity()
        sender.teleport(sender.dimension, p.x, p.y, p.z, sender.rotationYaw, sender.rotationPitch)
        sender.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 0))
      })
      if (target.isDefined) {
        sender.setRtpAccumulation(sender.getRtpAccumulation + ServerConfig.getRtpChancesRecoveryTime)
        sender.connection.sendPacket(new SPacketSoundEffect(SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, sender.posX, sender.posY, sender.posZ, 1.0F, 1.0F))
        target.foreach(p => sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.rtp.success", Utilities.Location.getLiteralFromVec3(p), (ServerConfig.getMaxRtpChances - sender.getRtpAccumulation * 1.0 / ServerConfig.getRtpChancesRecoveryTime).toInt)
          .setStyle(new Style().setColor(TextFormatting.GREEN))))
      }
      else sender.sendMessage(new ChatComponentServerTranslation("msg.neutron.cmd.rtp.triesExceeded", ServerConfig.getRtpMaxTriesOnFindingPosition)
        .setStyle(new Style().setColor(TextFormatting.DARK_RED)))
    }

    override def checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender match {
      case p: EntityPlayerMP => Utilities.Perm.hasPermOrElse(p, PermNodes.Location.RTP, _ => true)
      case _ => true
    }
  }
}
