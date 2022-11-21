package committee.nova.neutron.api.player

import committee.nova.neutron.api.player.storage.{IHome, IPosWithDim}
import committee.nova.neutron.util.collection.LimitedLinkedList
import net.minecraft.entity.player.EntityPlayerMP

import java.util.{HashSet => JSet}

trait INeutronPlayer {
  def getRtpAccumulation: Int

  def setRtpAccumulation(acc: Int): Unit

  def getTpaCoolDown: Int

  def setTpaCoolDown(cd: Int): Unit

  def teleportTo(that: EntityPlayerMP): Boolean

  def getHomes: JSet[IHome]

  def setHomes(homes: JSet[IHome]): Unit

  def getFormerPos: LimitedLinkedList[IPosWithDim]

  def setFormerPos(former: LimitedLinkedList[IPosWithDim]): Unit
}
