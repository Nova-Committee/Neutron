package committee.nova.neutron.api.player

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTTagCompound

trait INeutronPlayer {
  def getRtpAccumulation: Int

  def setRtpAccumulation(acc: Int): Unit

  def getTpaCoolDown: Int

  def setTpaCoolDown(cd: Int): Unit

  def teleportTo(that: EntityPlayerMP): Boolean

  def write(tag: NBTTagCompound): Unit

  def read(tag: NBTTagCompound): Unit
}
