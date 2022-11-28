package committee.nova.neutron.server.player.storage

import committee.nova.neutron.Neutron
import committee.nova.neutron.api.player.storage.{IHome, IPosWithDim}
import committee.nova.neutron.implicits.named2Str
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.collection.LimitedLinkedList
import committee.nova.neutron.util.reference.Tags
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.world.World
import net.minecraftforge.common.IExtendedEntityProperties

import scala.collection.JavaConversions._
import scala.collection.mutable

object NeutronEEP {
  final val id = "neutron"
}

class NeutronEEP extends IExtendedEntityProperties {
  private var player: EntityPlayer = _
  private var world: World = _
  private var cdTpa: Int = 0
  private var rtpAccumulation: Int = 0
  private val homes: mutable.LinkedHashSet[IHome] = new mutable.LinkedHashSet[IHome]
  private val formerPosQueue: LimitedLinkedList[IPosWithDim] = new LimitedLinkedList[IPosWithDim]
  private val muteStatus: MuteStatus = new MuteStatus

  override def saveNBTData(tag: NBTTagCompound): Unit = {
    val neutronTag = new NBTTagCompound
    neutronTag.setInteger(Tags.CD_TPA, cdTpa)
    neutronTag.setInteger(Tags.ACCUMULATION_RTP, rtpAccumulation)
    val homesTag = new NBTTagList
    for (home <- homes) {
      homesTag.appendTag(home.serialize)
    }
    neutronTag.setTag(Tags.HOMES, homesTag)
    val formerPos = new NBTTagList
    for (pos <- formerPosQueue) {
      formerPos.appendTag(pos.serialize)
    }
    neutronTag.setTag(Tags.FORMER_POS, formerPos)
    val mute = new NBTTagCompound
    mute.setBoolean(Tags.APPLIED, muteStatus.isApplied)
    mute.setBoolean(Tags.BY_CONSOLE, muteStatus.isExecutedByConsole)
    mute.setString(Tags.NOTE, muteStatus.getNote)
    neutronTag.setTag(Tags.MUTE_STATUS, mute)
    tag.setTag(Tags.NEUTRON_ROOT, neutronTag)
  }

  override def loadNBTData(tag: NBTTagCompound): Unit = {
    if (!tag.hasKey(Tags.NEUTRON_ROOT)) return
    val neutronTag = tag.getCompoundTag(Tags.NEUTRON_ROOT)
    cdTpa = neutronTag.getInteger(Tags.CD_TPA)
    rtpAccumulation = neutronTag.getInteger(Tags.ACCUMULATION_RTP)
    if (neutronTag.hasKey(Tags.HOMES)) {
      homes.clear()
      val homesTag = neutronTag.getTagList(Tags.HOMES, 10)
      for (i <- 0 until homesTag.tagCount) homes.add(new Home().deserialize(homesTag.getCompoundTagAt(i)))
    }
    if (neutronTag.hasKey(Tags.FORMER_POS)) {
      formerPosQueue.clear()
      val formerPosTag = neutronTag.getTagList(Tags.FORMER_POS, 10)
      for (i <- 0 until formerPosTag.tagCount) formerPosQueue.add(new FormerPos().deserialize(formerPosTag.getCompoundTagAt(i)))
    }
    if (neutronTag.hasKey(Tags.MUTE_STATUS)) {
      val mute = neutronTag.getCompoundTag(Tags.MUTE_STATUS)
      muteStatus.setApplied(mute.getBoolean(Tags.APPLIED))
      muteStatus.setExecutedByConsole(mute.getBoolean(Tags.BY_CONSOLE))
      muteStatus.setNote(mute.getString(Tags.NOTE))
    }
  }

  override def init(entity: Entity, world: World): Unit = {
    if (!entity.isInstanceOf[EntityPlayer]) {
      Neutron.LOGGER.error(Utilities.L10n.getFromCurrentLang("msg.neutron.err.eepInit"))
      return
    }
    this.player = entity.asInstanceOf[EntityPlayer]
    this.world = world
  }

  def getRtpAccumulation: Int = rtpAccumulation

  def setRtpAccumulation(acc: Int): Unit = rtpAccumulation = acc

  def getTpaCoolDown: Int = cdTpa

  def setTpaCoolDown(cd: Int): Unit = cdTpa = cd

  def getHomes: mutable.LinkedHashSet[IHome] = homes

  def setHomes(homes: mutable.LinkedHashSet[IHome]): Unit = {
    this.homes.clear()
    this.homes.addAll(homes)
  }

  def getFormerPos: LimitedLinkedList[IPosWithDim] = formerPosQueue

  def setFormerPos(former: LimitedLinkedList[IPosWithDim]): Unit = {
    formerPosQueue.clear()
    formerPosQueue.addAll(former)
  }

  def getMuteStatus: MuteStatus = muteStatus
}
