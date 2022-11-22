package committee.nova.neutron.util

import committee.nova.neutron.Neutron
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.storage.ServerStorage
import committee.nova.sjl10n.L10nUtilities
import committee.nova.sjl10n.L10nUtilities.JsonText
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.{ChatComponentText, IChatComponent}
import net.minecraft.world.WorldServer

import java.lang.{String => JString}
import java.math.{RoundingMode, BigDecimal => JDecimal}
import java.util.UUID
import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.Try

object Utilities {
  object L10n {
    val l10nMap: mutable.Map[String, JsonText] = new mutable.HashMap[String, JsonText]()

    def getL10n(lang: String): JsonText = {
      l10nMap.foreach(m => if (lang == m._1) return m._2)
      val n = L10nUtilities.create(Neutron.MODID, lang)
      l10nMap.put(lang, n)
      n
    }

    def initializeL10n(lang: String): JsonText = {
      if (lang != "en_us") getL10n(lang)
      getL10n("en_us")
    }

    def getSpace: IChatComponent = new ChatComponentText(" ")

    def getEmpty: IChatComponent = new ChatComponentText("")
  }

  object Player {
    def getPlayer(sender: ICommandSender, name: String): Option[EntityPlayerMP] = Option(CommandBase.getPlayer(sender, name))

    def getPlayerByName(name: String): Option[EntityPlayerMP] = Option(MinecraftServer.getServer.getConfigurationManager.func_152612_a(name))

    def getPlayerNameByUUID(uuid: UUID): String = {
      ServerStorage.uuid2Name.get(uuid)
        .orElse(Try(getPlayerByUUID(uuid).get.getDisplayName).toOption)
        .getOrElse(L10n.getL10n(ServerConfig.getLanguage).get("phr.neutron.unknownPlayer"))
    }

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
    def getLiteralFromPosTuple3(pos: (Double, Double, Double)): String = s"[${scale1(pos._1)}, ${scale1(pos._2)}, ${scale1(pos._3)}]"

    private def scale1(d: Double): String = Str.scale(d, 1)
  }

  object Str {
    def convertCollectionToString[T](iterator: Iterator[T], convertor: (T, Int) => String): String = {
      val buffer = new StringBuffer()
      buffer.append("[")
      iterator.zipWithIndex.foreach(c => buffer.append(convertor.apply(c._1, c._2).+(", ")))
      buffer.delete(buffer.lastIndexOf(","), buffer.length())
      buffer.append("]")
      buffer.toString
    }

    def scale(d: Double, scale: Int): String = {
      if (scale <= 0) return JString.valueOf(d)
      val decimal = new JDecimal(d).setScale(scale, RoundingMode.HALF_UP)
      JString.valueOf(decimal.doubleValue())
    }
  }
}
