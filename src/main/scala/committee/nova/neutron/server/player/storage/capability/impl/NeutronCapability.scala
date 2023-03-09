package committee.nova.neutron.server.player.storage.capability.impl

import committee.nova.neutron.Neutron.neutronCapability
import committee.nova.neutron.api.player.storage.{IHome, IPosWithDim}
import committee.nova.neutron.implicits._
import committee.nova.neutron.server.player.storage.capability.api.INeutronCapability
import committee.nova.neutron.server.player.storage.{FormerPos, Home, MuteStatus, StatsBeforeSuicide}
import committee.nova.neutron.util.collection.LimitedLinkedList
import committee.nova.neutron.util.reference.Tags
import net.minecraft.nbt.{NBTBase, NBTTagCompound, NBTTagList}
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability.IStorage
import net.minecraftforge.common.capabilities.{Capability, ICapabilitySerializable}

import scala.collection.JavaConversions._
import scala.collection.mutable

object NeutronCapability {
  class Provider extends ICapabilitySerializable[NBTTagCompound] {
    private val instance = new Impl

    private val storage = new Storage

    override def serializeNBT(): NBTTagCompound = {
      val tag = new NBTTagCompound
      tag.setTag("neutron", storage.writeNBT(neutronCapability, instance, null))
      tag
    }

    override def deserializeNBT(nbt: NBTTagCompound): Unit = {
      val tag = nbt.getCompoundTag("neutron")
      storage.readNBT(neutronCapability, instance, null, tag)
    }

    override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = neutronCapability == capability

    override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = if (neutronCapability == capability) neutronCapability.cast(instance) else null.asInstanceOf[T]
  }

  class Storage extends IStorage[INeutronCapability] {
    override def writeNBT(capability: Capability[INeutronCapability], instance: INeutronCapability, side: EnumFacing): NBTBase = {
      val neutronTag = new NBTTagCompound
      neutronTag.setInteger(Tags.CD_TPA, instance.getTpaCoolDown)
      neutronTag.setInteger(Tags.ACCUMULATION_RTP, instance.getRtpAccumulation)
      val homesTag = new NBTTagList
      for (home <- instance.getHomes) {
        homesTag.appendTag(home.serialize)
      }
      neutronTag.setTag(Tags.HOMES, homesTag)
      val formerPos = new NBTTagList
      for (pos <- instance.getFormerPos) {
        formerPos.appendTag(pos.serialize)
      }
      neutronTag.setTag(Tags.FORMER_POS, formerPos)
      val mute = new NBTTagCompound
      mute.setBoolean(Tags.APPLIED, instance.getMuteStatus.isApplied)
      mute.setBoolean(Tags.BY_CONSOLE, instance.getMuteStatus.isExecutedByConsole)
      mute.setString(Tags.NOTE, instance.getMuteStatus.getNote)
      neutronTag.setTag(Tags.MUTE_STATUS, mute)
      if (instance.getStatsBeforeSuicide.isValid) {
        val suicide = new NBTTagCompound
        suicide.setFloat(Tags.HEALTH, instance.getStatsBeforeSuicide.getHealth)
        suicide.setInteger(Tags.FOOD_LEVEL, instance.getStatsBeforeSuicide.getFoodLevel)
        suicide.setFloat(Tags.SATURATION, instance.getStatsBeforeSuicide.getSaturation)
        neutronTag.setTag(Tags.STATS_BEFORE_SUICIDE, suicide)
      }
      val tag = new NBTTagCompound
      tag.setTag(Tags.NEUTRON_ROOT, neutronTag)
      tag
    }

    override def readNBT(capability: Capability[INeutronCapability], instance: INeutronCapability, side: EnumFacing, nbt: NBTBase): Unit = {
      if (!nbt.isInstanceOf[NBTTagCompound]) return
      val tag = nbt.asInstanceOf[NBTTagCompound]
      if (!tag.hasKey(Tags.NEUTRON_ROOT)) return
      val neutronTag = tag.getCompoundTag(Tags.NEUTRON_ROOT)
      instance.setTpaCoolDown(neutronTag.getInteger(Tags.CD_TPA))
      instance.setRtpAccumulation(neutronTag.getInteger(Tags.ACCUMULATION_RTP))
      if (neutronTag.hasKey(Tags.HOMES)) {
        instance.getHomes.clear()
        val homesTag = neutronTag.getTagList(Tags.HOMES, 10)
        for (i <- 0 until homesTag.tagCount) instance.getHomes.add(new Home().deserialize(homesTag.getCompoundTagAt(i)))
      }
      if (neutronTag.hasKey(Tags.FORMER_POS)) {
        instance.getFormerPos.clear()
        val formerPosTag = neutronTag.getTagList(Tags.FORMER_POS, 10)
        for (i <- 0 until formerPosTag.tagCount) instance.getFormerPos.add(new FormerPos().deserialize(formerPosTag.getCompoundTagAt(i)))
      }
      if (neutronTag.hasKey(Tags.MUTE_STATUS)) {
        val mute = neutronTag.getCompoundTag(Tags.MUTE_STATUS)
        val status = instance.getMuteStatus
        status.setApplied(mute.getBoolean(Tags.APPLIED))
        status.setExecutedByConsole(mute.getBoolean(Tags.BY_CONSOLE))
        status.setNote(mute.getString(Tags.NOTE))
      }
      if (neutronTag.hasKey(Tags.STATS_BEFORE_SUICIDE)) {
        val s = instance.getStatsBeforeSuicide
        s.setValid(true)
        val suicide = neutronTag.getCompoundTag(Tags.STATS_BEFORE_SUICIDE)
        s.setHealth(suicide.getFloat(Tags.HEALTH))
        s.setFoodLevel(suicide.getInteger(Tags.FOOD_LEVEL))
        s.setSaturation(suicide.getFloat(Tags.SATURATION))
      }
    }
  }

  class Impl extends INeutronCapability {
    private var cdTpa: Int = 0
    private var rtpAccumulation: Int = 0
    private val homes: mutable.LinkedHashSet[IHome] = new mutable.LinkedHashSet[IHome]
    private val formerPosQueue: LimitedLinkedList[IPosWithDim] = new LimitedLinkedList[IPosWithDim]
    private val muteStatus: MuteStatus = new MuteStatus
    private val statsBeforeSuicide: StatsBeforeSuicide = new StatsBeforeSuicide

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

    def getStatsBeforeSuicide: StatsBeforeSuicide = statsBeforeSuicide
  }
}
