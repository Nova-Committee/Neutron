package committee.nova.neutron.api.player.storage

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.Vec3

trait IPosWithDim {
  def getDim: Int

  def setDim(dim: Int): Unit

  def getPos: Vec3

  def setPos(pos: Vec3): Unit

  def getX: Double = getPos.xCoord

  def getY: Double = getPos.yCoord

  def getZ: Double = getPos.zCoord

  def serialize: NBTTagCompound

  def deserialize(tag: NBTTagCompound): IPosWithDim
}
