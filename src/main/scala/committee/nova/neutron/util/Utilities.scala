package committee.nova.neutron.util

import committee.nova.neutron.Neutron
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.sjl10n.L10nUtilities
import committee.nova.sjl10n.L10nUtilities.JsonText
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.{ChatComponentText, IChatComponent}
import net.minecraft.world.WorldServer

import java.util.UUID
import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.mutable

object Utilities {
  object L10n {
    val l10nMap: mutable.Map[String, JsonText] = new mutable.HashMap[String, JsonText]()

    def getL10n(lang: String): JsonText = {
      l10nMap.foreach(m => if (lang == m._1) return m._2)
      val n = L10nUtilities.create(Neutron.MODID, lang)
      l10nMap.put(lang, n)
      n
    }

    def getSpace: IChatComponent = new ChatComponentText(" ")

    def getEmpty: IChatComponent = new ChatComponentText("")
  }

  object Player {
    def getPlayer(sender: ICommandSender, name: String): Option[EntityPlayerMP] = Option(CommandBase.getPlayer(sender, name))

    def getPlayerByName(name: String): Option[EntityPlayerMP] = Option(MinecraftServer.getServer.getConfigurationManager.func_152612_a(name))

    def getPlayerByUUID(uuid: UUID): Option[EntityPlayerMP] = {
      for (o <- MinecraftServer.getServer.getConfigurationManager.playerEntityList) {
        o match {
          case p: EntityPlayerMP if uuid == p.getUniqueID => return Option(p)
          case _ =>
        }
      }
      None
    }
  }

  object Teleportation {
    def getSafeHeight(world: WorldServer, x: Int, z: Int): Int = {
      val y = world.getPrecipitationHeight(x, z)
      if (world.getBlock(x, y - 1, z).getBlocksMovement(world, x, y - 1, z)) Int.MinValue else y
    }

    @tailrec
    def getSafePosToTeleport(world: WorldServer, x: Int, z: Int, tries: Int): Option[(Double, Double, Double)] = {
      val dist = ServerConfig.getRtpMaxVerticalAxisRange
      val x1 = x - dist + world.rand.nextInt(2 * dist)
      val z1 = z - dist + world.rand.nextInt(2 * dist)
      val y = getSafeHeight(world, x1, z1)
      if (y != Int.MinValue) {
        world.markBlockForUpdate(x1, y, z1)
        return Some((x1 + 0.5, y + 0.2, z1 + 0.5))
      }
      if (tries >= ServerConfig.getRtpMaxTriesOnFindingPosition) None else getSafePosToTeleport(world, x, z, tries + 1)
    }
  }

  object Location {
    def getLiteralFromPosTuple3(pos: (Double, Double, Double)): String = s"[${pos._1}, ${pos._2}, ${pos._3}]"
  }

  object String {
    def convertCollectionToString[T](traversable: Traversable[T], convertor: T => String): String = {
      val buffer = new StringBuffer()
      buffer.append("[")
      traversable.foreach(c => buffer.append(convertor.apply(c).+(", ")))
      buffer.delete(buffer.lastIndexOf(","), buffer.length())
      buffer.append("]")
      buffer.toString
    }
  }
}
