package committee.nova.neutron.util

import committee.nova.neutron.Neutron
import committee.nova.sjl10n.L10nUtilities
import committee.nova.sjl10n.L10nUtilities.JsonText
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.{ChatComponentText, IChatComponent}

import java.util.UUID
import scala.collection.JavaConversions._
import scala.collection.mutable

object Utilities {
  val l10nMap: mutable.Map[String, JsonText] = new mutable.HashMap[String, JsonText]()

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

  def getL10n(lang: String): JsonText = {
    l10nMap.foreach(m => if (lang == m._1) return m._2)
    val n = L10nUtilities.create(Neutron.MODID, lang)
    l10nMap.put(lang, n)
    n
  }

  def getSpace: IChatComponent = new ChatComponentText(" ")

  def getEmpty: IChatComponent = new ChatComponentText("")
}
