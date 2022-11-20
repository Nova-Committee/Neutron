package committee.nova.neutron.api.player

import committee.nova.neutron.api.player.storage.IHome
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

  def getFormerX: Double

  def getFormerY: Double

  def getFormerZ: Double

  def getFormerDim: Int

  def setFormerX(x: Double): Unit

  def setFormerY(y: Double): Unit

  def setFormerZ(z: Double): Unit

  def setFormerDim(dim: Int): Unit

  def hasNoValidFormerPos: Boolean
}
