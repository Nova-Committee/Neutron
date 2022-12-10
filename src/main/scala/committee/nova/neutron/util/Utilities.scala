package committee.nova.neutron.util

import committee.nova.dateutils.DateUtils
import committee.nova.neutron.Neutron
import committee.nova.neutron.compat.ProtonCompat
import committee.nova.neutron.implicits._
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import committee.nova.neutron.server.storage.ServerStorage
import committee.nova.sjl10n.L10nUtilities
import committee.nova.sjl10n.L10nUtilities.JsonText
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.util.{ChatComponentText, DamageSource, IChatComponent, Vec3}
import net.minecraft.world.WorldServer

import java.lang.{String => JString}
import java.math.{RoundingMode, BigDecimal => JDecimal}
import java.text.{DecimalFormat, MessageFormat}
import java.util.UUID
import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.Try

object Utilities {
  object L10n {
    val l10nMap: mutable.Map[String, JsonText] = new mutable.HashMap[String, JsonText]()

    def getFromCurrentLang(key: String): String = getL10n(ServerConfig.getLanguage).get(key)

    def getFromCurrentLang(key: String, args: Any*): String = MessageFormat.format(getFromCurrentLang(key), args.toArray.asInstanceOf[Array[AnyRef]].toSeq: _*)

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

    def formatDate(raw: String): String = {
      var formatted = raw
      for (i <- DateUtils.units.reverse) formatted = formatted.replaceAllLiterally(i, getFromCurrentLang(s"time.neutron.$i"))
      formatted
    }

    def getComponentArrayFromIterator[T](iterator: Iterator[T], converter: (T, Int) => String): mutable.MutableList[IChatComponent] = {
      val list = new mutable.MutableList[IChatComponent]
      iterator.zipWithIndex.foreach(z => list.+=(new ChatComponentText(converter.apply(z._1, z._2))))
      list
    }

    def getSpace: IChatComponent = new ChatComponentText(" ")

    def getEmpty: IChatComponent = new ChatComponentText("")

    def mergeComponent(parent: IChatComponent, children: IChatComponent*): IChatComponent = {
      var merged = parent
      for (c <- children) merged = merged.appendSibling(c)
      merged
    }
  }

  object Player {
    val suicide: DamageSource = new DamageSource("suicide") {
      override def func_151519_b(e: EntityLivingBase): IChatComponent = new ChatComponentServerTranslation("ann.neutron.suicide", e.func_145748_c_.getFormattedText)
    }.setDamageBypassesArmor().setDamageAllowedInCreativeMode()

    def getPlayer(sender: ICommandSender, name: String): Option[EntityPlayerMP] = Try(CommandBase.getPlayer(sender, name)).toOption

    def getPlayerByName(name: String): Option[EntityPlayerMP] = Option(MinecraftServer.getServer.getConfigurationManager.func_152612_a(name))

    def getPlayerNameByUUID(uuid: UUID): String = {
      ServerStorage.uuid2Name.get(uuid)
        .orElse(Try(getPlayerByUUID(uuid).get.getCommandSenderName).toOption)
        .getOrElse(L10n.getFromCurrentLang("phr.neutron.unknownPlayer"))
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

    def getPlayerSkull(name: String): ItemStack = new ItemStack(Items.skull, 1, 3).setTagDisplayName(name)
  }

  object Teleportation {
    def getSafeHeight(world: WorldServer, x: Int, z: Int): Int = {
      val y = world.getPrecipitationHeight(x, z)
      if (world.getBlock(x, y - 1, z).getBlocksMovement(world, x, y - 1, z)) Int.MinValue else y
    }

    @tailrec
    def getSafePosToTeleport(world: WorldServer, x: Int, z: Int, tries: Int): Option[Vec3] = {
      val dist = ServerConfig.getRtpMaxVerticalAxisRange
      val x1 = x - dist + world.rand.nextInt(2 * dist)
      val z1 = z - dist + world.rand.nextInt(2 * dist)
      val y = getSafeHeight(world, x1, z1)
      if (y != Int.MinValue) {
        world.markBlockForUpdate(x1, y, z1)
        return Some(Vec3.createVectorHelper(x1 + 0.5, y + 0.2, z1 + 0.5))
      }
      if (tries >= ServerConfig.getRtpMaxTriesOnFindingPosition) None else getSafePosToTeleport(world, x, z, tries + 1)
    }
  }

  object Location {
    def getLiteralFromPosTuple3(x: Double, y: Double, z: Double): String = s"[${scale1(x)}, ${scale1(y)}, ${scale1(z)}]"

    def getLiteralFromVec3(pos: Vec3): String = getLiteralFromPosTuple3(pos.xCoord, pos.yCoord, pos.zCoord)

    private def scale1(d: Double): String = Str.scale(d, 1)
  }

  object Str {
    val timeFormatter = new DecimalFormat("########0.000")

    def convertIteratorToString[T](iterator: Iterator[T], convertor: (T, Int) => String, prefix: String, suffix: String): String = {
      val buffer = new StringBuffer()
      buffer.append(prefix)
      iterator.zipWithIndex.foreach(c => buffer.append(convertor.apply(c._1, c._2).+(", ")))
      val lastIndex = buffer.lastIndexOf(",")
      if (lastIndex > 0) buffer.delete(lastIndex, buffer.length())
      buffer.append(suffix)
      buffer.toString
    }

    def convertStringArgsToString(array: String*): String = {
      convertIteratorToString[String](array.toIterator.filter(s => s.nonEmpty), (s, _) => s, "", "")
    }

    def scale(d: Double, scale: Int): String = {
      if (scale <= 0) return JString.valueOf(d)
      val decimal = new JDecimal(d).setScale(scale, RoundingMode.HALF_UP)
      JString.valueOf(decimal.doubleValue())
    }
  }

  object Math {
    def mean(values: Array[Long]): Long = {
      var sum = 0L
      for (v <- values) sum += v
      sum / values.length
    }
  }

  object Perm {
    private var protonLoaded = false

    def initPermCompat(): Unit = {
      protonLoaded = Try(Class.forName("committee.nova.proton.core.perm.Group")).isSuccess
    }

    def loadPermCompat(): Unit = {
      if (protonLoaded) ProtonCompat.init()
    }

    def isProtonLoaded: Boolean = protonLoaded

    def hasPermOrElse(player: EntityPlayerMP, perm: String, defaultValue: EntityPlayerMP => Boolean): Boolean = {
      if (isProtonLoaded) return ProtonCompat.hasPerm(player, perm)
      //...
      defaultValue.apply(player)
    }
  }
}
